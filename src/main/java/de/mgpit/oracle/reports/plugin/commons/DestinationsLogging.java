package de.mgpit.oracle.reports.plugin.commons;


import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Layout;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.RollingFileAppender;

import de.mgpit.oracle.reports.plugin.commons.io.IOUtility;
import de.mgpit.oracle.reports.plugin.destination.MgpDestination;
import de.mgpit.types.TypedString;
import oracle.reports.RWException;
import oracle.reports.utility.Utility;

/**
 * 
 * @author mgp
 * 
 *         Factory for setting up a log file for an Oracle Reports destination plugin.
 *         <p>
 *         There are two factory methods
 *         <ul>
 *         <li>{@link #createOrReplaceClassLevelLogger(Class, String, String)}</li>
 *         <li>{@link #createOrReplacePackageLevelLogger(Class, String, String)}</li>
 *         </ul>
 *         each taking three parameters
 *         <ul>
 *         <li>a Class for determining the logger's namespace</li>
 *         <li>an optional filename/path for setting the log file</li>
 *         <li>an optional String for setting the logger's log level</li>
 *         </ul>
 *         <h2>Determining the log file's file name</h2>
 *         <ul>
 *         <li>The simplest situation is where that the caller of one of the factory methods provides a full path for the
 *         log file's file name and points to a valid directory.</li>
 *         <li>If there's no file name provided the class' full name will be used as file name with all <code>.</code> (dots)
 *         replaced by <code>_</code> (underlines) and an extension of <code>.log</code>.</li>
 *         <li>If there directory provides is <strong>not valid</strong> (i.e. does not exist or there is no read/write access [for the
 *         Oracle Reports process] <sup>1</sup> ) the log file will be placed
 *         <ol>
 *         <li>in the Oracle Reports <code>logs directory</code>
 *         <li>or, if this is not valid, in the Oracle Reports <code>temp direcotry</code>
 *         <li>or, if this is not valid, in the <code>default temp direcotry</code>
 *         </ol>
 *         </ul>
 *         <p>
 *         <sup>1</sup> The "is valid" property is of course time dependent. Any changes made to the file system after creating
 *         the logger may have the effect that the directory given is not valid any more.
 */
public final class DestinationsLogging {

    /**
     * Holds a list of loggers (identified by their name) already configured ...
     * <p>
     * If a logger is registered here it will not be configured again.
     * <p>
     * Consequence/goal: Subsequent MgpDestinations addressing the same logger will not override the logger with their configuration.
     * This is "first come first serve"
     */
    private static final List configuredDestinationLoggerNames = new LinkedList();

    /**
     * Holds the layout for a log message pattern made of ISO8601 date , level , and message
     */
    private static final Layout DATE_LEVEL_MESSAGE_LAYOUT = new PatternLayout( "%d{ISO8601} [%-5p] :: %m%n" );

    /**
     * Holds the layout for a log message pattern made of ISO8601 date , class , thread , level , and message
     */
    private static final Layout VERBOSE_WIDE_LAYOUT = new PatternLayout( "%d{ISO8601} | %-65C | %-25t | %-5p :: %m%n" );

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

