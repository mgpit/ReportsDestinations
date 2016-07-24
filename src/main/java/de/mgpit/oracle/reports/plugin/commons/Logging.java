package de.mgpit.oracle.reports.plugin.commons;


import java.io.IOException;
import java.util.logging.Level;

import org.apache.log4j.FileAppender;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.Priority;
import org.apache.log4j.RollingFileAppender;

import de.mgpit.oracle.reports.plugin.commons.io.IOUtility;

public class Logging {

    private static final String DATE_SEVERITY_MESSAGE = "%d{yyyy-MM-dd HH:mm:ss} [%-5p] - %m%n";
    private static final PatternLayout DEFAULT_LAYOUT = new PatternLayout( DATE_SEVERITY_MESSAGE );
    private static final Level DEFAULT_THRESHOLD = Level.INFO;

    public static final FileAppender getClassLevelFileAppender( final Class clazz, final String optionalFileName )
            throws IOException {
        return getFileAppender( clazz, optionalFileName, false );
    }

    public static final FileAppender getPackageLevelFileAppender( final Class clazz, final String optionalFileName )
            throws IOException {
        return getFileAppender( clazz, optionalFileName, true );

    }

    private static final FileAppender getFileAppender( final Class clazz, final String optionalFileName,
            final boolean packageLevel ) throws IOException {
        
        final String appenderName = ( packageLevel )?U.packagename( clazz ):clazz.getName();
        final String fileName = IOUtility.asLogFileName( appenderName );
        
        final FileAppender newFileAppender = new RollingFileAppender( DEFAULT_LAYOUT, fileName, true );
        newFileAppender.setName( appenderName );
        newFileAppender.activateOptions();
        return newFileAppender;
    }

    private Logging() {
    }

}
