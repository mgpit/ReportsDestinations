package de.mgpit.oracle.reports.plugin.commons;


import oracle.reports.RWException;
import oracle.reports.utility.Utility;

/**
 * 
 * @author mgp
 *         <p>
 *         This class provides some static utility methods:
 *         <ul>
 *         <li>{@link #w(int)}, {@link #w(long}, {@link #w(Object)}, {@link #w(short)}, and {@link #w(String)}
 *         will return a String representation of their parameter wrapped in square brackets.</li>
 *         <li>{@link #isEmpty(String)} will check if the String provided is null or the empty String</li>
 *         </ul>
 *
 */
public final class U {

    /**
     * Wraps a string in square brackets.
     * 
     * @param s
     *            String to be wrapped
     * @return the String wrapped in square brackets
     */
    public static String w( String s ) {
        StringBuffer wrapped = new StringBuffer( ((s == null) ? 0 : s.length()) + 2 );
        wrapped.append( "[" ).append( s ).append( "]" );
        return wrapped.toString();
    }

    public static String w( Object o ) {
        return w( o.toString() );
    }

    public static String w( short sh ) {
        return w( "" + sh );
    }

    public static String w( int i ) {
        return w( "" + i );
    }

    public static String w( long lng ) {
        return w( "" + lng );
    }

    /**
     * 
     * @param s
     *            a String
     * @return {@code true} if the String s is null or them empty String "".
     */
    public static boolean isEmpty( String s ) {
        return (s == null) || "".equals( s );
    }

    public static boolean eq( String s1, String s2 ) {
        if ( s1 == null ) {
            return s2 == null;
        }
        return s1.equals( s2 );
    }

    /* Some assertion stuff ... :-) */

    public static void assertNotNull( Object o ) throws AssertionError {
        assertNotNull( o, o.getClass().getName() + " MUST NOT be null!" );
    }

    public static void assertNotNull( Object o, String message ) throws AssertionError {
        if ( o == null ) {
            throw new AssertionError( new NullPointerException( message ) );
        }
    }

    public static void assertNotEmpty( String s ) throws AssertionError {
        assertNotEmpty( s, "String MUST NOT be null oder empty!" );
    }

    public static void assertNotEmpty( String s, String message ) throws AssertionError {
        if ( isEmpty( s ) ) {
            throw new AssertionError( new IllegalArgumentException( message ) );
        }
    }

    public static void assertTrue( boolean b ) throws AssertionError {
        assertTrue( b, "Condition NOT met!" );
    }

    public static void assertTrue( boolean b, String message ) throws AssertionError {
        if ( !b ) {
            throw new AssertionError( message, new Exception( "Expression or variable does not evaluate to true!" ) );
        }
    }

    public static final class Rw {
        public static void assertNotEmpty( final String s ) throws RWException {
            new U.RwExceptionWrappedAssertion() {

                public void test() throws Exception {
                    U.assertNotEmpty( s );
                }
            }.evaluate();
        }
        
        public static void assertNotEmpty( final String s, final String message ) throws RWException {
            new U.RwExceptionWrappedAssertion() {
                
                protected void test() throws Exception {
                   U.assertNotEmpty( s, message ); 
                }
            }.evaluate();
        }
        
        public static void assertNotNull( final Object o ) throws RWException {
            new U.RwExceptionWrappedAssertion() {
                
                protected void test() throws Exception {
                    U.assertNotNull( o );
                }
            }.evaluate();
        }
        
        public static void assertNotNull( final Object o, final String message ) throws RWException {
            new U.RwExceptionWrappedAssertion() {
                
                protected void test() throws Exception {
                    U.assertNotNull( o, message );
                }
            }.evaluate();
        }
    }

    static abstract class RwExceptionWrappedAssertion {
        public final void evaluate() throws RWException {
            try {
                test();
            } catch ( Throwable t ) {
                throw Utility.newRWException( new Exception( t.getCause() ) );
            }
        }

        protected abstract void test() throws Exception;
    }

}
