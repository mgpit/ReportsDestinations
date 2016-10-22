package de.mgpit.oracle.reports.plugin.commons;

/**
 * 
 * @author mgp
 * 
 * Magic numbers container.
 * <p>
 * This class defines static constants giving names to numeric, string or boolean values which are used
 * with a special meaning in other classes and/or methods.
 * <p>
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
     * Holds the magig number indicating that a given character could not be found within a string.
     */
    public static final int CHARACTER_NOT_FOUND = -1;
    
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
    
}
