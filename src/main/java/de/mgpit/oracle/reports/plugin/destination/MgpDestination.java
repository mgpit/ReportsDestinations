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
 */
public abstract class MgpDestination extends Destination {

    protected abstract Logger getLogger();

    private int numberOfFilesInDistribution = -1;
    private int indexOfCurrentlyDistributedFile = -1;

    /**
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
     * 
     * @param formatCode
     *            numeric format code
     * @return the numerically coded {@code format} in human readable format
     *         as "<em>&lt;Mime String&gt;</em>"
     */
    public static final String getFileFormatAsMimeType( final short formatCode ) {
        return Utility.format2Mime( formatCode );
    }

    /**
     * 
     * @param s
     *            a String
     * @return {@code true} if the String s is null or them empty String "".
     */
    public static final boolean isEmpty( final String s ) {
        return U.isEmpty( s );
    }
    
    /**
     * Answer if the string given is a (syntactically correct) Destination URI.
     * <p>
     * <ul>
     *  <li>Must be syntactically correckt</li>
     *  <li>Must be <em>absolute</em></li>
     *  <li>Must <strong>not</strong> be <em>opaque</em></li>
     * </ul>
     * @param str
     * @return <code>true</code> if the string given is a Destination URI, else <code>false</code>
     */
    public static final boolean isDestinationURI( final String given, final String scheme ) {
        if ( isEmpty( given ) ) {
            return false;
        }
        try {
            URI tmp = new URI( given );
            return !tmp.isOpaque()
                   && tmp.isAbsolute()
                   && !isEmpty( tmp.getScheme() )
                   && tmp.getScheme().equalsIgnoreCase( scheme );
        } catch ( URISyntaxException syntax ) {
            return false;
        }
    }

    /**
     * Start a new distribution cycle for a report to this destination. Will
     * distribute one or many files depending on the format.
     * <p>
     * {@link Destination#setProperties()} and {@link Destination#getProperties()} indicate that the <code>allProperties</code>
     * will already have been passed to the instance so passing them seems redundant.
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

    protected void dumpProperties( final Properties allProperties ) {
        dumpProperties( allProperties, getLogger() );
    }

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
     * Send a file from the current distribution to the destination.
     * 
     * @param isMainFile
     *            flag if the file to be distributed is the main file
     * @param cacheFileFilename
     *            full file name of the cache file to be distributed
     * @param fileFormat
     *            file format code of the file to be distributed
     * @param fileSize
     *            file size of the file to be distributed
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
                sendMainFile( cacheFileFilename, fileFormat );
            } else {
                sendAdditionalFile( cacheFileFilename, fileFormat );
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
        U.Rw.assertNotNull(sourceFile, "Source file must not be null!" );
        try {
            InputStream in = IOUtility.asFileInputStream( sourceFile );
            return in;
        } catch ( FileNotFoundException fileNotFound ) {
            getLogger().error( "Error on getting content for distribution.!", fileNotFound );
            throw Utility.newRWException( fileNotFound );
        }
    }

    /**
     * Stop the distribution cycle.
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
     * Initialize the destination on Report Server startup.
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