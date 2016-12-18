package de.mgpit.oracle.reports.plugin.destination.content.eai.fwk;


import java.util.Properties;

import junit.framework.TestCase;

public class SimpleFrameworkHeaderTest extends TestCase {

    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testBuild() {
        final SimpleFrameworkHeader simple = new SimpleFrameworkHeader();
        final Properties parameters = new Properties();
        boolean exceptionOccured = false;
        try {
            final String text = simple.getHeaderAsStringPropulatedWith( parameters );
            System.out.println( text );
        } catch ( Exception any ) {
            any.printStackTrace( System.err );
            exceptionOccured = true;
        }
        assertFalse( exceptionOccured );
    }

}
