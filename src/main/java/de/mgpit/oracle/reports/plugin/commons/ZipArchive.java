package de.mgpit.oracle.reports.plugin.commons;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import org.apache.log4j.Logger;

import de.mgpit.oracle.reports.plugin.commons.io.IOUtility;

/**
 * 
 * @author mgp
 *         <p>
 *         ZipArchive implements a flat ZIP file (i.e. no directory structure). The ZipArchive is used
 *         by the ZipDestination for storing the files of a distribution process.
 *         <p>
 *         The archive can be opened in appending or overwriting mode by calling
 *         one of the classes factory methods named {@link #newOrExistingNamed(String)} or {@link #newNamed(String)}.
 *         <p>
 *         New entries will be added by providing a file name and an entry name to {@link #addFile(String, String)}.
 *         During one distribution entry names must be unique, i.e. each call {@link #addFile(String, String)} must provide different
 *         entry name.
 *         <p>
 *         At the end clients of ZipArchive have to {@link #close()} the ZIP archive.
 *         <p>
 *         When appending to an existing archive the ZipArchive will partially be able to handle duplicate entries, meaning that
 *         entries from the existing archive will be replaced by new entries with the same name.
 *         <p>
 *         Any errors during processing will be wrapped in a {@link ArchivingException}.
 * 
 */
public class ZipArchive {

    private static final Logger LOG = Logger.getLogger( ZipArchive.class );

    private String fileName;
    private boolean appending = false;
    private boolean open = false;
    private ZipOutputStream zipper;
    private File temporaryFile;

    private Map entriesCreated;

    /**
     * Factory method for a new ZipArchive.
     * Will replace an existing ZIP file with the same name.
     * 
     * @param fileName
     *            full file name of the ZIP archive to be created
     * @return a ZipFile instance
     * 
     * @throws Error
     *             if fileName is provided as null or empty String
     */
    public static ZipArchive newNamed( final String fileName ) {
        U.assertNotEmpty( fileName, "fileName must not be null or empty string!" );
        ZipArchive archive = new ZipArchive( fileName );
        return archive;
    }

    /**
     * Factory method for creating a new ZIP archive in appending mode.
     * A ZIP archive in appending mode will add new entries to the archive and/or replace existing entries.
     * 
     * @param fileName
     * @return a ZipFile instance
     * 
     * @throws Error
     *             if fileName is provided as null or empty String
     */
    public static ZipArchive newOrExistingNamed( final String fileName ) {
        U.assertNotEmpty( fileName, "fileName must not be null or empty string!" );
        ZipArchive archive = new ZipArchive( fileName ).forAppending();
        return archive;
    }

    private ZipArchive forAppending() {
        this.appending = true;
        return this;
    }

    private ZipArchive() {
    }

    private ZipArchive( final String fileName ) {
        this.fileName = fileName;
        createTemporaryFileFromName( fileName );
    }

    private ZipArchive createTemporaryFileFromName( final String fileName ) {
        temporaryFile = new File( fileName + ".part" );
        return this;
    }

    /**
     * Closes the ZIP Archive.
     * <p>
     * Until here the new entries/content have been written to a temporary file.
     * In Append mode the entries of an already existing file will now be copied to the temporary file.
     * Finally the temporary file will be renamed to the destination file name.
     * 
     * @return the receiving ZipArchive instance
     * @throws ArchivingException
     *             on any error during copying or renaming
     */
    public ZipArchive close() throws ArchivingException {
        if ( !this.isOpen() ) {
            return this;
        }

        try {
            final File destinationFile = new File( getFileName() );
            if ( destinationFile.exists() ) {
                if ( isAppending() ) {
                    copyExistingEntries( destinationFile );
                }
                destinationFile.delete();
            }
            this.zipper.close();
            markClosed();
            final boolean successful = temporaryFile.renameTo( destinationFile );
            LOG.info( "Creation of " + getFileName() + " was" + (successful ? " " : " not") + "successful." );
        } catch ( IOException ioException ) {
            final String message = "Error when closing Zip archive " + getFileName();
            LOG.error( message, ioException );
            throw new ArchivingException( message, ioException );
        }
        return this;
    }

    /**
     * Called when the ZIP file already exists an we are in Append mode.
     * Copies the entries from the existing archive to the temporary ZIP archive.
     * 
     * @param existingZipFile
     *            the already existing ZIP file
     * @param zipper
     *            the temporary ZIP file used during creation
     * @throws IOException
     */
    private void copyExistingEntries( final File existingZipFile ) throws IOException {
        LOG.info( "About to copy entries from existing ZIP archive ..." );
        final ZipFile zipFile = new ZipFile( existingZipFile );
        final Enumeration entries = zipFile.entries();
        while ( entries.hasMoreElements() ) {
            final ZipEntry anEntry = (ZipEntry) entries.nextElement();
            if ( !hasEntry( anEntry ) ) {
                LOG.debug( " *** Copying entry " + anEntry + " from existing archive ..." );
                if ( !anEntry.isDirectory() ) {
                    InputStream zipped = zipFile.getInputStream( anEntry );
                    createEntry( anEntry, zipped );
                } else {
                    this.zipper.closeEntry();
                }
            } else {
                LOG.debug( " *** Skipping entry " + anEntry + ". Newer version exists." );
            }
        }
        zipFile.close();
    }

