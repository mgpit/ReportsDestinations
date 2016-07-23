package de.mgpit.oracle.reports.plugin.destination;


import java.io.File;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Properties;

import org.apache.log4j.Appender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

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
    protected static void initLogging( final Properties destinationsProperties, Class clazz ) {
        cel( "Trying to init Logging for " + clazz.getName() );
        if ( destinationsProperties != null ) {
            Properties log4jProperties = new Properties();
            try {
                InputStream in = clazz.getResourceAsStream( "/log4j.properties" );
                log4jProperties.load( in );
            } catch ( Exception any ) {
                log4jProperties = null;
                cel( "Error when reading log4j.properties: " + any.toString() );
            }

            if ( log4jProperties != null ) {
                cel( "Re-Configuring log4j ..." );
                PropertyConfigurator.configure( log4jProperties );

                Logger logger = Logger.getLogger( clazz.getPackage().getName() );
                setLogFile( destinationsProperties, logger );
                setLogLevel( destinationsProperties, logger );

                dumpLogger( logger );
                Logger clazzLogger = Logger.getLogger( clazz );
                dumpLogger( clazzLogger );
                // dumpLogger( (Logger)Logger.getLogger( clazz ).getParent());

            }
        }
    }

    protected static void dumpLogger( Logger logger ) {
        if ( logger == null ) {
            cel( "NULL Logger" );
        } else {
            cel( "Checking Logging setup ..." );
            cel( "Logger name is " + logger.getName() + " :: " + logger.toString() );
            cel( "Logging level is " + ((logger.getLevel()==null)?"":logger.getLevel().toString()) );
            Enumeration appenders = logger.getAllAppenders();
            if ( appenders != null ) {
                while ( appenders.hasMoreElements() ) {
                    Appender a = (Appender) appenders.nextElement();
                    cel( "Appender named: " + a.getName() );
                    if ( FileAppender.class.isAssignableFrom( a.getClass() ) ) {
                        FileAppender fa = (FileAppender) a;
                        cel( fa.getName() + " is a FileAppender" );
                        cel( "FileAppender logs to File " + fa.getFile() );
                    }
                }
            }
        }
    }

    protected static void cel( final String message ) {
        Utility.createErrorLog( message, "lorem.log" );
    }

    /**
     * 
     */
    private static void setLogLevel( final Properties destinationsProperties, final Logger logger ) {
        String logLevel = destinationsProperties.getProperty( "loglevel" );
        if ( !isEmpty( logLevel ) ) {
            logger.setLevel( Level.toLevel( logLevel ) );
        }
    }

    private static void setLogFile( final Properties destinationsProperties, Logger logger ) {
        String logFileName = destinationsProperties.getProperty( "logfile", null );
        if ( isEmpty( logFileName ) ) {
            String targetDir = Utility.getLogsDir();
            if ( isEmpty( targetDir ) ) {
                targetDir = Utility.getTempDir();
            }
            logFileName = (!isEmpty( targetDir )) ? targetDir + File.separator + "destination.log" : "";
        }
        if ( !isEmpty( logFileName ) ) {
            Enumeration appenders = logger.getAllAppenders();
            while ( appenders.hasMoreElements() ) {
                Appender anAppender = (Appender) appenders.nextElement();
                if ( FileAppender.class.isAssignableFrom( anAppender.getClass() ) ) {
                    FileAppender aFileAppender = (FileAppender) anAppender;
                    if ( isEmpty( aFileAppender.getFile() ) ) {
                        aFileAppender.setFile( logFileName );
                    }
                }
            }
        }
        LOG.info( "Log file now is " + logFileName );

    }
}