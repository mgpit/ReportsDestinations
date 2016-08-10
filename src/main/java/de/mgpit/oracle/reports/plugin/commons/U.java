package de.mgpit.oracle.reports.plugin.commons;


import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.StringUtils;

import de.mgpit.oracle.reports.plugin.commons.io.IOUtility;
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

    public static final String classname( Class clazz ) {
        assertNotNull( clazz );
        String packageName = packagename( clazz );
        String fullName = clazz.getName();
        return fullName.substring( packageName.length() );
    }

    public static final String classname( String maybeFullClassName ) {
        assertNotEmpty( maybeFullClassName );
        int lastDotPosition = maybeFullClassName.lastIndexOf( '.' );
        return (lastDotPosition == Magic.CHARACTER_NOT_FOUND) ? maybeFullClassName
                : maybeFullClassName.substring( lastDotPosition, +1 );
    }

    public static final String packagename( Class clazz ) {
        assertNotNull( clazz );
        Package thePackage = clazz.getPackage();
        return (thePackage == null) ? "" : thePackage.getName();
    }

    public static final String packagename( String maybeFullClassName ) {
        assertNotEmpty( maybeFullClassName );
        int lastDotPosition = maybeFullClassName.lastIndexOf( '.' );
        return (lastDotPosition == Magic.CHARACTER_NOT_FOUND) ? "" : maybeFullClassName.substring( 0, lastDotPosition );
    }

    public static final Object coalesce( Object optionalObject, Object defaultObject ) {
        return (optionalObject == null) ? defaultObject : optionalObject;
    }

    public static final String coalesce( String optionalString, String defaultString ) {
        return (optionalString == null) ? defaultString : optionalString;
    }

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
        return w( (o == null) ? "<null object>" : o.toString() );
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

    private static final String PW = "**********";

    public static String obfuscate( final String connectString ) {
        if ( connectString == null ) {
            return PW;
        }
        String connectStringRegex = "^\\s*(\\S+?)/(\\S+?)(@(\\S*))?\\s*$";
        Pattern connectStringPattern = Pattern.compile( connectStringRegex );
        Matcher matcher = connectStringPattern.matcher( connectString );

        String obfuscated = PW;
        if ( matcher.find() ) {
            String username = matcher.group( 1 );
            String database = matcher.group( 4 );
            obfuscated = username + "/" + PW + (isEmpty( database ) ? "" : "@".concat( database ));
        }

        return obfuscated;
    }

    public static boolean isBase64Marked( String str ) {
        if ( str == null ) {
            return false;
        }
        return str.startsWith( Magic.BASE64_MARKER );
    }

    public static String decodeIfBase64( String str ) {
        if ( !isBase64Marked( str ) ) {
            return str;
        }
        String encoded = str.substring( Magic.BASE64_MARKER.length() );
        
        String decoded = (Base64.isBase64( encoded )) ? StringUtils.newStringUtf8( Base64.decodeBase64( encoded ) ) : encoded;
        return decoded;
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
