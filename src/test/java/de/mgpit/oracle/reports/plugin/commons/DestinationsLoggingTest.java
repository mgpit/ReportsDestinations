package de.mgpit.oracle.reports.plugin.commons;

import junit.framework.TestCase;

public class DestinationsLoggingTest extends TestCase {

    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    
    public void testGivenOrFallbackFilenameFromClass() {
        boolean exceptionOccured = false;
        String message = "<null>";
        try {
            String filename = DestinationsLogging.givenOrFallbackFilenameFrom( null, DestinationsLoggingTest.class );
            System.out.println( filename );
        } catch ( Exception any ) {
            message = any.getMessage();
            any.printStackTrace( );
            exceptionOccured = false;
        }
        assertFalse( "There has been an exception: " + message, exceptionOccured );
    }
}
