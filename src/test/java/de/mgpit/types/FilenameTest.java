package de.mgpit.types;


import de.mgpit.oracle.reports.plugin.commons.U;
import junit.framework.TestCase;

public class FilenameTest extends TestCase {

    public void setUp() throws Exception {}

    public void tearDown() throws Exception {}

    public void testOf() {
        final String fullFileName = "bar.tmp.txt";
        Filename filename = Filename.of( fullFileName );

        assertEquals( filename.toString(), fullFileName );
    }
    
    public void testWithExtension( ){
        final String fullFilename = "C:\\tmp\\reports\\bar.zip";
        final String expectedFullFilename = "C:\\tmp\\reports\\bar.part";
        
        Filename filename = Filename.of( fullFilename );
        Filename expected = Filename.of( expectedFullFilename );
        Filename actual = filename.withNewExtension( ".part" );
        
        // System.out.println( filename.toString() );
        // System.out.println( expected.toString() );
        // System.out.println( actual.toString() );
        
        assertEquals( expectedFullFilename, actual.toString() );
        
        assertEquals( expected, actual );

        
    }
    
    public void testEquals() {
        final String fullFilename = "C:\\tmp\\reports\\bar.zip";
        
        Filename filename = Filename.of( fullFilename );
        Filename other = Filename.of( fullFilename );
        Filename third = Filename.of( fullFilename );
        
        assertEquals( other, filename );
        assertEquals( filename, other ); // commutativity
        assertEquals( other, third );
        assertEquals( filename, third ); // transitivity
        assertEquals( filename, filename ); // reflexivity
        assertEquals( other, other ); // reflexivity
    }
    
    public void testCompareTo() {
        final String fullFilename = "C:\\tmp\\reports\\bar.zip";
        
        Filename filename = Filename.of( fullFilename );
        Filename other = Filename.of( fullFilename );
        
        assertTrue( "Doesn't compare to equal!", filename.compareTo( other ) == 0 );
        assertTrue( "Doesn't compare to equal!", other.compareTo( filename ) == 0 );
        assertTrue( "Doesn't compare to equal!", filename.compareTo( filename ) == 0 );
        
        
        Filename less = Filename.of( "C:\\tmp\\reports\\aaa.zip" );
        Filename greater = Filename.of( "C:\\tmp\\reports\\zzz.zip" );
        
        assertTrue( other.compareTo( less ) > 0 );
        assertTrue( other.compareTo( greater ) < 0 );
        
        assertTrue( less.compareTo( greater ) < 0 );
        assertTrue( greater.compareTo( less ) > 0 );
        
        assertEquals( other.compareTo( other ), 0 );
    }
    
    public void testCopy() {
        Filename expected = Filename.of( "C:\\tmp\\reports\\zzz.zip" );
        Filename actual = expected.copy();
        
        assertEquals( actual, expected );
        assertFalse( actual == expected );
        assertTrue( actual != expected );
    }
}
