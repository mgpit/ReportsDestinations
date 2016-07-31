package de.mgpit.oracle.reports.plugin.commons;


import java.io.File;
import java.io.IOException;
import java.security.AccessController;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Appender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Layout;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

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

    public static final void setupClassLevelLogger( Class clazz, String optionalFilename, String optionalLoglevelName )
            throws RWException {
        setupLogger( clazz, U.classname( clazz ), optionalFilename, optionalLoglevelName );
    }

    public static final void setupPackageLevelLogger( Class clazz, String optionalFilename, String optionalLoglevelName )
            throws RWException {
        String trimmedFilename = optionalFilename.trim();
        String trimmedLoglevelName = optionalLoglevelName.trim().toUpperCase();
        setupLogger( clazz, U.packagename( clazz ), trimmedFilename, trimmedLoglevelName );
    }

    private static final void setupLogger( Class clazz, String loggerName, String optionalFilename, String optionalLoglevelName )
            throws RWException {
        if ( configuredDestinationLoggerNames.contains( loggerName ) ) return;

        final Logger logger = Logger.getLogger( loggerName );
        logger.removeAllAppenders();

        resetLoglevel( logger, optionalLoglevelName );

        try {
            Appender newAppender = buildFileAppender( clazz, optionalFilename );
            logger.addAppender( newAppender );
        } catch ( IOException ioex ) {
            throw Utility.newRWException( ioex );
        } finally {
            // At least we tried to setup the logger named "loggerName" ...
            // ... so don't try again in any case
            configuredDestinationLoggerNames.add( loggerName );
        }

    }

    private static final void resetLoglevel( Logger logger, String optionalLoglevelName ) {
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

    private static final Appender buildFileAppender( Class clazz, String optionalFilename ) throws IOException {
        String filename = givenOrFallbackFilenameFrom( optionalFilename, clazz );
        FileAppender newAppender = new FileAppender( DATE_LEVEL_MESSAGE_LAYOUT, filename, Magic.LOG_APPEND );
        return newAppender;
    }

    private static final String givenOrFallbackFilenameFrom( String optionalFilename, Class clazz ) {
        return givenOrFallbackFilenameFrom( optionalFilename, clazz.getName() );
    }

    private static final String givenOrFallbackFilenameFrom( String optionalFilename, String alternativeFilename ) {
        String filenameToStartWith = U.coalesce( optionalFilename, alternativeFilename );

        File dummy = new File( filenameToStartWith );
        String directoryname = dummy.getParent();
        String filename = U.coalesce( dummy.getName(), alternativeFilename );

        if ( !isValidDirectory( directoryname ) ) {
            directoryname = Utility.getLogsDir();
            if ( !isValidDirectory( directoryname ) ) {
                directoryname = Utility.getTempDir();
                if ( !isValidDirectory( directoryname ) ) {
                    directoryname = getTempDir();
                }
            }
        }

        File logfile = new File( new File( directoryname ), filename );
        return logfile.getPath();
    }

    private static String tmpdir;

    private static synchronized String getTempDir() {
        if ( tmpdir == null ) {
            GetPropertyAction a = new GetPropertyAction( "java.io.tmpdir" );
            tmpdir = ((String) AccessController.doPrivileged( a ));
        }
        return tmpdir;
    }

    private static boolean isValidDirectory( String directoryname ) {
        if ( U.isEmpty( directoryname ) ) {
            return false;
        }
        File directory = new File( directoryname );
        return directory.exists();
    }

    private static final boolean isDirectory( File file ) {
        boolean maybeDirectory = file.exists();
        return maybeDirectory & file.isDirectory();
    }

}
