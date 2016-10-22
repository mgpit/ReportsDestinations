package de.mgpit.oracle.reports.plugin.destination.content.types;

import java.io.IOException;
import java.io.OutputStream;


public interface Content {
    public static final String PROPERTY_NAME_PREFIX = "content.";
    
    /**
     * Reads the contents next byte
     * @return byte read or {@link de.mgpit.oracle.reports.plugin.commons.Magic Magic#END_OF_STREAM}
     */
    public int read();
    
    /**
     * 
     * @return {@code true} if the payload should be provided, now,{@code false} else
     */
    public boolean dataWanted();
    
    /**
     * Get the signal that the payload is finished
     */
    public void setDataFinished();
    
    /**
     * Write this to the given output stream.
     * 
     * @param out the output stream
     * @throws IOException if any error occurs
     */
    public void writeToOut( OutputStream out ) throws IOException;


}
