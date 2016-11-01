package de.mgpit.oracle.reports.plugin.destination.mq;


import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.mgpit.oracle.reports.plugin.commons.U;
import de.mgpit.types.ModifierRawDeclaration;
import junit.framework.TestCase;

public class MQDestinationTest extends TestCase {

    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testRegexp() {
        final String first2lastExpression = "^([^><]+>>)*([^><])+$";
        final String last2firstExpression = "^([^><]+<<)*([^><])+$";

        final Pattern first2last = Pattern.compile( first2lastExpression );
        final Pattern last2first = Pattern.compile( last2firstExpression );

        final String first = "Foo>>Bar>>Batz";
        final String second = "Fizz<<Buzz";
        final String third = "Lorem";
        final String fourth = "";

        Matcher f2l_first = first2last.matcher( first );
        assertTrue( f2l_first.matches() );

        Matcher f2l_second = first2last.matcher( second );
        assertFalse( f2l_second.matches() );

        Matcher f2l_third = first2last.matcher( third );
        assertTrue( f2l_third.matches() );

        Matcher f2l_fourth = first2last.matcher( fourth );
        assertFalse( f2l_fourth.matches() );

        Matcher l2f_first = last2first.matcher( first );
        assertFalse( l2f_first.matches() );

        Matcher l2f_second = last2first.matcher( second );
        assertTrue( l2f_second.matches() );

        Matcher l2f_third = last2first.matcher( third );
        assertTrue( l2f_third.matches() );

        Matcher l2f_fourth = last2first.matcher( fourth );
        assertFalse( l2f_fourth.matches() );

    }
    
    private String nullOrValue( String s ) {
        return (s==null)?"<null>":U.w( s );
    }
    private void commonAsserts( Matcher m ) {
        final int EXPECTED_NUMBER_OF_GROUPS = 4;
        assertEquals( EXPECTED_NUMBER_OF_GROUPS, m.groupCount() );
        System.out.println( "--------------------" );
        System.out.println( "0: " + nullOrValue(m.group( 0 ) ) );
        System.out.println( "1: " + nullOrValue( m.group( 1 ) ) );
        System.out.println( "2: " + nullOrValue( m.group( 2 ) ) );
        System.out.println( "3: " + nullOrValue( m.group( 3 ) ) );
        System.out.println( "4: " + nullOrValue( m.group( 4 ) ) );
    }

    public void testRegexp2() {
        final Pattern P = Pattern.compile( ModifierRawDeclaration.PATTERN );
        

        Matcher m;

        System.out.println( P.pattern() );
        
//        m = P.matcher(  null  );
//        assertFalse( m.matches() );
        
        m = P.matcher( "" );
        assertFalse( m.matches() );

        m = P.matcher( "1DIMENSION" );
        assertFalse( m.matches() );
        

        m = P.matcher( "A" );
        assertTrue( m.matches() );
        commonAsserts( m );

        m = P.matcher( "NAME" );
        assertTrue( m.matches() );
        commonAsserts( m );

        m = P.matcher( "NAME1" );
        assertTrue( m.matches() );
        commonAsserts( m );

        m = P.matcher( "NAME1( FOO )" );
        assertTrue( m.matches() );
        commonAsserts( m );
        assertEquals( "NAME1", m.group( 2 ) );
        assertEquals( "FOO", m.group( 4 ) );

        m = P.matcher( "NAME1( CDM:KUNDENOUTPUT )" );
        assertTrue( m.matches() );
        commonAsserts( m );
        
        m = P.matcher( "NAME1( CDM.KUNDENOUTPUT )" );
        assertTrue( m.matches() );
        commonAsserts( m );
        
        
        m = P.matcher( "NAME1( CDM-KUNDENOUTPUT )" );
        assertTrue( m.matches() );
        commonAsserts( m );
        
        m = P.matcher( " NAME1      ( CDM-KUNDENOUTPUT     )" );
        assertTrue( m.matches() );
        commonAsserts( m );
        
        m = P.matcher( "  NAME1( CDM-KUNDENOUTPUT )" );
        assertTrue( m.matches() );
        commonAsserts( m );
        
        m = P.matcher( "NAME1( CDM-KUNDENOUTPUT ) " );
        assertTrue( m.matches() );
        commonAsserts( m );
        
        m = P.matcher( "NAME1( CDM-KUNDENOUTPUT )  " );
        assertTrue( m.matches() );
        commonAsserts( m );
        
        m = P.matcher( " NAME1( CDM-KUNDENOUTPUT ) " );
        assertTrue( m.matches() );
        commonAsserts( m );

    }

}
