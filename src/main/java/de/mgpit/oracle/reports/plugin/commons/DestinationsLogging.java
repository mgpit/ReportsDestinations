package de.mgpit.oracle.reports.plugin.commons;


import java.io.File;
import java.io.FilePermission;
import java.io.IOException;
import java.security.AccessControlException;
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

/**
 * 
 * @author mgp
 * 
 * Factory for setting up a log file for an Oracle Reports destination plugin.
 * <p>
 * There are two factory methods
 * <ul>
 *      <li>{@link #createOrReplaceClassLevelLogger(Class, String, String)}</li>
 *      <li>{@link #createOrReplacePackageLevelLogger(Class, String, String)}</li>
 * </ul>
 * each taking three parameters
 * <ul>
 *      <li>a Class for determining the logger's namespace</li>
 *      <li>an optional filename/path for setting the log file</li>
 *      <li>an optional String for setting the logger's log level</li> 
 * </ul>
 * <h2>Determining the log file's file name</h2>
 * <ul>
 *      <li>The simplest situation is where that the caller of one of the factory methods provides a full path for the 
 *      log file's file name and points to a valid directory.</li>
 *      <li>If there's no file name provided the class' full name will be used as file name with all <code>.</code> (dots)
 *      replaced by <code>_</code> (underlines) and an extension of <code>.log</code>.</li>
 *      <li>If there directory provides is <strong>not valid</strong> (i.e. does not exist or there is no read/write access [for the
 *      Oracle Reports process] <sup>1</sup> ) the log file will be placed
 *      <ol>
 *          <li>in the Oracle Reports <code>logs directory</code>
 *          <li>or, if this is not valid, in the Oracle Reports <code>temp direcotry</code>
 *          <li>or, if this is not valid, in the <code>default temp direcotry</code> 
 *      </ol>
 * </ul>
 * <p>
 * <sup>1</sup> The "is valid" property is of course time dependent. Any changes made to the file system after creating
 * the logger may have the effect that the directory given is not valid any more. 
 */
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

    /**
     * Creates a log4j logger for the namespace definded by the class' full name.
     * The logger will have one appender - a RollingFileAppender.
     * 
     * @param clazz
     *            The class for which the logger will be set up. Will be used to derive the logger namespace. May be used for determining the log file name.
     * @param optionalFilename
     *            A filename/path, maybe null
     * @param optionalLoglevelName
     *            Any String understood by log4j's {@link Level#toLevel} method. The log level for the logger, maybe null, defaults to <code>INFO</code>.
     *            
     * @throws RWException
     */
    public static final void createOrReplaceClassLevelLogger( final Class clazz, final String optionalFilename,
            final String optionalLoglevelName ) throws RWException {
        setupLogger( clazz, U.classname( clazz ), optionalFilename, optionalLoglevelName );
    }

    /**
     * Creates a log4j logger for the namespace definded by the class' package name.
     * The logger will have one appender - a RollingFileAppender.
     * 
     * @param clazz
     *            The class for which the logger will be set up. Will be used to derive the logger namespace. May be used for determining the log file name.
     * @param optionalFilename
     *            A filename/path, maybe null
     * @param optionalLoglevelName
     *            Any String understood by log4j's {@link Level#toLevel} method. The log level for the logger, maybe null, defaults to <code>INFO</code>.
     *            
     * @throws RWException
     */
    public static final void createOrReplacePackageLevelLogger( final Class clazz, final String optionalFilename,
            final String optionalLoglevelName ) throws RWException {
        setupLogger( clazz, U.packagename( clazz ), optionalFilename, optionalLoglevelName );
    }

    static final void setupLogger( final Class clazz, String givenLoggerName, final String givenOptionalFilename,
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

    static final void resetLoglevel( final Logger logger, final String optionalLoglevelName ) {
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

    static final FileAppender buildFileAppender( final Class clazz, final String optionalFilename ) throws IOException {
        String filename = givenOrFallbackFilenameFrom( optionalFilename, clazz );
        RollingFileAppender newAppender = new RollingFileAppender( DATE_LEVEL_MESSAGE_LAYOUT, filename, Magic.LOG_APPEND );
        newAppender.setMaxFileSize( "1MB" );
        newAppender.setMaxBackupIndex( 5 ); // arbitrary - may be sufficient for 5 days ...
        newAppender.activateOptions();
        return newAppender;
    }

    static final String givenOrFallbackFilenameFrom( final String optionalFilename, final Class clazz ) {
        String classNameAsFilename = IOUtility.asLogfileFilename( clazz.getName() );
        return givenOrFallbackFilenameFrom( optionalFilename, classNameAsFilename );
    }

    static final String givenOrFallbackFilenameFrom( final String optionalFilename, final String alternativeFilename ) {
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
                    // Assumption: tempdir will always exist and be valid
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
        
        return isDirectory( directoryname ) && canReadWriteDeleteInDirectory( directoryname );
    }
    
    private static final boolean canReadWriteDeleteInDirectory( final String directoryname) {
        boolean weCan = true;
        try {
            AccessController.checkPermission(new FilePermission(directoryname, "read,write,delete"));
        } catch ( AccessControlException ace ) {
            weCan = false;
        }
        return weCan;
    }

    private static final boolean isDirectory( final String maybeDirectoryName ) {
        File maybeDirectory = new File( maybeDirectoryName );
        return maybeDirectory.exists() && maybeDirectory.isDirectory();
    }

}
