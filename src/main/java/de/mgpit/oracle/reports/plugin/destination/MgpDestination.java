package de.mgpit.oracle.reports.plugin.destination;


import java.util.Properties;

import org.apache.log4j.Logger;

import de.mgpit.oracle.reports.plugin.commons.DestinationsLogging;
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

    private static final Logger LOG = Logger.getLogger( MgpDestination.class );

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
        LOG.info( "Distributing to Destination " + getDestype() );
        LOG.info(
                "Distribution target name is " + U.w( targetName ) + ". Main format is " + getFileFormatAsMimeType( mainFormat ) );
        LOG.info( "Distribution will contain " + U.w( totalNumberOfFiles ) + " files with a total size of " + U.w( totalFileSize )
                + " bytes." );

        return true; // continue to send
    }

    /**
     * Initialize the destination on Report Server startup.
     *
     * @param destinationsProperties
     *            the properties set in the report server's conf file within the
     *            destination's configuration section ({@code //destination/property})
     * @throws RWException
     */
    public static void init( final Properties destinationsProperties ) throws RWException {
        initLogging( destinationsProperties, MgpDestination.class );
        LOG.info( "Destination logging successfully initialized with properties: " + destinationsProperties );
        try {
            Destination.init( destinationsProperties );
        } catch ( RWException rwException ) {
            LOG.error( "Error during initializing Destination. See following message(s)!" );
            LOG.error( rwException );
            throw rwException;
        }
    }

    /**
     * Initializes the logging of the destination based on the properties provided in the destination's
     * configuration within the report server's conf file.
     * Will override the corresponding settings of the {@code log4j.properties} distributed with the destination's JAR file.
     * <p>
     * Will set the root {@code loglevel} if provided and the log file for {@code log4j.appender.fileout.File}.
     * 
     * @param destinationsProperties
     *            the properties set in the report server's conf file within the
     *            destination's configuration section ({@code //destination/property})
     */
    protected static void initLogging( final Properties destinationsProperties, Class clazz ) throws RWException {
        String logfileFilenameGiven = null;
        String loglevelLevelnameGiven = null;
        
        if ( destinationsProperties != null ) {
            logfileFilenameGiven = destinationsProperties.getProperty( "logfile" );
            loglevelLevelnameGiven = destinationsProperties.getProperty( "loglevel", "INFO" );
        }
        
        DestinationsLogging.setupPackageLevelLogger( clazz, logfileFilenameGiven, loglevelLevelnameGiven );
    }


//    private static void cel( String str ) {
//        Utility.createErrorLog( str, "myerror.log" );
//    }
}