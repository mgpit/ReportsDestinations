package de.mgpit.oracle.reports.plugin.commons;


import de.mgpit.types.Filename;
import junit.framework.TestCase;
import oracle.reports.utility.Utility;

public class DestinationsLoggingTest extends TestCase {

    protected void setUp() throws Exception {
        super.setUp();
        Utility.setOracleHome( "O:\\DevSuite10gR2" );
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testGivenOrFallbackFilenameFromClass() {
        boolean exceptionOccured = false;
        String message = "<null>";
        try {
            Filename filename = DestinationsLogging.givenOrFallbackFilenameFrom( null, DestinationsLoggingTest.class );
            final Filename expected = Filename.of( "O:\\DevSuite10gR2\\reports\\logs\\de_mgpit_oracle_reports_plugin_commons_DestinationsLoggingTest.log" );
            assertEquals( expected, filename );
        } catch ( Exception any ) {
            message = any.getMessage();
            any.printStackTrace();
            exceptionOccured = true;
        }
        assertFalse( "There has been an exception: " + message, exceptionOccured );
        
        
        try {
            final Filename expected = Filename.of( "O:\\DevSuite10gR2\\reports\\logs\\de_mgpit_oracle_reports_plugin_commons_DestinationsLoggingTest.log" );
            Filename filename = DestinationsLogging.givenOrFallbackFilenameFrom( expected, DestinationsLoggingTest.class );
            assertEquals( expected, filename );
        } catch ( Exception any ) {
            message = any.getMessage();
            any.printStackTrace();
            exceptionOccured = true;
        }
        assertFalse( "There has been an exception: " + message, exceptionOccured );
    }

    public void testSetupPackageLevelLogger() {
        boolean exceptionOccured = false;
        String message = "<null>";
        try {
            DestinationsLogging.createOrReplacePackageLevelLogger( DestinationsLoggingTest.class, null, null );
        } catch ( Exception any ) {
            message = any.getMessage();
            any.printStackTrace();
            exceptionOccured = true;
        }
        assertFalse( "There has been an exception: " + message, exceptionOccured );
    }

}
