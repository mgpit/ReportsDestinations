package de.mgpit.oracle.reports.plugin.destination.content;

import java.io.OutputStream;

public interface Envelope {

    public boolean dataWanted();

    public int read();
    
    public void writeToOut( OutputStream out );

    public void setDataFinished();

}
