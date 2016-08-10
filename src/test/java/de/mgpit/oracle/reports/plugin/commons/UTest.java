package de.mgpit.oracle.reports.plugin.commons;


import junit.framework.TestCase;

public class UTest extends TestCase {

    protected void setUp() throws Exception {
        super.setUp();
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

    protected void tearDown() throws Exception {
        super.tearDown();
    }

}
