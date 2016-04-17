package de.mgpit.oracle.reports.plugin.destination.zip;


import java.util.Properties;

import org.apache.log4j.Logger;

import de.mgpit.oracle.reports.plugin.commons.IOUtility;
import de.mgpit.oracle.reports.plugin.commons.StringCodedBoolean;
import de.mgpit.oracle.reports.plugin.commons.U;
import de.mgpit.oracle.reports.plugin.commons.ZipArchive;
import de.mgpit.oracle.reports.plugin.commons.ZipArchive.ArchivingException;
import de.mgpit.oracle.reports.plugin.destination.MgpDestination;
import oracle.reports.RWException;
import oracle.reports.server.Destination;
import oracle.reports.utility.Utility;

/**
 * 
 * @author mgp
 *         <p>
 *         Implements an Oracle Reports {@link oracle.reports.server.Destination} that puts
 *         the files of its distribution process into a ZIP file. The destination is able to distribute files to an existing
 *         ZIP file with some limitations. See below.
 * 
 *         <p>
 *         <strong>Registering the destination in your <em>reportserver.conf</em>:</strong>
 *         <p>
 *         The {@code DESTYPE} for this destination is suggested to be {@code ZIP}.
 *         Full configuration example:
 *          
 *         <pre> &lt;destination destype="ZIP" class="de.mgpit.oracle.reports.plugin.destination.zip.ZipDestination"&gt;
 *     &lt;property name="loglevel" value="DEBUG"/&gt; &lt;!-- log4j message levels. Allow debug messages --&gt; 
 *     &lt;property name="logfile"  value="/tmp/log/zipdestination.log"/&gt; &lt;!-- file to send the messages to --&gt;
 * &lt;/destination&gt;
 *         </pre>
 *
 *         <p>
 *         <strong>Using the destination:</strong>
 *         <p>
 *         The full name ( path and file name ) must be provided as parameter
 *         {@code ZIPFILENAME} or {@code zipfilename}.
 *         <ul>
 *         <li>{@code ZIPFILENAME=}&lt;<em>full path and name to ZIP file</em>&gt;</li>
 *         </ul>
 *         <p>
 *         If you want to have several distributions put to the same ZIP file you have
 *         to provide the parameter {@code APPEND} or {@code append}.
 *         <ul>
 *         <li>{@code APPEND=}[true|TRUE|yes|YES|on|ON|1]</li>
 *         </ul>
 *         <p>
 *         When running from <strong>Oracle Forms</strong> using the
 *         <em>REPORT_OBJECT</em> you can pass the parameters as follows.
 *         <ul>
 *         <li><code>SET_REPORT_OBJECT_PROPERTY( </code><em>REPORT_OBJECT</em>
 *         <code> , REPORT_DESTYPE, CACHE<sup>1)</sup> );</code></li>
 *         <li><code> SET_REPORT_OBJECT_PROPERTY( </code><em>REPORT_OBJECT</em>
 *         <code> , REPORT_OTHER, 'DESTYPE=ZIP ZIPFILENAME=&lt;</code><em>zip file name</em>&gt;<sup>2)</sup><code>' );</code>
 *         </li>
 *         <li>or, when appending {@code SET_REPORT_OBJECT_PROPERTY(}
 *         <em>REPORT_OBJECT</em>
 *         <code> , REPORT_OTHER, 'DESTYPE=ZIP ZIPFILENAME=</code>&lt;<em>zip file name</em>&gt;<sup>2)</sup><code> APPEND=TRUE' );</code>
 *         </li>
 *         </ul>
 *         <small>1) Any valid DESTYPE. {@code NULL} or {@code ''} not
 *         allowed.</small>
 *         <small>2) Standard rules for target file names do apply</small>
 *         <p>
 *         <strong>Limitations</strong>
 *         <p>
 *         The ZIP destination has the following limitations:
 *         <ul>
 *         <li>Entry names during one distribution cycle (wich starts with {@link #start(Properties, String, int, long, short)}
 *         and finishes with {@link #stop()}) must be unique - the receiving ZipArchive handles duplicates partially only.</li>
 *         <li>On re-distributing to the same ZIP archive ({@code APPEND=TRUE}) duplicates will be detected, i.e. previously added
 *         entries with the same name will be replaced by the content of the latest distribution.</li>
 *         <p>Depending on the {@code DESFORMAT} this will work only partially, though. For example re-running a distribution with
 *         {@code DESFORMAT=html} and a constant {@code DESNAME=}&lt;some name&gt; will include a new version for every non main
 *         file (e.g. image) as their name is constructed of &lt;some name&gt; and a generated numeric suffix. 
 *         </ul>
 * 
 * @see ZipArchive
 * @see MgpDestination
 * 
 */
