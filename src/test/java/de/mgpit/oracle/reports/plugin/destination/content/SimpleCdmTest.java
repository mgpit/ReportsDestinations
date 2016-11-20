package de.mgpit.oracle.reports.plugin.destination.content;

import java.util.Properties;

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
        final Properties foo = new Properties();
        boolean exceptionOccured = false;
        try  { 
            simple.build( foo );
        } catch (Throwable anly ) {
            exceptionOccured = true;
            anly.printStackTrace( System.err );
        }
        assertFalse( exceptionOccured );
    }
}
