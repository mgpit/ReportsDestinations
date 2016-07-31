package de.mgpit.oracle.reports.plugin.commons;


import java.io.File;
import java.io.IOException;
import java.security.AccessController;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.FileAppender;
import org.apache.log4j.Layout;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.RollingFileAppender;

import de.mgpit.oracle.reports.plugin.commons.io.IOUtility;
import oracle.reports.RWException;
import oracle.reports.utility.Utility;
import sun.security.action.GetPropertyAction;

public final class DestinationsLogging {

    /**
     * List of loggers (identified by their name) already configured ...
     * <p>
     * If a logger is registered here it will not be configured again.
     * <p>
     * Consequence: Subsequent MgpDestinations
     * addressing the same logger will not override the logger with their configuration. This is "first come first serve"
     */
    private static final List configuredDestinationLoggerNames = new LinkedList();

    private static final Layout DATE_LEVEL_MESSAGE_LAYOUT = new PatternLayout( "%d{yyyy-MM-dd HH:mm:ss} [%-5p] - %m%n" );

    public static final void setupClassLevelLogger( final Class clazz, final String optionalFilename,
            final String optionalLoglevelName ) throws RWException {
        setupLogger( clazz, U.classname( clazz ), optionalFilename, optionalLoglevelName );
    }

    public static final void setupPackageLevelLogger( final Class clazz, final String optionalFilename,
            final String optionalLoglevelName ) throws RWException {
        setupLogger( clazz, U.packagename( clazz ), optionalFilename, optionalLoglevelName );
    }

    protected static final void setupLogger( final Class clazz, String givenLoggerName, final String givenOptionalFilename,
            final String optionalLoglevelName ) throws RWException {

        U.Rw.assertNotNull( givenLoggerName );
        String loggerName = givenLoggerName.trim();
        String optionalFilename = (givenOptionalFilename == null) ? givenOptionalFilename : givenOptionalFilename.trim();
        if ( configuredDestinationLoggerNames.contains( loggerName ) ) return;

        final Logger logger = Logger.getLogger( loggerName );
        logger.removeAllAppenders();

        resetLoglevel( logger, optionalLoglevelName );

        try {
            FileAppender newAppender = buildFileAppender( clazz, optionalFilename );
            logger.addAppender( newAppender );

            logger.info( "Logging to: " + newAppender.getFile() );
            logger.info( "Current level is: " + logger.getLevel().toString() );
        } catch ( IOException ioex ) {
            throw Utility.newRWException( ioex );
        } finally {
            // At least we tried to setup the logger named "loggerName" ...
            // ... so don't try again in any case
            configuredDestinationLoggerNames.add( loggerName );
        }

    }

    protected static final void resetLoglevel( final Logger logger, final String optionalLoglevelName ) {
        Level newLevel = Level.INFO;

        if ( !U.isEmpty( optionalLoglevelName ) ) {
            newLevel = Level.toLevel( optionalLoglevelName );
            if ( !newLevel.toString().equals( optionalLoglevelName ) ) {
                // Conversion failed ...
                // Don't want DEBUG als fallback, though
                newLevel = Level.INFO;
            }
        }
        logger.setLevel( newLevel );
    }

    protected static final FileAppender buildFileAppender( final Class clazz, final String optionalFilename ) throws IOException {
        String filename = givenOrFallbackFilenameFrom( optionalFilename, clazz );
        RollingFileAppender newAppender = new RollingFileAppender( DATE_LEVEL_MESSAGE_LAYOUT, filename, Magic.LOG_APPEND );
        newAppender.setMaxFileSize( "1MB" );
        newAppender.setMaxBackupIndex( 5 ); // arbitrary - may be sufficient for 5 days ...
        newAppender.activateOptions();
        return newAppender;
    }

    protected static final String givenOrFallbackFilenameFrom( final String optionalFilename, final Class clazz ) {
        String classNameAsFilename = IOUtility.asLogfileFilename( clazz.getName() );
        return givenOrFallbackFilenameFrom( optionalFilename, classNameAsFilename );
    }

    protected static final String givenOrFallbackFilenameFrom( final String optionalFilename, final String alternativeFilename ) {
        String filenameToStartWith = U.coalesce( optionalFilename, alternativeFilename );

        File dummy = new File( filenameToStartWith );
        String directoryname = dummy.getParent();
        String filename = U.coalesce( dummy.getName(), alternativeFilename );

        if ( !isValidDirectory( directoryname ) ) {
            directoryname = Utility.getLogsDir();
            if ( !isValidDirectory( directoryname ) ) {
                directoryname = Utility.getTempDir();
                if ( !isValidDirectory( directoryname ) ) {
                    directoryname = IOUtility.getTempDir();
                }
            }
        }

        File logfile = new File( new File( directoryname ), filename );
        return logfile.getPath();
    }

    private static boolean isValidDirectory( final String directoryname ) {
        if ( U.isEmpty( directoryname ) ) {
            return false;
        }
        File directory = new File( directoryname );
        return directory.exists();
    }

    private static final boolean isDirectory( final File file ) {
        boolean maybeDirectory = file.exists();
        return maybeDirectory & file.isDirectory();
    }

}
