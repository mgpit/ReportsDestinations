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
import de.mgpit.types.Filename;
import oracle.reports.RWException;
import oracle.reports.plugin.destination.ftp.DesFTP;
import oracle.reports.server.Destination;
import oracle.reports.utility.Utility;

/**
 * Oracle&reg; Reports Destination with separate logging.
 * <p>
 * Abstract super class for {@code de.mgpit.oracle.reports.destination.*}-Report Destinations.
 * <p style="color:FireBrick; font-size:110%">
 * See notes on inheriting from this class at end!
 * <p>
 * Provides logging setup during {@link #init(Properties)} of the destination and some handy utility methods.
 * <p>
 * All {@code de.mgpit.oracle.reports.destination.*} report destinations log to a separate file. You can set the log level
 * via the {@code loglevel} property and the log file's name via the {@code logfile} property of the destination plugin
 * configuration in the report server's configuration file. If no specific file name is provided the name defaults to
 * {@code destination.log} and can be found in the report server's log directory (or the temp directory if the server does
 * not have a log directory).
 * <p>
 * The static part of a Destination holds the information configured in the {@code <servername>.cfg} file via
 * {@code //destination/property} elements (see also notes at end).
 * <p>
 * The instance part holds the information needed for the current distribution. Assumption is that for each distribution
 * a new instance of the Destination will be created by the Oracle&reg; Reports server and that there is no caching or
 * destination pooling.
 * <p>
 * A distribution cycle consists of the sequence {@code start} &rarr; {@code sendFile}<sup>{1..n}</sup> &rarr; {@code stop()}.
 * The number of invocations of {@code sendFile}s depends on the reports output format.
 * A {@code PDF} for example will produce one file whereas a {@code HTML} output will produce a main file containing the HTML and
 * several additional files for e.g. each image embedded in the current report.
 * <p>
 * <strong style="color:Teal">!!! A note on subclassing / inheriting from this class!!!</strong>
 * <p>
 * The initialization process of a <em>Oracle&reg; Reports</em> is done in a static manner. Roughly outlined
 * <ul>
 * <li>the <em>Oracle&reg; Reports server</em> reads its {@code <reportservername>.conf} configuration file</li>
 * <li>delegates initialization by calling {@code Destination.initDest(ServerConfig)}
 * <li>the {@code Destination.initDest}
 * <ul>
 * <li>iterates over all destinations defined in the configuration file</li>
 * <li>extracts their properties from the {@code ServerConfig}</li>
 * <li>and finally invokes the <em>static</em> {@code init(Properties)} method</li>
 * </ul>
 * </li>
 * </ul>
 * So subclasses should call {@code MgpDestination.init()} (or {@code MySuperClass.init()}, resp.) explicitly
 * as part of their {@code init(Properties)} method.
 * <p>
 * <strong>Leaf classes</strong> in your hierarchy can call {@code MgpDestination.initLogging(Properties, Class)} explicitly as part of
 * their {@code init(Properties)} method if they want to establish their own log file.
 * <p>
 * <strong>Leaf classes</strong> may guard the initialization process as follows
 * 
 * <pre>
 * {@code
 *  final Logger log = Logger.getRootLogger();
 *  try {
 *     <em>MySuperclass</em>.init( destinationsProperties );
 *  } catch ( RWException rwException ) {
 *     log.fatal( "Error during delegation of initialization to <em>MySuperclass</em>.", rwException );
 *     throw rwException;
 *  } catch ( Throwable somethingUnexpected ) {
 *     log.fatal( "Fatal error during delegation of initialization to <em>MySuperclass</em>!" );
 *     throw Utility.newRWException( new Exception( "Fatal error during initializing <em>MyDestinationClass</em>" ) );
 *  }
 * ...
 * }
 * </pre>
 * 
 *
 * @see DestinationsLogging
 * 
 * @author mgp
 */
public abstract class MgpDestination extends Destination {

    protected static final String DEFAULT_LOG_LEVEL = "INFO";

    protected abstract Logger getLogger();