public final class ZipDestination extends MgpDestination {

    private static Logger LOG = Logger.getLogger( ZipDestination.class );

    private ZipArchive zipArchive;
    private String zipEntryName;
    private boolean inAppendingMode;

    /**
     * Stop the distribution cycle.
     */
    protected void stop() throws RWException {
        try {
            getZipArchive().close();
            super.stop();
        } catch ( Exception any ) {
            LOG.error( "Error during finishing distribution. See following message(s)!" );
            LOG.error( any );
            RWException rwException = Utility.newRWException( any );
            throw rwException;
        }
        LOG.info( "Finished distribution to " + U.w( getZipArchiveFileName() ) );
    }

    /**
     * Send another file from the current distribution to the destination.
     * 
     * @param isMainFile
     *            flag if the file to be distributed is the main file
     * @param cacheFileName
     *            full file name of the cache file to be distributed
     * @param fileFormat
     *            file format code of the file to be distributed
     * @param fileSize
     *            file size of the file to be distributed
     * 
     */
    protected void sendFile( boolean isMainFile, String cacheFileName, short fileFormat, long fileSize ) throws RWException {
        try {
            if ( isMainFile ) {
                sendMainFile( cacheFileName, fileFormat );
            } else {
                sendOtherFile( cacheFileName, fileFormat );
            }
        } catch ( Exception any ) {
            LOG.error( "Error during sending file " + U.w( cacheFileName ) + ". See following message(s)!" );
            LOG.error( any );
            RWException rwException = Utility.newRWException( any );
            throw rwException;
        }
    }

    /**
     * Send the main file to the destination
     * 
     * @param cacheFileName
     *            full file name of the cache file to be distributed
     * @param fileFormat
     *            file format code
     * @throws ArchivingException
     */
    private void sendMainFile( String cacheFileName, short fileFormat ) throws ArchivingException {
        LOG.info( "Sending Main file named " + U.w( cacheFileName ) + " to " + U.w( getZipArchiveFileName() ) );
        LOG.info( "ZIP Entry will be of format " + humanReadable( fileFormat ) + " named " + U.w( getZipEntryName() ) );
        addFileToArchive( cacheFileName, getZipEntryName() );
    }

    /**
     * Send any subordinate file to the destination
     * 
     * @param cacheFileName
     *            full file name of the cache file to be distributed
     * @param fileFormat
     *            file format code
     * @throws ArchivingException
     */
    private void sendOtherFile( String cacheFileName, short fileFormat ) throws ArchivingException {
        String otherFileZipEntryName = Utility.fileNameOnly( cacheFileName );
        LOG.info( "Sending Other file named " + U.w( cacheFileName ) + " to " + U.w( getZipArchiveFileName() ) );
        LOG.info( "ZIP Entry will be of format " + humanReadable( fileFormat ) + " named " + U.w( otherFileZipEntryName ) );
        addFileToArchive( cacheFileName, otherFileZipEntryName );
    }

