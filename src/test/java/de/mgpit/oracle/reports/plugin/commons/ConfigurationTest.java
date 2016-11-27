package de.mgpit.oracle.reports.plugin.commons;


import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;

import com.sun.java.util.collections.Arrays;
import com.sun.java.util.collections.Comparator;
import com.sun.java.util.collections.Iterator;

import de.mgpit.oracle.reports.plugin.commons.driver.MQ.Configuration;
import junit.framework.TestCase;

public class ConfigurationTest extends TestCase {

    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testFromURI() {
        URI testUri = null;
        boolean uriOK = true;
        try {
            testUri = new URI( "wmq://localhost:1414/dest/queue/QMGR.IN.QUEUE@QMGR?channelName=CHANNEL_1" );
        } catch ( URISyntaxException syntax ) {
            uriOK = false;
        }
        assertTrue( uriOK );
        Configuration configurationToTest = Configuration.fromURI( testUri );
        assertNotNull( configurationToTest );
    }

    public void testToURI() {
        URI testUri = null;
        boolean uriOK = true;
        try {
            testUri = new URI( "wmq://localhost:1414/dest/queue/QMGR.IN.QUEUE@QMGR?channelName=CHANNEL_1" );
        } catch ( URISyntaxException syntax ) {
            uriOK = false;
        }
        assertTrue( uriOK );
        Configuration configurationToTest = Configuration.fromURI( testUri );
        assertEquals( testUri, configurationToTest.toURI() );
    }

    public void testEqualsObject() {
        URI testUri = null;
        boolean uriOK = true;
        try {
            testUri = new URI( "wmq", null, "localhost", 1414, "/dest/queue/QMGR.IN.QUEUE@QMGR", "channelName=CHANNEL_1", null );
        } catch ( URISyntaxException syntax ) {
            uriOK = false;
        }
        assertTrue( uriOK );

        Configuration c1 = Configuration.fromURI( testUri );
        Configuration c2 = Configuration.fromURI( testUri );
        Configuration c3 = new Configuration( "localhost", 1414, "QMGR", "CHANNEL_1", "QMGR.IN.QUEUE" );

        assertEquals( c1, c2 );
        assertEquals( c2, c1 ); // kommutativ
        assertEquals( c1, c1 ); // reflexiv

        assertEquals( c2, c3 );
        assertEquals( c1, c3 ); // transitiv
    }

    public void testQueryParameterOverridesWmqQmgr() {
        String expectedUriString = "wmq://localhost:1414/dest/queue/QMGR.IN.QUEUE@QMGR?channelName=CHANNEL_1";
        boolean uriOK = true;
        URI creationUri = null;
        try {
            creationUri = new URI(
                    "wmq://localhost:1414/dest/queue/QMGR.IN.QUEUE@NONSENSE?connectQueueManager=QMGR&channelName=CHANNEL_1" );
        } catch ( URISyntaxException syntax ) {
            uriOK = false;
        }
        assertTrue( uriOK );

        Configuration testConfiguration = Configuration.fromURI( creationUri );
        URI configurationsUri = testConfiguration.toURI();

        String actualUriString = configurationsUri.toString();
        assertEquals( expectedUriString, actualUriString );
    }

    public void testJavadocExampleCode() throws URISyntaxException {
        URI uri = new URI( "wmq://localhost/dest/queue/QUEUE.IN@QMGR?channelName=CHANNEL_1" );
        Configuration c1 = Configuration.fromURI( uri );

        URI another = new URI( "wmq", null, "localhost", 1414, "/dest/queue/QUEUE.IN@QMGR", "channelName=CHANNEL_1", null );
        Configuration c2 = Configuration.fromURI( another );

        URI third = new URI( "wmq", null, "localhost", 1414, "/dest/queue/QUEUE.IN@FOO",
                "connectQueueManager=QMGR&channelName=CHANNEL_1", null );
        Configuration c3 = Configuration.fromURI( third );
        
        System.out.println( "C1 and C2 are " + (c1.equals( c2 ) ? "equal" : "different") );
        System.out.println( "C2 and C3 are " + (c2.equals( c3 ) ? "equal" : "different") );
        System.out.println( "C1 and C3 are " + (c1.equals( c3 ) ? "equal" : "different") );

    }
    
    public void testDumpSystemProperties2StandardOut() {
        Properties systemProperties = System.getProperties();
        
        Object[] keySet = systemProperties.keySet().toArray();
        Arrays.sort( keySet, new Comparator() {

            public int compare( Object o1, Object o2 ) {
                String s1 = (String) o1; String s2 = (String) o2;
                return s1.compareTo( s2 );
            }
            
        });
        Iterator keys = Arrays.asList(keySet).iterator();
        
        int i = 0;
        while ( keys.hasNext() ){
            String key = (String)keys.next();
            String value = systemProperties.getProperty( key );
            
            // System.out.printf( "%1$3d: [%2$-30s] is [%3$s]\n", i++, key, value );
            System.out.println( U.rpad(i++,3)+": " + U.lpad( key,  30 ) + " is " + value );
        }
    }

}
