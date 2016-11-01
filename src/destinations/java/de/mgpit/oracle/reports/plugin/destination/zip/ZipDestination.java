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
package de.mgpit.oracle.reports.plugin.destination.zip;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;

import org.apache.log4j.Logger;

import de.mgpit.oracle.reports.plugin.commons.U;
import de.mgpit.oracle.reports.plugin.commons.URIUtility;
import de.mgpit.oracle.reports.plugin.commons.ZipArchive;
import de.mgpit.oracle.reports.plugin.commons.ZipArchive.ArchivingException;
import de.mgpit.oracle.reports.plugin.commons.io.IOUtility;
import de.mgpit.oracle.reports.plugin.destination.MgpDestination;
import de.mgpit.types.Entryname;
import de.mgpit.types.Filename;
import de.mgpit.types.StringCodedBoolean;
import oracle.reports.RWException;
import oracle.reports.utility.Utility;

/**
 * 
 * Implements an Oracle&reg; Reports {@link oracle.reports.server.Destination} for distributing
 * the files of a distribution process to a ZIP file. The destination is able to append files to an existing
 * ZIP file with some limitations with respect to duplicates - see below.
 * 
 * <p>
 * <strong>Registering the destination in your <em>reportserver.conf</em>:</strong>
 * <p>
 * The {@code DESTYPE} for this destination is suggested to be {@code zip}.
 * Full configuration example:
 * 
 * <pre>
 * {@code
 *  <destination destype="zip" class="de.mgpit.oracle.reports.plugin.destination.zip.ZipDestination">
 *     <property name="loglevel" value="DEBUG"/> <!-- log4j message levels. Allow debug messages --> 
 *     <property name="logfile"  value="/tmp/log/zipdestination.log"/> <!-- file to send the messages to -->
 *  </destination>
 *         }
 * </pre>
 *
 * <p>
 * <strong>Using the destination:</strong>
 * <p>
 * There are two ways to specify the zip file used as a distribution target.
 * <ul>
 * <li>passing separate parameters for file name and append mode</li>
 * <li>passing an URI as {@code DESNAME}</li>
 * </ul>
 * 
 * <p>
 * <strong>1) Using separate parameters</strong>
 * <p>
 * The full name ( path and file name ) must be provided as parameter {@code zipfilename}.
 * <ul>
 * <li>{@code zipfilename=}<em>full path and name to ZIP file</em></li>
 * </ul>
 * <p>
 * If you want to have several distributions put to the same ZIP file you add the parameter {@code append}.
 * <ul>
 * <li>{@code append=}[true|TRUE|yes|YES|on|ON|1]</li>
 * </ul>
 * When using parameters the entry name will be calculated from {@code DESNAME}
 * 
 * <p>
 * <strong>2) Using an URI</strong>
 * <p>
 * When using the URI approach you specify the ZIP file in the {@code DESNAME}
 * <ul>
 * <li>{@code zip:///}<em>full path and name to ZIP file</em>{@code ?append=}[true|TRUE|yes|YES|on|ON|1]{@code &entry=}<em>filename</em>
 * </ul>
 * <strong>note: </strong>scheme must be <strong>zip</strong> (lowercase!)
 * <p>
 * When running from <strong>Oracle&reg; Forms</strong>&trade; using a <em>REPORT_OBJECT</em> you can pass the parameters as follows.
 * <ul>
 * <li>{@code SET_REPORT_OBJECT_PROPERTY( }REPORT_OBJECT</em>
 * {@code , REPORT_DESTYPE, CACHE }<sup>1)</sup> {@code );}</li>
 * <li>Parameter passsing style:
 * <ul>
 * <li>{@code SET_REPORT_OBJECT_PROPERTY( }<em>REPORT_OBJECT</em>
 * {@code , REPORT_DESNAME, 'foo.pdf' );}
 * </li>
 * <li>{@code SET_REPORT_OBJECT_PROPERTY( }<em>REPORT_OBJECT</em>
 * {@code , REPORT_OTHER, 'DESTYPE=ZIP' ); -- Cannot be set via PARAM_LIST}
 * </li>
 * <li>other parameters via Parameter List
 * 
 * <pre>
 * {@code 
 * distribution_parameters := CREATE_PARAMETER_LIST( 'SOME_UNIQUE_NAME' );  
 * -- NOT ALLOWED: ADD_PARAMETER( distribution_parameters, 'DESTYPE'    , TEXT_PARAMETER, 'zip' );
 * ADD_PARAMETER( distribution_parameters, 'ZIPFILENAME', TEXT_PARAMETER, 'bar.zip' );
 * -- if you want to append to an existing ZIP file create the following parameter
 * -- ADD_PARAMETER( distribution_parameters, 'APPEND', TEXT_PARAMETER, 'true' );
 * --
 * -- Pass the Parameter List on RUN_REPORT_OBJECT
 * --
 * RUN_REPORT_OBJECT( REPORT_OBJECT, distribution_parameters ); }
 * </pre>
 * 
 * </li>
 * </ul>
 * </li>
 * <li>URI passing style:
 * <ul>
 * <li>{@code SET_REPORT_OBJECT_PROPERTY( }<em>REPORT_OBJECT</em>
 * {@code , REPORT_DESNAME, 'zip:///home/oracle/reports/bar.zip?append=false&entry=foo.pdf' );}
 * </li>
 * <li>{@code SET_REPORT_OBJECT_PROPERTY( }<em>REPORT_OBJECT</em>
 * {@code , REPORT_OTHER, 'DESTYPE=ZIP' );}
 * </li>
 * </ul>
 * </li>
 * </ul>
 * <small>1) Any valid DESTYPE. {@code NULL} or {@code ''} not allowed.</small>
 * <p>
 * <strong>Limitations</strong>
 * <p>
 * The ZIP destination has the following limitations:
 * <ul>
 * <li>Entry names during one distribution cycle must be unique - the receiving ZipArchive handles duplicates only partially.</li>
 * <li>On re-distributing to the same ZIP archive ({@code APPEND=TRUE}) duplicates will be detected, i.e. previously added
 * entries with the same name will be replaced by the content of the latest distribution.</li>
 * <p>
 * Depending on the {@code DESFORMAT} this will work only partially, though. For example re-running a distribution with
 * {@code DESFORMAT=html} and a constant {@code DESNAME=}<em>some name</em> will include a new version for every non main
 * file (e.g. image) as their name is constructed of <em>some name</em> and a generated numeric suffix.
 * </ul>
 * 
 * @see ZipArchive
 * @see MgpDestination
 *
 * @author mgp
 */
