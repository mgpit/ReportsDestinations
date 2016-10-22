package de.mgpit.oracle.reports.plugin.destination.content.types;

import java.io.IOException;
import java.io.OutputStream;

public interface Content {
    public static final String PROPERTY_NAME_PREFIX = "content.";
    
    public int read();
    
    public void writeToOut( OutputStream out ) throws IOException;


}
