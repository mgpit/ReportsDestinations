package de.mgpit.oracle.reports.plugin.commons;


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
     * 
     * @param s
     * @return the String wrapped in square brackets
     */
    public static String w( String s ) {
        StringBuffer wrapped = new StringBuffer( s.length() + 2 );
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
}
