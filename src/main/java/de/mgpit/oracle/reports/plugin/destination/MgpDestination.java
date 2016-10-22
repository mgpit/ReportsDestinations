package de.mgpit.oracle.reports.plugin.destination;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import org.apache.log4j.Logger;

import de.mgpit.oracle.reports.plugin.commons.DestinationsLogging;
import de.mgpit.oracle.reports.plugin.commons.U;
import de.mgpit.oracle.reports.plugin.commons.io.IOUtility;
import oracle.reports.RWException;
import oracle.reports.plugin.destination.ftp.DesFTP;
import oracle.reports.server.Destination;
import oracle.reports.utility.Utility;

/**
 * 
 * @author mgp
 *         <p>
 *         Abstract super class for {@code de.mgpit.oracle.reports.destination.*}-Report Destinations.
 *         <p>
 *         Provides logging setup during {@link #init(Properties)} of the destination and some handy utility methods.
 *         <p>
 *         {@code de.mgpit.oracle.reports.destination.*}-Report Destinations log to a separate file. You can set the log level
 *         via the {@code loglevel} property and the log file's name via the {@code logfile} property of the destination plugin
 *         configruation in the report server's conf file. If no specific file name is provided the name defaults to
 *         {@code destination.log} and can be found in the report server's log directory (or the temp directory if the server does
 *         not have a log directory).
 *         <p>
 *         The static part of a Destination holds the information configured in the {@code <servername>.cfg} file via
 *         {@code //destination/property} elements.
 *         <p>
 *         The instance part holds the information needed for the current distribution. Assumption is that for each distribution
 *         a new instance of the Destination will be created by the Oracle Reports&trade; server and that there is no caching or
 *         destination pooling.
 *         <p>
 *         A distribution cylcle consists of the sequence {@code start} &rarr; {@code sendFile}<sup>{1..n}</sup> &rarr; {@code stop()}.
 *         The number of {@code sendFile}s depends on the reports output format.
 *         A {@code PDF} for example will produce one file whereas a {@code HTML} output will produce a main file containing the HTML and
 *         several additional files for e.g. each image embedded in the current report.
 * 
 */
public abstract class MgpDestination extends Destination {

    protected abstract Logger getLogger();

    private int numberOfFilesInDistribution = -1;
    private int indexOfCurrentlyDistributedFile = -1;

    /**
     * Translates the numeric code for the file format into a human readable text like {@code application/pdf}
     * 
     * @param formatCode
     *            numeric format code
     * @return the numerically coded {@code format} in human readable format
     *         as "[<em>&lt;String&gt;</em>|<em>&lt;Mime String&gt;</em>]"
     */
    public static final String humanReadable( short formatCode ) {
        return U.w( getFileFormatAsString( formatCode ) + "|" + getFileFormatAsMimeType( formatCode ) );
    }

    /**
     * Gets the string representation for the format code given.
     * 
     * @param formatCode
     *            numeric format code
     * @return the numerically coded {@code format} in human readable format
     *         as "&lt;String&gt;</em>"
     */
    public static final String getFileFormatAsString( final short formatCode ) {
        return Utility.format2String( formatCode );
    }

    /**
     * Gets the mime type string representation for the format code given.
     * @param formatCode
     *            numeric format code
     * @return the numerically coded {@code format} in human readable format
     *         as "<em>&lt;Mime String&gt;</em>"
     */
    public static final String getFileFormatAsMimeType( final short formatCode ) {
        return Utility.format2Mime( formatCode );
    }

    /**
     * @see U#isEmpty(String)
     * 
     */
    public static final boolean isEmpty( final String s ) {
        return U.isEmpty( s );
    }

    /**
     * Answer if the string given is a (syntactically correct) Destination URI.
     * <p>
     * <ul>
     * <li>Must be syntactically correckt</li>
     * <li>Must be <em>absolute</em></li>
     * <li>Must <strong>not</strong> be <em>opaque</em></li>
     * </ul>
     * 
     * @param str
     * @return <code>true</code> if the string given is a Destination URI, else <code>false</code>
     */
    public static final boolean isDestinationURI( final String given, final String scheme ) {
        if ( isEmpty( given ) ) {
            return false;
        }
        try {
            URI tmp = new URI( given );
            return !tmp.isOpaque() && tmp.isAbsolute() && !isEmpty( tmp.getScheme() ) && tmp.getScheme().equalsIgnoreCase( scheme );
        } catch ( URISyntaxException syntax ) {
            return false;
        }
    }