    /**
     * Creates a new entry in the ZIP archive using the file's content.
     * 
     * @param sourceFileFilename
     *            full file name of the source file to be put into the archive
     * @param entryName
     *            entry name the file will have in the ZIP archive.
     * @return the receiving ZipArchive instance
     * 
     * @throws ArchivingException
     *             if an error occurs during adding.
     * @throws Error
     *             if one of the parameters is provided as null or empty String.
     */
    public ZipArchive addFile( final String sourceFileFilename, final String entryName ) throws ArchivingException {
        U.assertNotEmpty( sourceFileFilename, "sourceFileName must not be null or empty string!" );
        U.assertNotEmpty( entryName, "entryName must not be null or empty string!" );
        try {
            File sourceFile = new File( sourceFileFilename );
            final FileInputStream fileInput = new FileInputStream( sourceFile );
            addFromStream( fileInput, entryName, sourceFile.lastModified() );
        } catch ( FileNotFoundException notfound ) {
            final String message = "Error when creating a new Entry from file!";
            LOG.error( message, notfound );
            throw new ArchivingException( message, notfound );
        }
        return this;
    }

    /**
     * Creates a new entry in the ZIP archive using the content provided by the InputStream.
     * 
     * @param source
     *            an Input stream providing the content to be put into the archive
     * @param entryName
     *            entry name the file will have in the ZIP archive.
     * @return the receiving ZipArchive instance
     * 
     * @throws ArchivingException
     *             if an error occurs during adding.
     * @throws Error
     *             if one of the parameters is provided as null or empty String.
     */
    public ZipArchive addFromStream( final InputStream source, final String entryName ) throws ArchivingException {
        return addFromStream( source, entryName, System.currentTimeMillis() );
    }

    /**
     * Creates a new entry in the ZIP archive using the content provided by the InputStream.
     * 
     * @param source
     *            an Input stream providing the content to be put into the archive
     * @param entryName
     *            entry name the file will have in the ZIP archive.
     * @param time
     *            the modification date to set for the entry
     * @return the receiving ZipArchive instance
     * 
     * @throws ArchivingException
     *             if an error occurs during adding.
     * @throws Error
     *             if one of the parameters is provided as null or empty String.
     */
    public ZipArchive addFromStream( final InputStream source, final String entryName, long time ) throws ArchivingException {
        U.assertNotNull( source, "Input stream must not be null!" );
        U.assertNotEmpty( entryName, "entryName must not be null or empty string!" );
        if ( !isOpen() ) {
            openTemporaryZipArchive();
        }
        createEntryFromInputStream( source, entryName, time );
        registerEntry( entryName );
        return this;
    }

    private void registerEntry( final String entryName ) {
        if ( entriesCreated == null ) {
            entriesCreated = new HashMap();
        }
        entriesCreated.put( entryName, Boolean.TRUE );
    }

    private boolean hasEntry( final ZipEntry entry ) {
        return hasEntry( entry.getName() );
    }

    private boolean hasEntry( final String entryName ) {
        return (entriesCreated == null) ? false : entriesCreated.containsKey( entryName );
    }

    private void openTemporaryZipArchive() throws ArchivingException {
        try {
            this.zipper = new ZipOutputStream( new FileOutputStream( temporaryFile ) );
        } catch ( FileNotFoundException fileNotFound ) {
            final String message = "Error when opening Zip Archive on " + getTemporaryFileName();
            LOG.error( message, fileNotFound );
            throw new ArchivingException( message, fileNotFound );
        }
        markOpened();
    }

    private void createEntryFromInputStream( final InputStream source, final String entryName, long entrysTimestamp )
            throws ArchivingException {
        final ZipEntry zipEntry = new ZipEntry( entryName );
        
        try {
            zipEntry.setTime( entrysTimestamp );
            zipEntry.setComment( "Created by ZipArchive" );
            createEntry( zipEntry, source );
        } catch ( IOException ioException ) {
            final String message = "Error when creating a new Entry from file!";
            LOG.error( message, ioException );
            throw new ArchivingException( message, ioException );
        }
    }

    private void createEntry( ZipEntry zipEntry, InputStream contentSource ) throws IOException {
        try {
            this.zipper.putNextEntry( zipEntry );
            IOUtility.copyFromTo( contentSource, this.zipper );
        } finally {
            this.zipper.closeEntry();
            contentSource.close();
        }
    }

    private String getTemporaryFileName() {
        return this.temporaryFile.getName();
    }

    public String getFileName() {
        return this.fileName;
    }

    public boolean isAppending() {
        return this.appending;
    }

    private void markOpened() {
        this.open = true;
    }

    private void markClosed() {
        this.open = false;
    }

    public boolean isOpen() {
        return this.open;
    }

    public class ArchivingException extends Exception {
        private static final long serialVersionUID = -1831777826155115964L;

        protected ArchivingException( final Throwable cause ) {
            super( cause );
        }

        protected ArchivingException( final String message, final Throwable cause ) {
            super( message, cause );
        }
    }

}