package de.mgpit.oracle.reports.plugin.destination.mq;


import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

}