public final class ZipDestination extends MgpDestination {

    private static final Logger LOG = Logger.getLogger( ZipDestination.class );

    private ZipArchive zipArchive;
    private URI uri;
    // private String zipEntryName;
    // private boolean inAppendingMode;

    /**
     * Stop the distribution cycle.
     */
    protected void stop() throws RWException {
        try {
            this.zipArchive.close();
            super.stop();
        } catch ( Exception any ) {
            getLogger().error( "Error during finishing distribution!", any );
            throw Utility.newRWException( any );
        }
        getLogger().info( "Finished distribution to " + U.w( getZipArchiveFileName() ) );
    }

    /**
     * Send the main file to the destination
     * 
     * @param cacheFile
     *            full file name of the cache file to be distributed
     * @param fileFormat
     *            file format code
     * @throws RWException
     */
    protected void sendMainFile( final Filename cacheFile, final short fileFormat ) throws RWException {
        Filename destinationFile = Filename.of( getDesname() );
        Entryname entryName = Entryname.of( destinationFile );
        getLogger().info( "MAIN file " + U.w( cacheFile ) + " of format " + humanReadable( fileFormat ) + " will be put as "
                + U.w( entryName ) + " to " + U.w( getZipArchiveFileName() ) );
        try {
            addFileToArchiveWithName( cacheFile, entryName );
        } catch ( Throwable anyOther ) {
            getLogger().fatal( "Fatal Error during sending main file " + U.w( cacheFile ) + "!", anyOther );
            throw Utility.newRWException( new Exception( anyOther ) );
        }
    }

    /**
     * Send any subordinate file to the destination
     * 
     * @param cacheFile
     *            full file name of the cache file to be distributed
     * @param fileFormat
     *            file format code
     * @throws RWException
     */
    protected void sendAdditionalFile( final Filename cacheFile, final short fileFormat ) throws RWException {
        Entryname entryName = Entryname.of( cacheFile );
        getLogger().info( "Other file " + U.w( cacheFile ) + " of format " + humanReadable( fileFormat ) + " will be put as "
                + U.w( entryName ) + " to " + U.w( getZipArchiveFileName() ) );
        try {
            addFileToArchiveWithName( cacheFile, entryName );
        } catch ( Throwable anyOther ) {
            getLogger().fatal( "Fatal Error during sending additional file " + U.w( cacheFile ) + "!", anyOther );
            throw Utility.newRWException( new Exception( anyOther ) );
        }
    }

