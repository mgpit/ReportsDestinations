package de.mgpit.oracle.reports.plugin.destination;


import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import org.apache.log4j.Logger;

import HTTPClient.ParseException;
import HTTPClient.URI;
import de.mgpit.oracle.reports.plugin.commons.DestinationsLogging;
import de.mgpit.oracle.reports.plugin.commons.Magic;
import de.mgpit.oracle.reports.plugin.commons.U;
import oracle.reports.RWException;
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

    /**
     * 
     * @param format
     *            numeric format code
     * @return the numerically coded {@code format} in human readable format
     *         as "[<em>&lt;String&gt;</em>|<em>&lt;Mime String&gt;</em>]"
     */
    public static final String humanReadable( short format ) {
        return U.w( getFileFormatAsString( format ) + "|" + getFileFormatAsMimeType( format ) );
    }

    /**
     * 
     * @param format
     *            numeric format code
     * @return the numerically coded {@code format} in human readable format
     *         as "&lt;String&gt;</em>"
     */
    public static final String getFileFormatAsString( final short format ) {
        return Utility.format2String( format );
    }

    /**
     * 
     * @param format
     *            numeric format code
     * @return the numerically coded {@code format} in human readable format
     *         as "<em>&lt;Mime String&gt;</em>"
     */
    public static final String getFileFormatAsMimeType( final short format ) {
        return Utility.format2Mime( format );
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
     * 
     */
    protected boolean start( final Properties allProperties, final String targetName, final int totalNumberOfFiles,
            final long totalFileSize, final short mainFormat ) throws RWException {
        getLogger().info( "Distributing to Destination " + getDestype() );
        getLogger().info(
                "Distribution target name is " + U.w( targetName ) + ". Main format is " + getFileFormatAsMimeType( mainFormat ) );
        getLogger().info( "Distribution will contain " + U.w( totalNumberOfFiles ) + " files with a total size of "
                + U.w( totalFileSize ) + " bytes." );

        if ( getLogger().isDebugEnabled() ) {
            dumpProperties( allProperties );
        }

        return true; // continue to send
    }

    protected void dumpProperties( final Properties allProperties ) {
        Logger lOG = getLogger();
        Set keys = new TreeSet(); // we want the keys sorted ...
        keys.addAll( allProperties.keySet() );
        Iterator allKeys = keys.iterator();

        lOG.debug( "Got the following properties ..." );
        while ( allKeys.hasNext() ) {
            String key = (String) allKeys.next();
            String givenValue = allProperties.getProperty( key );

            String valueToPrint = filter( key, givenValue );
            lOG.debug( U.w( key ) + " -> " + U.w( valueToPrint.length() ) + U.w( valueToPrint ) );
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
            } catch ( ParseException parsex ) {
                return "<notparsable>" + inspectedValue;
            }
        }
        return inspectedValue;
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
            log.error( "Error during initializing Destination. See following message(s)!" );
            log.error( rwException );
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
}