    /**
     * Starts a new distribution cycle for a report to this destination. Will
     * distribute one or many files depending on the output format specified for the report execution.
     * <p>
     * The existance of the {@link Destination#setProperties()} and {@link Destination#getProperties()} indicates that the {@code allProperties}
     * will already have been passed to this instance so passing them seems redundant. Class Destination also has an instance
     * level field named {@code mProps} set/read by these methods.
     * 
     * @param allProperties
     *            all properties (parameters) passed to the report.
     *            For Oracle Forms&trade; this will include the parameters set via <code>SET_REPORT_OBJECT_PROPERTY</code>
     *            plus the parameters passed via a <code>ParamList</code>
     * @param targetName
     *            target name of the distribution.
     * @param totalNumberOfFiles
     *            total number of files to be distributed.
     * @param totalFileSize
     *            total file size of all files distributed.
     * @param mainFormat
     *            the output format of the main file.
     */
    protected boolean start( final Properties allProperties, final String targetName, final int totalNumberOfFiles,
            final long totalFileSize, final short mainFormat ) throws RWException {
        getLogger().info( "Distributing to Destination " + getDestype() );
        getLogger().info(
                "Distribution target name is " + U.w( targetName ) + ". Main format is " + getFileFormatAsMimeType( mainFormat ) );
        getLogger().info( "Distribution will contain " + U.w( totalNumberOfFiles ) + " files with a total size of "
                + U.w( totalFileSize ) + " bytes." );

        this.numberOfFilesInDistribution = totalNumberOfFiles;
        this.indexOfCurrentlyDistributedFile = 0;

        if ( getLogger().isDebugEnabled() ) {
            dumpProperties( allProperties );
        }

        return true; // continue to send
    }

    /**
     * Dumps the properties given using this object's current logger.
     * 
     * @param allProperties
     *            the properties to dump
     */
    protected void dumpProperties( final Properties allProperties ) {
        dumpProperties( allProperties, getLogger() );
    }

    /**
     * Dumps the properties given using the specified logger.
     * 
     * @param givenProperties
     *            the properties to dump
     * @param logger
     *            the logger to use
     */
    static protected void dumpProperties( final Properties givenProperties, Logger logger ) {
        if ( logger.isDebugEnabled() ) {
            Set keys = new TreeSet(); // we want the keys sorted ...
            keys.addAll( givenProperties.keySet() );
            Iterator allKeys = keys.iterator();

            logger.debug( "Got the following properties ..." );
            while ( allKeys.hasNext() ) {
                String key = (String) allKeys.next();
                String givenValue = givenProperties.getProperty( key );

                String valueToPrint = filter( key, givenValue );
                StringBuffer sb = new StringBuffer();
                logger.debug( U.w( U.lpad( key, 20 ) ) + " -> " + U.w( U.lpad( valueToPrint.length(), 4 ) ) + U.w( valueToPrint ) );
            }
        }
    }

    protected static String filter( final String key, final String givenValue ) {
        if ( givenValue == null ) {
            return "<null>";
        }

        final String inspectedValue = U.decodeIfBase64( givenValue );

        if ( "userid".equalsIgnoreCase( key ) ) {
            return U.obfuscate( inspectedValue );
        }

        if ( "url".equalsIgnoreCase( key ) || "uri".equalsIgnoreCase( key ) ) {
            try {
                URI uri = new URI( inspectedValue );
                return uri.toString();
            } catch ( URISyntaxException syntax ) {
                return "<notparsable>. Got: " + inspectedValue;
            }
        }
        return inspectedValue;
    }

