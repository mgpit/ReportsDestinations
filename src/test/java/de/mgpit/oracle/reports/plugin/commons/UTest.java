package de.mgpit.oracle.reports.plugin.commons;


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

}
