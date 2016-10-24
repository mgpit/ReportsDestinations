/*
 * Copyright 2016 Marco Pauls www.mgp-it.de
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

/**
 * @license APACHE-2.0
 */
package de.mgpit.types;


import java.util.HashMap;
import java.util.Map;

/**
 * 
 * A String Representation for Booleans.
 * <p>
 * Class {@code Boolean} (see {@link Boolean#valueOf(String)}) is only able to work with the String literal "true". 
 * {@code StringCodedBoolean} can also interpret the literals "yes", "on", "1" as {@code true} value.
 * <p>
 * For simplicity: All Strings which cannot be interpreted as a literal for the value {@code true} will be
 * interpreted as {@code false}.
 * 
 * @author mgp
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
