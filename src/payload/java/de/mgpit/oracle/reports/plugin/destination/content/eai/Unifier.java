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
package de.mgpit.oracle.reports.plugin.destination.content.eai;


import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * A Unifier.
 * <p>
 * The Unifier is a pseudo unique id for identifying EAI messages.
 * A Unifier is composed (and limited by the framework) of 20 bytes an follows the format
 * 
 * <pre>
 *  {@code yyyyMMddhhmmssSSSnnn}
 * </pre>
 * 
 * where
 * <ul>
 * <li>{@code yyyyMMddhhmmssSSS} is a {@link SimpleDateFormat} and</li>
 * <li>{@code nnn} a left padded integer in the range between {@code 000 .. 999}</li>
 * </ul>
 * Hence, to have <em>unique</em> unifiers one cannot draw more than 1000 unifiers per millisecond.
 * 
 * @author mgp
 *
 */
public class Unifier {
    private static final SimpleDateFormat UNIFIER_PREFIX_FORMAT = new SimpleDateFormat( "yyyyMMddhhmmssSSS" );
    private static final DecimalFormat UNIFIER_SUFFIX_FORMAT = new DecimalFormat( "000" );
    private static int UNIFIER_COUNTER = 0;
    private static String last = null;

    private static Unifier lock = new Unifier();

    /**
     * Gets a new Unifier.
     * 
     * @return a new Unifier
     */
    public static String next() {
        synchronized (lock) {
            final Date now = Calendar.getInstance().getTime();
            final String unifierPrefix = UNIFIER_PREFIX_FORMAT.format( now );
            final String unifierSuffix = UNIFIER_SUFFIX_FORMAT.format( (double) UNIFIER_COUNTER );

            UNIFIER_COUNTER = (UNIFIER_COUNTER < 999) ? UNIFIER_COUNTER + 1 : 0;

            last = unifierPrefix.concat( unifierSuffix );
            return last;
        }
    }

    public static String last() {
        synchronized(lock) {
            return last;
        }
    }

    private Unifier() {}
}