    /**
     * Put a file to the ZIP archive
     * 
     * @param sourceFileName
     *            name of the source file (i.e. cache file) to be put into the
     *            archive
     * @param entryName
     *            name for the source in the ZIP file
     * @throws ArchivingException
     */
    protected void addFileToArchive( String sourceFileName, String entryName ) throws ArchivingException {
        ZipArchive zipArchive = getZipArchive();
        zipArchive.addFile( sourceFileName, entryName );
    }

    /**
     * Start a new distribution cycle for a report to this destination. Will
     * distribute one or many files depending on the format.
     * 
     * @param allProperties
     *            all properties (system and user parameters) passed to the
     *            report
     * @param targetName
     *            target name of the distribution. Will be the entry name of the
     *            ZIP file.
     * @param totalNumberOfFiles
     *            total number of files to be distributed.
     * @param totalFileSize
     *            total file size of all files distributed.
     * @param mainFormat
     *            the output format of the main file.
     */
    protected boolean start( Properties allProperties, String targetName, int totalNumberOfFiles, long totalFileSize,
            short mainFormat ) throws RWException {

        boolean continueToSend = super.start( allProperties, targetName, totalNumberOfFiles, totalFileSize, mainFormat );

        if ( continueToSend ) {

            String zipArchiveFileName = getZipArchiveNameFromCallParameters( allProperties, targetName );
            setZipEntryName( Utility.fileNameOnly( targetName ) );
            setInAppendingMode( getAppendFlagFromCallParameters( allProperties ) );

            createZipArchive( zipArchiveFileName, isInAppendingMode() );

            LOG.info( "Starting distribution to " + U.w( zipArchiveFileName ) + ". " + modeMessage() );
            continueToSend = true;
        }
        return continueToSend;
    }

    /**
     * Create a new or use an eventually existing ZIP archive. An existing
     * archive will only be reused if called in appending mode, i.e. the APPEND
     * parameter is set to true or yes.
     * 
     * @param zipArchiveFileName
     *            full file name of the ZIP file to be created / updated.
     */
    private void createZipArchive( String zipArchiveFileName, boolean inAppendingMode ) {
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
    private String getZipArchiveNameFromCallParameters( Properties allProperties, String targetName ) {
        String zipArchiveNameProvided = allProperties.getProperty( "ZIPFILENAME",
                allProperties.getProperty( "zipfilename", targetName ) );
        String calculatedZipArchiveName = IOUtility.withZipExtension( zipArchiveNameProvided );
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
    private boolean getAppendFlagFromCallParameters( Properties allProperties ) {
        String appendParameterValue = allProperties.getProperty( "APPEND", allProperties.getProperty( "append", "FALSE" ) );
        return StringCodedBoolean.valueOf( appendParameterValue );
    }

    private String modeMessage() {
        return inAppendingMode ? "New entries will be appended to ZIP file." : "New ZIP file will be created.";
    }

    /**
     * Initialize the destination on Report Server startup. Will mainly
     * initialize log4j Logging.
     * 
     * @param destinationsProperties
     * @throws RWException
     */
    public static void init( Properties destinationsProperties ) throws RWException {
        MgpDestination.init( destinationsProperties );
        LOG.info( "Destination " + U.w( ZipDestination.class.getName() ) + " started." );
    }

    public static void shutdown() {
        MgpDestination.shutdown();
        LOG.info( "Destination " + U.w( ZipDestination.class.getName() ) + " shut down." );
    }

    private ZipArchive getZipArchive() {
        return this.zipArchive;
    }

    private void setInAppendingMode( boolean inAppendingMode ) {
        this.inAppendingMode = inAppendingMode;
    }

    public boolean isInAppendingMode() {
        return this.inAppendingMode;
    }

    private void setZipEntryName( String zipEntryName ) {
        this.zipEntryName = zipEntryName;
    }

    private String getZipEntryName() {
        return this.zipEntryName;
    }

    private String getZipArchiveFileName() {
        return this.zipArchive.getFileName();
    }

}