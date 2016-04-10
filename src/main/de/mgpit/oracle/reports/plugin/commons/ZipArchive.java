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

import de.mgpit.oracle.reports.plugin.destination.zip.ZipDestination;
import oracle.reports.utility.Utility;

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
 * @see ZipDestination
 */
public class ZipArchive {

    private static Logger LOG = Logger.getLogger( ZipArchive.class );

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
     * @throws AssertionError
     *             if fileName is provided as null or empty String
     */
    public static ZipArchive newNamed( final String fileName ) {
        if ( U.isEmpty( fileName ) ) {
            throw new Error( new NullPointerException( "fileName must not be null or empty string!" ));
        }
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
     * @throws AssertionError
     *             if fileName is provided as null or empty String
     */
    public static ZipArchive newOrExistingNamed( final String fileName ) {
        if ( U.isEmpty( fileName ) ) {
            throw new Error( new NullPointerException( "fileName must not be null or empty string!" ));
        }
        ZipArchive archive = new ZipArchive( fileName ).forAppending();
        return archive;
    }

    private ZipArchive forAppending() {
        this.appending = true;
        return this;
    }

    private ZipArchive() {
        // NOP
    }

    private ZipArchive( String fileName ) {
        this.fileName = fileName;
        createTemporaryFileFromName( fileName );
    }

    private ZipArchive createTemporaryFileFromName( String fileName ) {
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
     * @throws ArchivingException on any error during copying or renaming
     */
    public ZipArchive close() throws ArchivingException {
        if ( !this.isOpen() ) {
            return this;
        }

        ZipOutputStream zipper = getZipper();
        try {
            File destinationFile = new File( getFileName() );
            if ( destinationFile.exists() ) {
                if ( isAppending() ) {
                    copyExistingEntries( destinationFile, zipper );
                }
                destinationFile.delete();
            }
            zipper.close();
            markClosed();
            boolean successful = temporaryFile.renameTo( destinationFile );
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
    private void copyExistingEntries( File existingZipFile, ZipOutputStream zipper ) throws IOException {
        LOG.info( "About to copy entries from existing ZIP archive ..." );
        ZipFile zipFile = new ZipFile( existingZipFile );
        Enumeration entries = zipFile.entries();
        while ( entries.hasMoreElements() ) {
            ZipEntry anEntry = (ZipEntry) entries.nextElement();
            if ( !hasEntry( anEntry ) ) {
                LOG.debug( " *** Copying entry " + anEntry + " from existing archive ..." );
                if ( !anEntry.isDirectory() ) {
                    InputStream zipped = zipFile.getInputStream( anEntry );
                    createEntry( zipper, anEntry, zipped );
                } else {
                    zipper.closeEntry();
                }
            } else {
                LOG.debug( " *** Skipping entry " + anEntry + ". Newer version exists." );
            }
        }
        zipFile.close();
    }

    /**
     * Add a file to the ZIP archive.
     * 
     * @param sourceFileName
     *            full file name of the source file to be put into the archive
     * @param entryName
     *            entry name the file will have in the ZIP archive. If provided {@code null} the
     * @return the receiving ZipArchive instance
     * 
     * @throws ArchivingException
     *             if an error occurs during adding.
     * @throws AssertionError
     *             if one of the parameters is provided as null or empty String.
     */
    public ZipArchive addFile( String sourceFileName, String entryName ) throws ArchivingException {
        if ( U.isEmpty( sourceFileName ) ) {
            throw new Error( new NullPointerException( "sourceFileName must not be null or empty string!" ));
        }
        if ( U.isEmpty( entryName ) ) {
            throw new Error( new NullPointerException( "entryName must not be null or empty string!" ));
        }
        if ( !isOpen() ) {
            openTemporaryZipArchive();
        }
        createEntryFromFile( sourceFileName, entryName );
        registerEntry( sourceFileName, entryName );
        return this;
    }

    private void registerEntry( String sourceFileName, String entryName ) {
        if ( entriesCreated == null ) {
            entriesCreated = new HashMap();
        }
        entriesCreated.put( entryName, sourceFileName );
    }

    private boolean hasEntry( ZipEntry entry ) {
        return hasEntry( entry.getName() );
    }

    private boolean hasEntry( String entryName ) {
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

    /**
     * Create a new ZIP file entry including file content.
     * 
     * @param sourceFileName
     *            full file name of the source file to be put into the archive
     * @param entryName
     *            entry name the file will have in the ZIP archive
     * @throws ArchivingException
     */
    private void createEntryFromFile( final String sourceFilename, final String entryName ) throws ArchivingException {
        ZipEntry zipEntry = new ZipEntry( entryName );
        ZipOutputStream zipper = getZipper();

        try {
            File sourceFile = new File( sourceFilename );
            zipEntry.setTime( sourceFile.lastModified() );
            zipEntry.setComment( "Created by ZipArchive" );
            FileInputStream fileInput = new FileInputStream( sourceFile );
            createEntry( zipper, zipEntry, fileInput );
        } catch ( IOException ioException ) {
            final String message = "Error when creating a new Entry from file!";
            LOG.error( message, ioException );
            throw new ArchivingException( message, ioException );
        }
    }

    private void createEntry( ZipOutputStream zipper, ZipEntry zipEntry, InputStream contentSource ) throws IOException {
        try {
            zipper.putNextEntry( zipEntry );
            IOUtility.copyFromTo( contentSource, zipper );
        } finally {
            zipper.closeEntry();
            contentSource.close();
        }
    }

    private ZipOutputStream getZipper() {
        return this.zipper;
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

        protected ArchivingException( Throwable cause ) {
            super( cause );
        }

        protected ArchivingException( String message, Throwable cause ) {
            super( message, cause );
        }
    }

}