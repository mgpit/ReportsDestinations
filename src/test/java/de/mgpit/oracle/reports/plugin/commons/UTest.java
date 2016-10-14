package de.mgpit.oracle.reports.plugin.commons;


import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.MalformedInputException;

import junit.framework.TestCase;

public class UTest extends TestCase {

    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testObfuscate() {
        String connectString;

        connectString = "foo/bar";
        assertEquals( "foo/*****", U.obfuscate( connectString ) );

        connectString = "foo/bar@fizzbuzz";
        assertEquals( "foo/*****@fizzbuzz", U.obfuscate( connectString ) );

        connectString = "  foo/bar@fizzbuzz";
        assertEquals( "foo/*****@fizzbuzz", U.obfuscate( connectString ) );

        connectString = "foo/bar@fizzbuzz  ";
        assertEquals( "foo/*****@fizzbuzz", U.obfuscate( connectString ) );

        connectString = " foo/bar@fizzbuzz ";
        assertEquals( "foo/*****@fizzbuzz", U.obfuscate( connectString ) );

        connectString = "lorem ipsum";
        assertEquals( "*****", U.obfuscate( connectString ) );

        connectString = null;
        assertEquals( "*****", U.obfuscate( connectString ) );

    }

    public void testClassname() {

        System.out.println( U.classname( Object.class ) );
        System.out.println( Object.class.getName() );
        System.out.println( U.packagename( Object.class ) );
        System.out.println( Object.class.getPackage() );
        assertEquals( "Object", U.classname( Object.class ) );

        System.out.println( U.classname( String.class ) );
        System.out.println( String.class.getName() );
        System.out.println( U.packagename( String.class ) );
        System.out.println( String.class.getPackage() );
        assertEquals( "String", U.classname( String.class ) );

    }

    public void testURIvsURL() {
        String str = "wmq://localhost:1414/dest/queue/foo@bar";

        boolean noExceptionOccured = true;
        try {
            URI uri = new URI( str );
        } catch ( URISyntaxException syntax ) {
            syntax.printStackTrace();
            noExceptionOccured = false;
        }
        
        assertTrue( noExceptionOccured );
        
        try {
            URL url = new URL( str );
        } catch ( MalformedURLException malformed ) {
            malformed.printStackTrace();
            noExceptionOccured = false;
        }
        
        assertTrue( noExceptionOccured );

    }
    
    public void testArrayIteration() {
        String[] some = { "First", "Second", "Third", "Fourth", "Fifth" };
        int count = some.length;
        
        /* Forward */
        for ( int runIndex = 0; runIndex < count; runIndex++ ) {
            System.out.println( some[runIndex] );
        }
        
        /* Reverse */
        for ( int runIndex = --count; runIndex >= 0; --runIndex  ) {
            System.out.println( some[runIndex] );
        }
    }

}
