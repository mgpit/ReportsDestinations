package de.mgpit.oracle.reports.plugin.destination.content;


import java.util.Properties;

import de.mgpit.oracle.reports.plugin.destination.content.cdm.SimpleCdm;
import junit.framework.TestCase;

public class SimpleCdmTest extends TestCase {

    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testBuild() {
        final SimpleCdm simple = new SimpleCdm();
        final Properties parameters = new Properties();
        for ( int lineNumber = 1; lineNumber < 6; lineNumber++ ) {
            final String key = "address_line_" + lineNumber;
            parameters.setProperty( key, key );
        }
        boolean exceptionOccured = false;
        try {
            simple.build( parameters );
        } catch ( Throwable anly ) {
            exceptionOccured = true;
            anly.printStackTrace( System.err );
        }
        assertFalse( exceptionOccured );
    }
}
