package de.mgpit.types;


import java.util.HashMap;
import java.util.Map;

/**
 * 
 * @author mgp
 *
 *         Class for working with boolean as Strings.
 *         {@link Boolean#valueOf(String)} is only able to work with the String literal "true", this utility can also interpret
 *         the literals "yes", "on", "1" as {@code true} value.
 *
 */
public class StringCodedBoolean {

    /**
     * Returns a boolean value based on the string given. The Boolean returned will be {@code true}
     * if the string argument is not null and is equal, ignoring case, to one of the strings "true", "yes", "on", "1".
     * <p>
     * Examples
     * 
     * <pre>
     * {@code
     * StringCodedBoolean.valueOf("True") returns true.
     * StringCodedBoolean.valueOf("yes") returns true.
     * StringCodedBoolean.valueOf("ON") returns true.
     * StringCodedBoolean.valueOf("no") returns false.
     * StringCodedBoolean.valueOf("") returns false.
     * StringCodedBoolean.valueOf("Lorem Ipsum Dolor Si Amet") returns false.}
     * </pre>
     * 
     * @param stringWithBooleanMeaning
     *            a String
     * @return the Boolean value represented by the String
     */
    public static boolean valueOf( final String stringWithBooleanMeaning ) {
        final Boolean b = (Boolean) stringsMeaningTrue.get( stringWithBooleanMeaning.toLowerCase() );
        return (b == null ? false : b.booleanValue());
    }

    private StringCodedBoolean() {
        //
    }

    /**
     * Holds the strings which will be interpreted as {@code true}.
     * Contains their lowercase representation, only. {@ling #valueOf} will deal with different cases.
     */
    private static final Map stringsMeaningTrue;
    static {
        stringsMeaningTrue = new HashMap();
        stringsMeaningTrue.put( "true", Boolean.TRUE );
        stringsMeaningTrue.put( "yes", Boolean.TRUE );
        stringsMeaningTrue.put( "on", Boolean.TRUE );
        stringsMeaningTrue.put( "1", Boolean.TRUE );
    }

}
