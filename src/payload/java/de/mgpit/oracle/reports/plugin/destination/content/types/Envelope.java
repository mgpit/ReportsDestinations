package de.mgpit.oracle.reports.plugin.destination.content.types;

import java.io.IOException;
import java.io.OutputStream;

public interface Envelope {

    public boolean dataWanted();

    public int read();
    
    public void writeToOut( OutputStream out ) throws IOException;

    public void setDataFinished();

}
