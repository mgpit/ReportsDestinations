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
 *         Provider of a bunch of of utility methods, for example methods for
 *          <ul>
 *              <li>handling class and package names</li>
 *              <li>decorating strings and numbers with square brackets</li>
 *              <li>padding strings and numbers</li>
 *              <li>testing strings and arrays for emptiness</li>
 *              <li>assertions</li>
 *          </ul>
 */
public final class U {

    /**
     * Returns the classes class name, only. Similar to Class.getSimpleName()
     * 
     * @param clazz
     *            the class
     * @return the classes class name
     */
    public static final String classname( Class clazz ) {
        assertNotNull( clazz );
        return classname( clazz.getName() );
    }

    public static final String classname( String maybeFullClassName ) {
        assertNotEmpty( maybeFullClassName );
        int lastDotPosition = maybeFullClassName.lastIndexOf( '.' );
        return (lastDotPosition == Magic.CHARACTER_NOT_FOUND) ? maybeFullClassName
                : maybeFullClassName.substring( lastDotPosition + 1 );
    }

    public static final String packagename( Class clazz ) {
        assertNotNull( clazz );
        return packagename( clazz.getName() );
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

    /**
     * Wraps the String representation of an object in square brackets
     * 
     * @param o
     *            Object to be wrapped
     * @return the string representation of the object wrapped in square brackets
     */
    public static String w( final Object o ) {
        return w( (o == null) ? "<null object>" : o.toString() );
    }

    /**
     * Wraps the string representation of the short in square bracktes
     * 
     * @param sh
     *            short to be wrapped
     * @return the string representation of the short wrapped in square brackets
     */
    public static String w( final short sh ) {
        return w( "" + sh );
    }

    /**
     * Wraps the string representation of the int in square bracktes
     * 
     * @param i
     *            int to be wrapped
     * @return the string representation of the int wrapped in square brackets
     */
    public static String w( final int i ) {
        return w( "" + i );
    }

    /**
     * Wraps the string representation of the long in square bracktes
     * 
     * @param lng
     *            long to be wrapped
     * @return the string representation of the short wrapped in square brackets
     */
    public static String w( final long lng ) {
        return w( "" + lng );
    }

    private static final String PW = "**********";

    /**
     * Obfuscate the password of a "username/password@somehost" connect string with stars
     * 
     * @param connectString
     * @return the connect string with the obfuscated password
     */
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

    /**
     * Checks if the string provided starts with the {@link Magic#BASE64_MARKER}
     * 
     * @param str
     *            string to be checked
     * @return true if the string provided starts with {@link Magic#BASE64_MARKER} else false
     */
    public static boolean isBase64Marked( String str ) {
        if ( str == null ) {
            return false;
        }
        return str.startsWith( Magic.BASE64_MARKER );
    }

    /**
     * Check if the string given is marked as BASE64 encoded and return the decoded string.
     * <p>
     * A string is supposed to be BASE64 encoded if it starts with prefix {@link Magic#BASE64_MARKER}
     * 
     * @param str
     *            to be eventually decoded
     * @return the decoded string
     */
    public static String decodeIfBase64( String str ) {
        if ( !isBase64Marked( str ) ) {
            return str;
        }
        String encoded = str.substring( Magic.BASE64_MARKER.length() );

        String decoded = (Base64.isBase64( encoded )) ? StringUtils.newStringUtf8( Base64.decodeBase64( encoded ) ) : encoded;
        return decoded;
    }

    /**
     * Left or right pad a string with a padding character to a given target length.
     * <p>
     * If the string's length is greater than the target length no padding occurs and the string itself will be returned.
     * 
     * @param str
     *            String to be padded
     * @param len
     *            target/padding length
     * @param pad
     *            padding character
     * @param appendPadding
     *            flag if the string should be right padded ({@code true}) or left padded ({@code false})
     * @return the string left or right padded with pad characters
     * 
     */
    public static String pad( final String str, final int len, char pad, final boolean appendPadding ) {
        int currentLength = str.length();
        int delta = len - currentLength;
        boolean prependPadding = !appendPadding;

        String padded;
        if ( delta < 0 ) {
            padded = str; // .substring( 0, len );
        } else {
            StringBuffer sb = new StringBuffer( len );
            if ( appendPadding ) {
                sb.append( str );
            }
            for ( int i = 0; i < delta; i++ ) {
                sb.append( pad );
            }
            if ( prependPadding ) {
                sb.append( str );
            }
            padded = sb.toString();
        }
        return padded;
    }

    /**
     * Left or right pad a string to a given target length with space character.
     * <p>
     * If the string's length is greater than the target length no padding occurs and the string itself will be returned.
     * 
     * @param str
     *            String to be padded
     * @param len
     *            target/padding length
     * @param appendPadding
     *            flag if the string should be right padded ({@code true}) or left padded ({@code false})
     * @return the string left or right padded with space characters
     */
    public static String pad( final String str, final int len, final boolean appendPadding ) {
        return pad( str, len, ' ', appendPadding );
    }

    /**
     * Right pad the given string with space character to given length.
     * <p>
     * If the string's length is greater than the target length no padding occurs and the string itself will be returned.
     * 
     * @param str
     *            String to be padded
     * @param len
     *            target/padding length
     * @return the string right padded with space characters
     */
    public static String rpad( final String str, final int len ) {
        return pad( str, len, true );
    }

    /**
     * Right pad a given integer with space character to given length.
     * <p>
     * If the string's length is greater than the target length no padding occurs and the string itself will be returned.
     * 
     * @param i
     *            integer to be padded
     * @param len
     *            target/padding length
     * @return a string containing the integer right padded with space characters
     */
    public static String rpad( final int i, final int len ) {
        return rpad( String.valueOf( i ), len );
    }

    /**
     * Left pad the given string with space character to given length.
     * <p>
     * If the string's length is greater than the target length no padding occurs and the string itself will be returned.
     * 
     * @param str
     *            String to be padded
     * @param len
     *            target/padding length
     * @return the string left padded with space characters
     */
    public static String lpad( final String str, final int len ) {
        return pad( str, len, false );
    }

    /**
     * Left pad a given integer with space character to a given length.
     * <p>
     * If the string's length is greater than the target length no padding occurs and the string itself will be returned.
     * 
     * @param i
     *            integer to be padded
     * @param len
     *            target/padding length
     * @return a string containing the integer left padded with space characters
     */
    public static String lpad( final int i, final int len ) {
        return lpad( String.valueOf( i ), len );
    }

    /**
     * Right pad the given string with space character to given length.
     * <p>
     * If the string's length is greater than the target length no padding occurs and the string itself will be returned.
     * 
     * @param str
     *            String to be padded
     * @param len
     *            target/padding length
     * @param pad
     *            padding character
     * @return the string right padded with the given pad character
     */
    public static String rpad( final String str, final int len, char pad ) {
        return pad( str, len, pad, true );
    }

    /**
     * Right pad a given integer with a pad character to a given length.
     * <p>
     * If the length of the string representing the integer is greater than the target length no padding occurs and the string itself will be returned.
     * 
     * @param i
     *            integer to be padded
     * @param len
     *            target/padding length
     * @param pad
     *            padding character
     * @return a string containing the integer right padded with the given pad character
     */
    public static String rpad( final int i, final int len, char pad ) {
        return rpad( String.valueOf( i ), len, pad );
    }

    /**
     * Left pad the given string with space character to given length.
     * <p>
     * If the string's length is greater than the target length no padding occurs and the string itself will be returned.
     * 
     * @param str
     *            String to be padded
     * @param len
     *            target/padding length
     * @param pad
     *            padding character
     * @return the string left padded with the given pad character
     */
    public static String lpad( final String str, final int len, char pad ) {
        return pad( str, len, pad, false );
    }

    /**
     * Left pad a given integer with a pad character to a given length.
     * <p>
     * If the length of the string representing the integer is greater than the target length no padding occurs and the string itself will be returned.
     * 
     * @param i
     *            integer to be padded
     * @param len
     *            target/padding length
     * @param pad
     * @return a string containing the integer left padded with the given pad character
     */
    public static String lpad( final int i, final int len, char pad ) {
        return lpad( String.valueOf( i ), len, pad );
    }

    /**
     * Checks for null or empty strings.
     * <p>
     * A string is considered to be empty if <code>"".equals( str )</code> yields {@code true}
     * 
     * @param s
     *            the string to be checked
     * @return {@code true} if the String str is null or emptpy, {@code false} else
     */
    public static boolean isEmpty( final String str ) {
        return (str == null) || "".equals( str );
    }

    /**
     * Checks for null or empty string arrays.
     * <p>
     * The array is considered empty if it's length is zero.
     * 
     * @param strings
     *            the string array to be checked
     * @return @{code true} if the array is null or empty, {@code false} else
     */
    public static boolean isEmpty( final String[] strings ) {
        return (strings == null) || strings.length == 0;
    }

    /**
     * Convenience method for handling {@code null} cases. Checks two strings for equality.
     * <p>
     * Two strings are considered to be equal if both are {@code null} or
     * <code>str1.equals( str2 )</code> yields {@code true}. (commutativity applies!).
     * <p>
     * 
     * @param str1
     *            first string to be checked
     * @param str2
     *            second string to be checked
     * @return @{code true} if the two strings are equal, {@code false} else
     */
    public static boolean eq( final String str1, final String str2 ) {
        if ( str1 == null ) {
            return str2 == null;
        }
        return str1.equals( str2 );
    }

    /* Some assertion stuff ... :-) */

    /**
     * Assert that the object given is not the {@code null} value
     * 
     * @param o
     *            object to be tested
     * @throws AssertionError
     *             if the object given is {@code null}
     */
    public static void assertNotNull( final Object o ) throws AssertionError {
        assertNotNull( o, o.getClass().getName() + " MUST NOT be null!" );
    }

    /**
     * Assert that the object given is not the {@code null} value
     * 
     * @param o
     *            object to be tested
     * @param message
     *            error message to be provided if the assertion fails
     * @throws AssertionError
     *             if the object given is {@code null}
     */
    public static void assertNotNull( final Object o, final String message ) throws AssertionError {
        if ( o == null ) {
            throw new AssertionError( new NullPointerException( message ) );
        }
    }

    /**
     * Assert that the string given is not empty.
     * <p>
     * A string is considered to be empty if <code>"".equals( str )</code> yields {@code true}
     * 
     * @param str
     *            string to be tested
     * @throws AssertionError
     *             if the string given is empty
     */
    public static void assertNotEmpty( final String str ) throws AssertionError {
        assertNotEmpty( str, "String MUST NOT be null oder empty!" );
    }

    /**
     * Assert that the string given is not empty.
     * <p>
     * A string is considered to be empty if <code>"".equals( str )</code> yields {@code true}
     * 
     * @param str
     *            string to be tested
     * @param message
     *            error message to be provided if the assertion fails
     * @throws AssertionError
     *             if the string given is empty
     */
    public static void assertNotEmpty( final String str, final String message ) throws AssertionError {
        if ( isEmpty( str ) ) {
            throw new AssertionError( new IllegalArgumentException( message ) );
        }
    }

    /**
     * Assert that the given boolean is {@code true}.
     * <p>
     * The use case for this is of course to make assertions on boolean expressions / methods returning a boolean value as in
     * <pre>{@code
     *  U.assertTrue( employee.isASalesPerson() );
     * }</pre> or <pre>{@code
     *  private lookupSomebody( String name ) {
     *      U.assertTrue( !U.isEmpty( name ), "Must provide a non empty name!" );
     *      ...
     *  }
     * }</pre>
     * 
     * @param b
     *            boolean to be tested
     * @throws AssertionError
     *             if the boolean given is {@code false}
     */
    public static void assertTrue( final boolean b ) throws AssertionError {
        assertTrue( b, "Condition NOT met!" );
    }

    public static void assertTrue( final boolean b, final String message ) throws AssertionError {
        if ( !b ) {
            throw new AssertionError( message, new Exception( "Expression or variable does not evaluate to true!" ) );
        }
    }

    /**
     * This class implements {@link U}'s assertion methods which will wrap {@link AssertionError}s as {@link RWException}s.
     * <p>
     * This will avoid polluting your code with
     * <pre>{@code
     * try {
     *     U.assertXXX( ... );
     * } catch ( AssertionError error ) {
     *     throw Utility.newRWException( error ); 
     * }}</pre> snippets.
     *  
     * @author mgp
     *
     */
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
