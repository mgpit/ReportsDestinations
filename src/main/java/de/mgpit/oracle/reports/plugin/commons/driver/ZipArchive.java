/*
 * Copyright 2016 Marco Pauls www.mgp-it.de
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

/**
 * @license APACHE-2.0
 */
package de.mgpit.oracle.reports.plugin.commons.driver;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import org.apache.log4j.Logger;

import de.mgpit.oracle.reports.plugin.commons.U;
import de.mgpit.oracle.reports.plugin.commons.io.IOUtility;
import de.mgpit.types.Entryname;
import de.mgpit.types.Filename;

/**
 * 
 * A flat ZIP file.
 * <p>
 * ZipArchive implements a flat ZIP file (i.e. no directory structure). The ZipArchive is used
 * by the ZipDestination for storing the files of a distribution process.
 * <p>
 * The archive can be opened in appending or overwriting mode by calling
 * one of the classes factory methods named {@link #newOrExistingNamed(String)} or {@link #newNamed(String)}.
 * <p>
 * New entries will be added by providing a file name and an entry name to {@link #addFile(String, String)}.
 * During one packing cylce (i.e. opening the ZipArchive, adding files, closing the ZipArchive) entry names must be unique
 * meaning each call {@link #addFile(String, String)} must provide a different entry name.
 * <p>
 * At the end clients of ZipArchive have to {@link #close()} the ZIP archive.
 * <p>
 * When appending to an existing archive the ZipArchive will be able to handle duplicate entries, meaning that
 * entries from the existing archive will be replaced by new entries with the same name.
 * <p>
 * Any errors during processing will be wrapped in a {@link ArchivingException}.
 * 
 * @author mgp
 */
public class ZipArchive {

    private static final Logger LOG = Logger.getLogger( ZipArchive.class );

    public static final String ZIP_SCHEME = "zip";

    private Filename fileName;
    private boolean appending = false;
    private boolean open = false;
    private ZipOutputStream zipper;
    private File temporaryFile;

    private Map entriesCreated;

    /**
     * Gets a new ZipArchive - factory method.
     * Will replace an existing ZIP archive with the same name.
     * 
     * @param fileName
     *            full file name of the ZIP archive to be created
     * @return a ZipArchive instance
     * 
     * @throws Error
     *             if fileName is provided as null or empty String
     */
    public static ZipArchive newNamed( final Filename fileName ) {
        U.assertNotEmpty( fileName, "fileName must not be null or empty string!" );
        ZipArchive archive = new ZipArchive( fileName );
        return archive;
    }

