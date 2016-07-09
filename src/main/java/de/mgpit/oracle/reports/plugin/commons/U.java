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
    public static String w( final String str ) {
        final StringBuffer wrapped = new StringBuffer( ((str == null) ? 0 : str.length()) + 2 );
        wrapped.append( "[" ).append( str ).append( "]" );
        return wrapped.toString();
    }

    public static String w( final Object o ) {
        return w( o.toString() );
    }

    public static String w( final short sh ) {
        return w( "" + sh );
    }

    public static String w( final int i ) {
        return w( "" + i );
    }

    public static String w( final long lng ) {
        return w( "" + lng );
    }

    public static String pad( final String str, final int len, final boolean append ) {
        int currentLength = str.length();
        int delta = len - currentLength;
        boolean prepend = !append;

        String padded;
        if ( delta < 0 ) {
            padded = str.substring( 0, len );
        } else {
            StringBuffer sb = new StringBuffer( len );
            if ( prepend ) {
                sb.append( str );
            }
            for ( int i = 0; i < delta; i++ ) {
                sb.append( ' ' );
            }
            if ( append ) {
                sb.append( str );
            }
            padded = sb.toString();
        }
        return padded;
    }

    public static String rpad( final String str, final int len ) {
        return pad( str, len, true );
    }

    public static String rpad( final int i, final int len ) {
        return rpad( String.valueOf( i ), len );
    }
    
    public static String lpad( final String str, final int len ) {
        return pad( str, len, false );
    }

    public static String lpad( final int i, final int len ) {
        return lpad( String.valueOf( i ), len );
    }


    /**
     * 
     * @param s
     *            a String
     * @return {@code true} if the String str is null or them empty String "".
     */
    public static boolean isEmpty( final String str ) {
        return (str == null) || "".equals( str );
    }

    public static boolean eq( final String str1, final String str2 ) {
        if ( str1 == null ) {
            return str2 == null;
        }
        return str1.equals( str2 );
    }

    /* Some assertion stuff ... :-) */

    public static void assertNotNull( final Object o ) throws AssertionError {
        assertNotNull( o, o.getClass().getName() + " MUST NOT be null!" );
    }

    public static void assertNotNull( final Object o, final String message ) throws AssertionError {
        if ( o == null ) {
            throw new AssertionError( new NullPointerException( message ) );
        }
    }

    public static void assertNotEmpty( final String str ) throws AssertionError {
        assertNotEmpty( str, "String MUST NOT be null oder empty!" );
    }

    public static void assertNotEmpty( final String str, final String message ) throws AssertionError {
        if ( isEmpty( str ) ) {
            throw new AssertionError( new IllegalArgumentException( message ) );
        }
    }

    public static void assertTrue( final boolean b ) throws AssertionError {
        assertTrue( b, "Condition NOT met!" );
    }

    public static void assertTrue( final boolean b, final String message ) throws AssertionError {
        if ( !b ) {
            throw new AssertionError( message, new Exception( "Expression or variable does not evaluate to true!" ) );
        }
    }

    public static final class Rw {
        public static void assertNotEmpty( final String str ) throws RWException {
            new U.RwExceptionWrappedAssertion() {

                public void test() throws Exception {
                    U.assertNotEmpty( str );
                }
            }.evaluate();
        }

        public static void assertNotEmpty( final String str, final String message ) throws RWException {
            new U.RwExceptionWrappedAssertion() {

                protected void test() throws Exception {
                    U.assertNotEmpty( str, message );
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

    private static abstract class RwExceptionWrappedAssertion {
        public final void evaluate() throws RWException {
            try {
                test();
            } catch ( Throwable t ) {
                throw Utility.newRWException( new Exception( t.getCause() ) );
            }
        }

        protected abstract void test() throws Exception;
    }

    /**
     * We have to Modernize ... No AssertionError in Java 1.4
     * 
     * @author mgp
     *
     */
    private static class AssertionError extends Error {
        public AssertionError( String message, Throwable cause ) {
            super( message, cause );
        }

        public AssertionError( Throwable cause ) {
            super( cause );
        }

    }
}