    protected static final int SOME_PRIME = 23;
    protected static final boolean CONTINUE = true;
    protected static final boolean ABORT = false;

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
    protected static final String humanReadable( short formatCode ) {
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
    protected static final String getFileFormatAsString( final short formatCode ) {
        return Utility.format2String( formatCode );
    }

    /**
     * Gets the mime type string representation for the format code given.
     * 
     * @param formatCode
     *            numeric format code
     * @return the numerically coded {@code format} in human readable format
     *         as "<em>&lt;Mime String&gt;</em>"
     */
    protected static final String getFileFormatAsMimeType( final short formatCode ) {
        return Utility.format2Mime( formatCode );
    }

    /**
     * @see U#isEmpty(String)
     * 
     */
    protected static final boolean isEmpty( final String s ) {
        return U.isEmpty( s );
    }

    protected static final boolean isNotEmpty( final String s ) {
        return !U.isEmpty( s );
    }

    protected static final boolean isEmpty( final Filename fn ) {
        return U.isEmpty( fn );
    }

    protected static final boolean isNotEmpty( final Filename fn ) {
        return !U.isEmpty( fn );
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
    protected static final boolean isDestinationURI( final String given, final String scheme ) {
        if ( isEmpty( given ) ) {
            return false;
        }
        try {
            URI tmp = new URI( given );
            return !tmp.isOpaque() && tmp.isAbsolute() && isNotEmpty( tmp.getScheme() )
                    && tmp.getScheme().equalsIgnoreCase( scheme );
        } catch ( URISyntaxException syntax ) {
            return false;
        }
    }

    /**
     * Starts a new distribution cycle for a report to this destination.
     * <p>
     * <em>Remark:</em> The existence of the methods {@code Destination.setProperties()} and {@code Destination.getProperties()}
     * indicates that the {@code allProperties} will already have been passed to this instance so passing them seems redundant.
     * Class {@code Destination} also has an instance level field named {@code mProps} set/read by these methods.
     * 
     * @param allProperties
     *            parameters for this distribution passed as {@code Properties}
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

        return CONTINUE;
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
                sendMainFile( IOUtility.asPlatformFilename( Filename.of( cacheFileFilename ) ), fileFormat );
            } else {
                sendAdditionalFile( IOUtility.asPlatformFilename( Filename.of( cacheFileFilename ) ), fileFormat );
            }
        } catch ( Exception any ) {
            getLogger().error( "Error during sending file " + U.w( cacheFileFilename ) + ".", any );
            RWException rwException = Utility.newRWException( any );
            throw rwException;
        } catch ( Throwable somethingFatal ) {
            String message = "Fatal Error during sending " + ((isMainFile ? "main" : "additional")) + " file "
                    + U.w( cacheFileFilename ) + "!";
            getLogger().fatal( message, somethingFatal );
            throw Utility.newRWException( new Exception( message, somethingFatal ) );
        }
    }

    /**
     * Sends the main file from the distribution to the target.
     * 
     * @param cacheFileFilename
     * @param fileFormat
     * @throws RWException
     */
    protected abstract void sendMainFile( final Filename cacheFileFilename, final short fileFormat ) throws RWException;

    /**
     * Sends additional files from the distribution to the target.
     * 
     * @param cacheFileFilename
     * @param fileFormat
     * @throws RWException
     */
    protected abstract void sendAdditionalFile( final Filename cacheFileFilename, final short fileFormat ) throws RWException;

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
            InputStream in = IOUtility.inputStreamFromFile( sourceFile );
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
     * <p>
     * All destinations seem to be set up by the <code>main</code> thread of the reports server so we
     * should not expect multithreading issues ...
     * 
     * @param destinationsProperties
     *            the properties set in the report server's conf file within this
     *            destination's configuration section ({@code //destination/property})
     * @throws RWException
     */
    public static void init( final Properties destinationsProperties ) throws RWException {
        DestinationsLogging.assertRootLoggerExists();
        final Logger log = Logger.getRootLogger();
        try {
            Destination.init( destinationsProperties );
        } catch ( RWException rwException ) {
            log.fatal( "Error during delegation of initialization to Destination.", rwException );
            throw rwException;
        } catch ( Throwable somethingUnexpected ) {
            log.fatal( "Fatal error during delegation of initialization to Destination!" );
            throw Utility.newRWException( new Exception( "Fatal error during initializing MgpDestination" ) );
            // Back in the static shit here. All inheriting classes should duplicate this try-catch sequence ...
        }
    }

    /**
     * Initializes the logging of the destination based on the properties provided for the destination
     * in the report server's conf file.
     * Will override the corresponding settings of the {@code log4j.properties} distributed with the destination's JAR file.
     * <p>
     * <strong style="color:FireBrick">Must be called by leaf classes of the {@code MgpDestination} hierarchy, only</strong>
     * Else duplication and interweaving of log messages will occur.
     * </p>
     * 
     * 
     * @param destinationsProperties
     *            the properties set in the report server's conf file within the
     *            destination's configuration section ({@code //destination/property})
     * @param clazz
     *            Class to set up the logging for. Must be the (leaf) class calling this method.
     * 
     */
    protected static final void initLogging( final Properties destinationsProperties, Class clazz ) throws RWException {
        U.Rw.assertNotNull( clazz, "Cannot initialize logging with null Class!" );
        // final LocationInfo location = new LocationInfo( new Throwable(), MgpDestination.class.getName() );
        // final String callingClassName = location.getClassName();
        // if ( clazz.getName().equals( callingClassName ) ) {
        if ( destinationsProperties != null ) {
            /*
             * This is the reason why clazz has to match the calling class.
             * Imagine the following class hiearchy and each class performing its own initLogging() (during init()).
             * A
             * / \
             * B C
             * All of these calls will be given the same properties from the destination configuration in
             * the <reportserver>.conf file.
             * Starting with B this would be B with Properties B and then A with Properties B.
             * Followed by C with Properties C and then A with Properties C.
             * 
             * Each class will get its own Logger with its own appender, but after initialization
             * B's logger's appender points to file B.log. C's logger's appender to C.log and A's logger's
             * appender to C.log.
             * 
             * As log messages propagate up the hieararchy
             * B will log to B.log and C.log (via A)
             * C will log to C.log
             * A will log to C.log
             * 
             */
            final Filename logfilename = Filename.of( destinationsProperties.getProperty( "logfile" ) );
            final String loglevelname = destinationsProperties.getProperty( "loglevel", DEFAULT_LOG_LEVEL );
            DestinationsLogging.createOrReplacePackageLevelLogger( clazz, logfilename, loglevelname );
        } else {
            DestinationsLogging.createOrReplacePackageLevelLogger( clazz, null, DEFAULT_LOG_LEVEL );
        }
        // }
        /*
         * Logger.getRootLogger().error( "Calling class is " +
         * new LocationInfo( new Throwable(), clazz.getName() ).getClassName() );
         * ^^^^^^^^^^^^
         * ||||||||||||
         * Maybe useful for detecting call by a leaf class / the class specified as destination.
         * With Reports 10 getClassName() reports sun.reflect.NativeMethodAccessorImpl
         */
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
    protected static final boolean isBinaryFile( short formatCode ) {
        return new DesFTP().isBinaryFile( formatCode );
    }

}