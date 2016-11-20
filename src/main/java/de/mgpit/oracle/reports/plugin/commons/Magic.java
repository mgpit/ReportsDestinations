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
package de.mgpit.oracle.reports.plugin.commons;


/**
 * 
 * 
 * Magic numbers provider.
 * <p>
 * This class defines static constants giving names to numeric, string or boolean values which are used
 * with a special meaning in other classes and/or methods.
 *
 * @author mgp
 *
 */
public class Magic {
    // Streams magic numbers
    /**
     * Holds the magic number inidcation that the end of the stream has been reached.
     */
    public static final int END_OF_STREAM = -1;

    // String / Character magic numbers
    /**
     * Holds the magic number indicating that a given character could not be found within a string.
     */
    public static final int CHARACTER_NOT_FOUND = -1;
    
    /**
     * Holds the magic number indicating that a given substring could not be found within a string.
     */
    public static final int SUBSTRING_NOT_FOUND = -1;

    // Base64 magic numbers
    /**
     * Holds the boolean flag {@code true} indicating that the BASE64 stream should encode the bytes written/read.
     */
    public static final boolean ENCODE_WITH_BASE64 = true;
    /**
     * Holds the boolean flag {@code false} indicating that the BASE64 stream should decode the bytes written/read.
     */
    public static final boolean DECODE_FROM_BASE64 = false;

    // log4j magic numbers
    /**
     * Holds the boolean state {@code true} indicating that the log messages should be appended to an already existing log file.
     */
    public static final boolean APPEND_MESSAGES_TO_LOGFILE = true;
    /**
     * Holds the boolean state {@code false} indicating that the an already existing log file should be overwritten.
     */
    public static final boolean OVERWRITE_OR_CREATE_LOGFILE = false;
    /**
     * Holds the boolean state {@code true} indicating that the log messages should also be added to
     * the ancestors of the current appender.
     */
    public static final boolean ADD_MESSAGES_TO_ANCESTORS = true;
    /**
     * Holds the boolean state {@code false} indicating that the log messages should <strong>not</strong> be added to
     * the ancestors of the current appender.
     */
    public static final boolean DONT_ADD_MESSAGES_TO_ANCESTORS = false;

    /**
     * Holds the prefix indicating that the subsequent content of the string is BASE64 encoded.
     */
    public static final String BASE64_MARKER = "(BASE64)";
    
    
    /**
     * a Prime ...
     */
    public static final int PRIME = 23;

}