    /**
     * Put a file to the ZIP archive with given Entry name
     * 
     * @param sourcefilename
     *            name of the source file (i.e. cache file) to be put into the
     *            archive
     * @param entryName
     *            name for the source in the ZIP file
     * @throws ArchivingException
     */
    protected void addFileToArchiveWithName( final Filename sourcefilename, final Entryname entryname ) throws RWException {
        try {
            File sourceFile = IOUtility.fileFromName( sourcefilename );
            InputStream in = IOUtility.inputStreamFromFile( sourceFile );
            this.zipArchive.addFromStream( in, entryname, sourceFile.lastModified() );
        } catch ( FileNotFoundException fileNotFound ) {
            getLogger().error( "Error during distribution! Could not find file to add!" );
            throw Utility.newRWException( fileNotFound );
        } catch ( ArchivingException archivingException ) {
            getLogger().error( "Error during distribution! Could not archive file!", archivingException );
            throw Utility.newRWException( archivingException );
        }
    }

    /**
     * Starts a new distribution cycle for a report to a {@link ZipArchive}.
     * 
     * @param allProperties
     *            parameters for this distribution passed as {@code Properties}. Must include {@code desname} containing
     *            a zip-URI, or explicit {@code zipfilename}, {@code desname}, and optionally {@code append}. See usage notes of this class.
     *            <br>
     *            <em>Remark:</em> For Oracle&reg; Forms this will include the parameters set via {@code SET_REPORT_OBJECT_PROPERTY}
     *            plus the parameters passed via a {@code ParamList}
     * @param targetName
     *            target name of the distribution
     * @param totalNumberOfFiles
     *            total number of files being distributed
     * @param totalFileSize
     *            total file size of all files being distributed
     * @param mainFormat
     *            the output format of the main file being distributed
     * 
     * @throws RWException
     *             if there is a failure during distribution setup. The RWException normally will wrap the original Exception.
     */
    protected boolean start( final Properties allProperties, final String targetName, final int totalNumberOfFiles,
            final long totalFileSize, short mainFormat ) throws RWException {

        try {
            boolean continueToSend = super.start( allProperties, targetName, totalNumberOfFiles, totalFileSize, mainFormat );

            if ( continueToSend ) {

                continueToSend = setURI( allProperties, targetName );
                getLogger().info( "Distribution URI found to be " + this.uri.toString() );

                if ( continueToSend ) {
                    /*
                     * Could use the URI's path as the File class handles non-platform paths.
                     * For debugging purposes it's more convenient to be able to simply copy and paste the
                     * file name from the logging message, though.
                     */
                    Filename zipArchiveFileName = URIUtility.pathToPlatformFilename( this.uri );
                    Properties queryParameters = URIUtility.queryStringAsProperties( this.uri );

                    boolean inAppendingMode = false;
                    if ( queryParameters != null ) {
                        String desname = queryParameters.getProperty( "entry" );
                        setDesname( desname );

                        String appendParameterValue = allProperties.getProperty( "append", "FALSE" );
                        inAppendingMode = StringCodedBoolean.valueOf( appendParameterValue );
                    }
                    createZipArchive( zipArchiveFileName, inAppendingMode );
                    getLogger().info(
                            "Starting distribution to " + U.w( zipArchiveFileName ) + ". " + modeMessage( inAppendingMode ) );
                } else {
                    getLogger().warn( "Cannot continue to send ..." );
                }
            } else {
                getLogger().warn( "Cannot continue to send ..." );
            }
            return continueToSend;
        } catch ( RWException forLogging ) {
            getLogger().error( "Error during preparation of distribution!", forLogging );
            throw forLogging;
        }
    }