    /**
     * Sends a file from the cache to this destination.
     * <p>
     * A distribution forms a cylcle of
     * <br/>
     * {@code start} &rarr; {@code sendFile}<sup>{1..n}</sup> &rarr; {@code stop()}
     * 
     * @param isMainFile
     *            flag if the file to be distributed is the main file
     * @param cacheFileFilename
     *            full file name of the cache file to be distributed
     * @param fileFormat
     *            file format code of the file to be distributed
     * @param fileSize
     *            file size of the file to be distributed in bytes
     * 
     * 
     *            TODO: re-think if this should really be final?
     */
    protected final void sendFile( boolean isMainFile, String cacheFileFilename, short fileFormat, long fileSize )
            throws RWException {
        U.Rw.assertNotEmpty( cacheFileFilename );
        try {
            this.indexOfCurrentlyDistributedFile++;
            getLogger().info( "Sending file "
                    + U.w( U.lpad( indexOfCurrentlyDistributedFile, 2 ) + "/" + U.lpad( numberOfFilesInDistribution, 2 ) ) );
            if ( isMainFile ) {
                sendMainFile( IOUtility.asPlatformFilename( cacheFileFilename ), fileFormat );
            } else {
                sendAdditionalFile( IOUtility.asPlatformFilename( cacheFileFilename ), fileFormat );
            }
        } catch ( Exception any ) {
            getLogger().error( "Error during sending file " + U.w( cacheFileFilename ) + ".", any );
            RWException rwException = Utility.newRWException( any );
            throw rwException;
        } catch ( Throwable thrown ) {
            getLogger().fatal( "Fatal Error during sending file " + U.w( cacheFileFilename ) + ".", thrown );
            throw Utility.newRWException( new Exception( thrown ) );
        }
    }

    /**
     * Sends the main file from the distribution to the target.
     * 
     * @param cacheFileFilename
     * @param fileFormat
     * @throws RWException
     */
    protected abstract void sendMainFile( String cacheFileFilename, short fileFormat ) throws RWException;

    /**
     * Sends additional files from the distribution to the target.
     * 
     * @param cacheFileFilename
     * @param fileFormat
     * @throws RWException
     */
    protected abstract void sendAdditionalFile( String cacheFileFilename, short fileFormat ) throws RWException;

    /**
     * Opens an InputStream on the file given.
     * 
     * @param sourceFile
     * @return InputStream on the source file
     * 
     * @throws RWException
     */
    protected InputStream getContent( File sourceFile ) throws RWException {
        U.Rw.assertNotNull( sourceFile, "Source file must not be null!" );
        try {
            InputStream in = IOUtility.asFileInputStream( sourceFile );
            return in;
        } catch ( FileNotFoundException fileNotFound ) {
            getLogger().error( "Error on getting content for distribution.!", fileNotFound );
            throw Utility.newRWException( fileNotFound );
        }
    }

    /**
     * Stops the distribution cycle.
     */
    protected void stop() throws RWException {
        try {
            super.stop();
        } finally {
            this.indexOfCurrentlyDistributedFile = -1;
            this.numberOfFilesInDistribution = -1;
        }
    }

    /**
     * Initializes the destination on Report Server startup.
     * <p>
     * All destinations seem to be set up by the <code>main</code> thread of the reports server so we
     * should not expect multithreading issues ...
     * 
     * @param destinationsProperties
     *            the properties set in the report server's conf file within the
     *            destination's configuration section ({@code //destination/property})
     * @throws RWException
     */
    public static void init( final Properties destinationsProperties ) throws RWException {
        initLogging( destinationsProperties, MgpDestination.class );
        final Logger log = Logger.getRootLogger();
        log.info( "Destination logging successfully initialized with properties: " + destinationsProperties );
        try {
            Destination.init( destinationsProperties );
        } catch ( RWException rwException ) {
            log.error( "Error during initializing Destination.", rwException );
            throw rwException;
        }
    }

    /**
     * Initializes the logging of the destination based on the properties provided for the destination
     * in the report server's conf file.
     * Will override the corresponding settings of the {@code log4j.properties} distributed with the destination's JAR file.
     * <p>
     * 
     * @param destinationsProperties
     *            the properties set in the report server's conf file within the
     *            destination's configuration section ({@code //destination/property})
     */
    protected static void initLogging( final Properties destinationsProperties, Class clazz ) throws RWException {

        if ( destinationsProperties != null ) {
            final String logfileFilenameGiven = destinationsProperties.getProperty( "logfile" );
            final String loglevelLevelnameGiven = destinationsProperties.getProperty( "loglevel", "INFO" );

            DestinationsLogging.assertRootLoggerExists();
            DestinationsLogging.createOrReplacePackageLevelLogger( clazz, logfileFilenameGiven, loglevelLevelnameGiven );
        }
    }

    public static void shutdown() {
        Destination.shutdown();
    }

    /**
     * Answers if the given file fomat is binary.
     * <p>
     * For convenience, only. Delegates to {@link DesFTP#isBinaryFile(short)}
     * 
     * @param formatCode
     *            the magic format code of the file ...
     * @return true if the file format identified by <code>formatCode</code> is a binary format.
     */
    public static boolean isBinaryFile( short formatCode ) {
        return new DesFTP().isBinaryFile( formatCode );
    }
}