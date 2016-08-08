package de.mgpit.oracle.reports.plugin.commons;


import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import de.mgpit.oracle.reports.plugin.commons.io.IOUtility;
import oracle.reports.utility.Utility;

public class DestinationsLoggingJUL {

    private static final List CONFIGURED_LOGGER_NAMES = new LinkedList();
    private static final Formatter FORMATTER;

    public static Logger getClassLogger( final Class clazz, final String optionalFilename ) {
        String loggerName = U.classname( clazz );
        return getLogger( clazz, loggerName, optionalFilename );
    }

    public static Logger getPackageLogger( final Class clazz, final String optionalFilename ) {
        String loggerName = U.packagename( clazz );
        return getLogger( clazz, loggerName, optionalFilename );
    }

    private static synchronized Logger getLogger( final Class clazz, final String loggerName, final String optionalFilename ) {
        boolean mustSetupLogfileHandler = false;

        Logger logger = LogManager.getLogManager().getLogger( loggerName );
        if ( logger == null ) {
            logger = Logger.getLogger( loggerName );
        }

        mustSetupLogfileHandler = !CONFIGURED_LOGGER_NAMES.contains( loggerName );

        if ( mustSetupLogfileHandler ) {
            setupLogfileHandler( logger, optionalFilename );
            CONFIGURED_LOGGER_NAMES.add( loggerName );
        }

        return logger;
    }

    private static void setupLogfileHandler( final Logger logger, final String optionalFilename ) {
        String filename = getLogfileFilename( optionalFilename, logger.getName() );
        removeExistingHandlersOnLogger( logger );

        try {
            FileHandler fileHandler = new FileHandler( filename, Units.ONE_MEGABYTE, 0, Magic.APPEND_MESSAGES_TO_LOGFILE );
            fileHandler.setFormatter( FORMATTER );
            logger.addHandler( fileHandler );
        } catch ( IOException ioex ) {
            System.err.println( ioex.getMessage() );
        }

    }

    private static void removeExistingHandlersOnLogger( final Logger logger ) {
        Handler[] registeredHandlers = logger.getHandlers();
        if ( registeredHandlers.length > 0 ) {
            Iterator handlers = Arrays.asList( registeredHandlers ).iterator();
            while ( handlers.hasNext() ) {
                Handler aHandler = (Handler) handlers.next();
                logger.removeHandler( aHandler );
            }
        }
    }

    private static String getLogfileFilename( final String optionalFilename, final String loggerName ) {
        String filenameToStartWith = (optionalFilename != null) ? optionalFilename : IOUtility.asLogfileFilename( loggerName );

        File dummy = new File( filenameToStartWith );
        String directoryname = dummy.getPath();
        String filename = dummy.getName();

        boolean mustCalculateDirectoryName = U.isEmpty( directoryname ) || !(new File( directoryname ).exists());
        if ( mustCalculateDirectoryName ) {
            directoryname = Utility.getLogsDir();
            if ( U.isEmpty( directoryname ) ) {
                directoryname = Utility.getTempDir();
                if ( U.isEmpty( directoryname ) ) {
                    directoryname = System.getProperty( "user.dir" );
                }
            }
        }

        File logfile = new File( new File( directoryname ), filename );
        return logfile.getPath();
    }
    
    static {
        FORMATTER = new SimpleFormatter();
    }

}