    /**
     * Create a new or use an eventually existing ZIP archive. An existing
     * archive will only be reused if called in appending mode, i.e. the APPEND
     * parameter is set to true or yes.
     * 
     * @param zipArchiveFileName
     *            full file name of the ZIP file to be created / updated.
     */
    private void createZipArchive( final Filename zipArchiveFileName, final boolean inAppendingMode ) {
        if ( inAppendingMode ) {
            this.zipArchive = ZipArchive.newOrExistingNamed( zipArchiveFileName );
        } else {
            this.zipArchive = ZipArchive.newNamed( zipArchiveFileName );
        }
    }

    /**
     * Extract the ZIP archive's name from the call parameters.
     * The name must be provides as
     * <ul>
     * <li>{@code ZIPFILENAME=...}or</li>
     * <li>{@code zipfilename=...}</li>
     * </ul>
     * If no such parameter is provided the ZIP archive's name will be derived from the {@link targetName}</em>
     * 
     * @param allProperties
     *            all properties (system and user parameters) passed to the
     *            report
     * @param targetName
     *            target name of the distribution. Will be used as default name
     *            for the ZIP archive if there is no ZIPFILENAME property in
     *            allProperties
     * @return
     */
    private static Filename getZipArchiveNameFromCallParameters( final Properties allProperties, final String targetName ) {
        Filename zipArchiveNameProvided = Filename.of( allProperties.getProperty( "zipfilename", targetName ) );
        Filename calculatedZipArchiveName = IOUtility.withZipExtension( zipArchiveNameProvided );
        return calculatedZipArchiveName;
    }

    /**
     * Extract the APPEND mode from the call parameters.
     * 
     * @param allProperties
     *            all properties (system and user parameters) passed to the
     *            report
     * @return
     */
    private static boolean getAppendFlagFromCallParameters( final Properties allProperties ) {
        String appendParameterValue = allProperties.getProperty( "append", "FALSE" );
        return StringCodedBoolean.valueOf( appendParameterValue );
    }

    private boolean setURI( final Properties allProperties, final String targetName ) {
        URI tmpURI = null;
        boolean OK = true;
        if ( !isEmpty( targetName ) && targetName.startsWith( "zip:" ) ) {
            try {
                tmpURI = new URI( targetName );
            } catch ( URISyntaxException syntax ) {
                getLogger().error( "URI provided for DESNAME is not a valid URI! " + U.w( targetName ), syntax );
                OK = false;
            }
        }

        if ( OK ) {
            if ( tmpURI == null ) {
                final Filename zipFileFilename = getZipArchiveNameFromCallParameters( allProperties, targetName );
                final boolean inAppendingMode = getAppendFlagFromCallParameters( allProperties );
                try {
                    final Filename entryName = IOUtility.fileNameOnly( Filename.of( targetName ) );
                    final String scheme = "zip";
                    final String authority = "";
                    final String path = URIUtility.toUriPathString( zipFileFilename );
                    final String query = "entry=" + entryName + "&append=" + ((inAppendingMode) ? "true" : "false");
                    final String fragment = null;

                    tmpURI = new URI( scheme, authority, path, query, fragment );

                } catch ( URISyntaxException syntax ) {
                    getLogger().error( "Could not construct URI", syntax );
                    OK = false;
                } catch ( IOException io ) {
                    getLogger().error( "Could not construct URI", io );
                    OK = false;
                }
            }
        }

        this.uri = tmpURI;
        return OK;
    }

    private static String modeMessage( boolean inAppendingMode ) {
        return inAppendingMode ? "New entries will be appended to ZIP file." : "New ZIP file will be created.";
    }

    /**
     * Initializes the destination on Report Server startup.
     * <p>
     * Invoked by the Report Server.
     * <ul>
     * <li>initialize log4j</li>
     * </ul>
     * 
     * @param destinationsProperties
     *            the properties set in the report server's conf file within this
     *            destination's configuration section ({@code //destination/property})
     * @throws RWException
     */
    public static void init( final Properties destinationsProperties ) throws RWException {
        MgpDestination.init( destinationsProperties );
        initLogging( destinationsProperties, ZipDestination.class );
        dumpProperties( destinationsProperties, LOG );
        LOG.info( "Destination " + U.w( ZipDestination.class.getName() ) + " started." );
    }

    public static void shutdown() {
        MgpDestination.shutdown();
        LOG.info( "Destination " + U.w( ZipDestination.class.getName() ) + " shut down." );
    }

    private Filename getZipArchiveFileName() {
        return this.zipArchive.getFileName();
    }

    protected Logger getLogger() {
        return LOG;
    }

}