    /**
     * Gets a new ZipArchive in appending mode - factory method.
     * Will replace any existing ZIP archive with the same name.
     * A ZIP archive in appending mode will add new entries to the archive and/or replace existing entries.
     * 
     * @param fileName
     * @return a ZipArchive instance
     * 
     * @throws Error
     *             if fileName is provided as null or empty String
     */
    public static ZipArchive newOrExistingNamed( final Filename fileName ) {
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

    private ZipArchive( final Filename fileName ) {
        this.fileName = fileName;
        createTemporaryFileFromName( fileName );
    }

    /**
     * Creates a temporary ZipArchived with its name based on the file name given.
     * The archive is intended for temporary usage.
     * 
     * @param fileName
     * @return a new ZipArchive
     */
    private ZipArchive createTemporaryFileFromName( final Filename fileName ) {
        temporaryFile = IOUtility.fileFromName( fileName.concat( ".part" ) );
        return this;
    }

    /**
     * Closes the ZIP Archive.
     * <p>
     * <strong>Some notes:</strong>Until here the new entries/content have been written to a temporary file.
     * In Append mode the entries of an already existing file will now be copied to the temporary file.
     * Finally the temporary file will be renamed to the final file name as provided by the destination.
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
            final File destinationFile = IOUtility.fileFromName( getFileName() );
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
     * 
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
    public ZipArchive addFile( final Filename sourceFileFilename, final Entryname entryName ) throws ArchivingException {
        U.assertNotEmpty( sourceFileFilename, "sourceFileName must not be null or empty string!" );
        U.assertNotEmpty( entryName, "entryName must not be null or empty string!" );
        try {
            File sourceFile = IOUtility.fileFromName( sourceFileFilename );
            final FileInputStream fileInput = IOUtility.inputStreamFromFile( sourceFile );
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
    public ZipArchive addFromStream( final InputStream source, final Entryname entryName ) throws ArchivingException {
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
    public ZipArchive addFromStream( final InputStream source, final Entryname entryName, long time ) throws ArchivingException {
        U.assertNotNull( source, "Input stream must not be null!" );
        U.assertNotEmpty( entryName, "entryName must not be null or empty string!" );
        if ( !isOpen() ) {
            openTemporaryZipArchive();
        }
        createEntryFromInputStream( source, entryName, time );
        registerEntry( entryName );
        return this;
    }

    /**
     * Registers that this ZipArchive contains an entry with the name given
     * 
     * @param entryName
     *            entrie's name to register
     */
    private void registerEntry( final Entryname entryname ) {
        if ( entriesCreated == null ) {
            entriesCreated = new HashMap();
        }
        entriesCreated.put( entryname, Boolean.TRUE );
    }

    private boolean hasEntry( final ZipEntry entry ) {
        return hasEntry( Entryname.of( entry.getName() ) );
    }

    /**
     * Checks if this ZipArchive contains an entry with the name given
     * 
     * @param entryName
     *            entrie's name to check
     * @return {@code true} if this archive contains an entry with the name, else {@code false}
     */
    private boolean hasEntry( final Entryname entryName ) {
        return (entriesCreated == null) ? false : entriesCreated.containsKey( entryName );
    }

    /**
     * Opens a new temporary ZipArchive
     * 
     * @throws ArchivingException
     */
    private void openTemporaryZipArchive() throws ArchivingException {
        try {
            this.zipper = new ZipOutputStream( IOUtility.outputStreamFromFile( temporaryFile ) );
        } catch ( FileNotFoundException fileNotFound ) {
            final String message = "Error when opening Zip Archive on " + getTemporaryFileName();
            LOG.error( message, fileNotFound );
            throw new ArchivingException( message, fileNotFound );
        }
        markOpened();
    }

    /**
     * Creates a named entry from an input stream with the timestamp provided.
     * 
     * @param source
     *            input stream to add
     * @param entryName
     *            name of the new entry to create
     * @param entrysTimestamp
     *            timestamp to set for the new entry to create
     * @throws ArchivingException
     */
    private void createEntryFromInputStream( final InputStream source, final Entryname entryName, long entrysTimestamp )
            throws ArchivingException {
        final ZipEntry zipEntry = new ZipEntry( entryName.toString() );

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

    /**
     * Creates a new entry in this archive.
     * 
     * @param zipEntry
     *            the new entry
     * @param contentSource
     *            input stream on the source file
     * @throws IOException
     */
    private void createEntry( ZipEntry zipEntry, InputStream contentSource ) throws IOException {
        try {
            this.zipper.putNextEntry( zipEntry );
            IOUtility.copyFromTo( contentSource, this.zipper );
        } finally {
            this.zipper.closeEntry();
            contentSource.close();
        }
    }

    /**
     * Gets the temporary file name of this ZipArchive
     * 
     * @return string with the temporary file name of this archive
     */
    private String getTemporaryFileName() {
        return this.temporaryFile.getName();
    }

    /**
     * Gets the file name of this ZipArchive
     * 
     * @return string with the file name of this archive
     */
    public Filename getFileName() {
        return this.fileName;
    }

    /**
     * Gets if this ZipArchive is in appending mode.
     * 
     * @return {@code true} if this archive is in appending mode, else {@code false}
     */
    public boolean isAppending() {
        return this.appending;
    }

    /**
     * Marks this ZipArchive as openend.
     */
    private void markOpened() {
        this.open = true;
    }

    /**
     * Marks this ZipArchive as closed.
     */
    private void markClosed() {
        this.open = false;
    }

    /**
     * Gets if this ZipArchive is open
     * 
     * @return {@code true} if this archive is open, else {@code false}
     */
    public boolean isOpen() {
        return this.open;
    }

    /**
     * 
     * @author mgp
     * 
     *         Exception for wrapping other exceptions thrown during ZipArchive creation.
     *         So clients of ZipArchive have to deal with one type of checked exception, only.
     *
     */
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