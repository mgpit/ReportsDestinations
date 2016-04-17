package de.mgpit.oracle.reports.plugin.commons;

import java.util.HashMap;
import java.util.Map;

/**
 * 
 * @author mgp
 * <p>
 * Class for working with boolean as Strings.
 * {@link Boolean#valueOf(String)} is only able to work with the String literal "true", this utility can also interpret 
 * the literals "yes", "on", "1" as {@code true} value.
 *
 */
public class StringCodedBoolean {
    private static Map stringsMeaningTrue;
    /**
     * Returns a Boolean with a value represented by the specified String. The Boolean returned represents the value true 
     * if the string argument is not null and is equal, ignoring case, to one of the strings "true", "yes", "on", "1".
     * <p>
     * Examples
     * <pre>
     * StringCodedBoolean.valueOf("True") returns true.
     * StringCodedBoolean.valueOf("yes") returns true.
     * StringCodedBoolean.valueOf("ON") returns true.
     * StringCodedBoolean.valueOf("no") returns false.
     * StringCodedBoolean.valueOf("") returns false.
     * StringCodedBoolean.valueOf("Lorem Ipsum Dolor Si Amet") returns false.
     * </pre>
     * 
     * @param stringWithBooleanMeaning a String
     * @return the Boolean value represented by the String
     */
    
    public static boolean valueOf( String stringWithBooleanMeaning ){
        Boolean b = (Boolean)stringsMeaningTrue.get( stringWithBooleanMeaning.toLowerCase() );
        
        return (b==null?false:b.booleanValue());
    }
    private StringCodedBoolean(){
        //
    }
    
    static {
        stringsMeaningTrue = new HashMap();
        stringsMeaningTrue.put( "true", Boolean.TRUE );
        stringsMeaningTrue.put( "yes", Boolean.TRUE );
        stringsMeaningTrue.put( "on", Boolean.TRUE );
        stringsMeaningTrue.put( "1", Boolean.TRUE );
    }
    
    
}