    /**
     * Sets up a logger once. Subsequent calls for the same logger will have no effect.
     * <p>
     * The logger will be stripped of any existing appenders and given a RollingFileAppender.
     * 
     * @param clazz
     *            Should be class of the caller. Will be used to derive default values.
     * @param givenLoggerName
     *            Name of / namespace for the logger
     * @param givenOptionalFilename
     *            file name for the loggers file appender. On null name will be derived from clazz
     * @param optionalLoglevelName
     *            log level to set. On null level will be info.
     * 
     * @throws RWException
     */
    static final void setupLogger( final Class clazz, String givenLoggerName, final String givenOptionalFilename,
            final String optionalLoglevelName ) throws RWException {

        U.Rw.assertNotNull( givenLoggerName );
        String loggerName = givenLoggerName.trim();
        String optionalFilename = (givenOptionalFilename == null) ? givenOptionalFilename : givenOptionalFilename.trim();

        /*
         * From observation the report server sets up all destinations sequentially
         * running the main thread. Yet ensure only one thread manipulates the {@see #configuredDestinationLoggerNames}
         * at the same time.
         */
        synchronized (configuredDestinationLoggerNames) {
            if ( configuredDestinationLoggerNames.contains( loggerName ) ) return;

            final Logger logger = Logger.getLogger( loggerName );
            logger.removeAllAppenders();
            resetLoglevel( logger, optionalLoglevelName );
            logger.setAdditivity( Magic.ADD_MESSAGES_TO_ANCESTORS );

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

    }

    /**
     * Sets the loggers log level.
     * <p>
     * In contrast to {@link Level#toLevel(String)} the fallback level will be {@link Level#INFO}.
     * 
     * @param logger
     *            the logger to modify
     * @param optionalLoglevelName
     *            log level name as in {@link Level#toLevel(String)}
     */
    private static final void resetLoglevel( final Logger logger, final String optionalLoglevelName ) {
        Level newLevel = Level.INFO;

        if ( !U.isEmpty( optionalLoglevelName ) ) {
            newLevel = Level.toLevel( optionalLoglevelName );
            if ( !newLevel.toString().equals( optionalLoglevelName ) ) {
                // Conversion failed ...
                // Don't want DEBUG as fallback, though
                newLevel = Level.INFO;
            }
        }
        logger.setLevel( newLevel );
    }

    /**
     * Creates a FileAppender instance for the class given.
     * 
     * @param clazz
     *            class for which the FileApender will be created
     * @param optionalFilename
     *            optional file name for the logfile
     * @return a new RollingFileAppender
     * @throws IOException
     */
    private static final FileAppender buildFileAppender( final Class clazz, final String optionalFilename ) throws IOException {
        String filename = givenOrFallbackFilenameFrom( optionalFilename, clazz );
        RollingFileAppender newAppender = new RollingFileAppender( DATE_LEVEL_MESSAGE_LAYOUT, filename,
                Magic.APPEND_MESSAGES_TO_LOGFILE );
        newAppender.setMaxFileSize( "1MB" );
        newAppender.setMaxBackupIndex( 5 ); // arbitrary - may be sufficient for 5 days ...
        newAppender.activateOptions();
        return newAppender;
    }

    /* Leave it package global for JUnit Tests ... */
    /**
     * Gets a filename usable for creating a log file.
     * 
     * @see #givenOrFallbackFilenameFrom(String, String)
     * 
     * @param optionalFilename
     *            file name, maybe null
     * @param clazz
     *            class of which's name will be used as alternative file name
     * @return string with full file name
     */
    static final String givenOrFallbackFilenameFrom( final String optionalFilename, final Class clazz ) {
        String classNameAsFilename = IOUtility.asLogfileFilename( clazz.getName() );
        return givenOrFallbackFilenameFrom( optionalFilename, classNameAsFilename );
    }

    /* Leave it package global for JUnit Tests ... */
    /**
     * Gets a filename usable for creating a log file.
     * <p>
     * Goal is to return a valid file name, i.e. a filename located in an existing directory/path which the current process
     * is allowed to access.
     * Normally this should be the file name provided. If not, the report servers log directory is considered, if this won't work
     * the report servers temp directory is considered and finally the "system"'s temp directory.
     * 
     * @param optionalFilename
     *            file name, maybe null
     * @param alternativeFilename
     *            alternative file name to use if the optionalFilename is null
     * @return string with full filename
     */
    static final String givenOrFallbackFilenameFrom( final String optionalFilename, final String alternativeFilename ) {
        String filenameToStartWith = U.coalesce( optionalFilename, alternativeFilename );

        File dummy = IOUtility.asFile( filenameToStartWith );
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

        File logfile = IOUtility.asFile( directoryname, filename );
        return logfile.getPath();
    }

    /**
     * Checks if the pathname is a valid directory, i.e. must be
     * <ul>
     * <li>an existing directory</li>
     * <li>readable</li>
     * <li>writable</li>
     * </ul>
     * 
     * @param maybeDirectoryname
     *            pathname to test
     * @return <code>true</code> if the pathname is a valid directory, <code>false</code> else.
     */
    private static boolean isValidDirectory( final String maybeDirectoryname ) {
        if ( U.isEmpty( maybeDirectoryname ) ) {
            return false;
        }
        File possibleDirectory = IOUtility.asFile( maybeDirectoryname );

        boolean valid = possibleDirectory.isDirectory() && possibleDirectory.canRead() && possibleDirectory.canWrite();
        return valid;
    }

    /**
     * Holds if the root Logger already has been set up.
     */
    private static boolean rootLoggerIsSetUp = false;

    /*
     * Asserts that the root logger exists.
     * <p>
     * Deducting from observation the report server sets up all destinations sequentially
     * running the main thread. Yet ensure only one thread manipulates the {@see #rootLoggerIsSetUp}
     * at the same time by making this method synchronized.
     */
    public synchronized static void assertRootLoggerExists() {
        Logger root = Logger.getRootLogger();

        if ( !rootLoggerIsSetUp ) {
            root.removeAllAppenders();
            root.setLevel( Level.INFO );

            String directoryname;
            directoryname = Utility.getLogsDir();
            if ( !isValidDirectory( directoryname ) ) {
                directoryname = Utility.getTempDir();
                if ( !isValidDirectory( directoryname ) ) {
                    directoryname = IOUtility.getTempDir();
                }
            }

            File logFile = IOUtility.asFile( directoryname, IOUtility.asLogfileFilename( MgpDestination.class.getName() ) );
            try {
                RollingFileAppender rootLog = new RollingFileAppender( VERBOSE_WIDE_LAYOUT, logFile.getPath(),
                        Magic.APPEND_MESSAGES_TO_LOGFILE );
                rootLog.setThreshold( root.getLevel() );
                rootLog.setMaxFileSize( "10MB" );
                rootLog.setMaxBackupIndex( 3 );
                rootLog.activateOptions();
                root.addAppender( rootLog );
            } catch ( IOException ioex ) {
                ConsoleAppender consoleLog = new ConsoleAppender( VERBOSE_WIDE_LAYOUT, "System.err" );
                consoleLog.setThreshold( root.getLevel() );
                consoleLog.activateOptions();
                root.addAppender( consoleLog );
                root.error( "Error on setting up Root File Appender", ioex );
            }
            rootLoggerIsSetUp = true;
        }

    }

}
