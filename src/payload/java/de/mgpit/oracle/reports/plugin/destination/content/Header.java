package de.mgpit.oracle.reports.plugin.destination.content;

import java.io.OutputStream;

public interface Header {

    public boolean dataWanted();

    public int read();
    
    public void writeToOut( OutputStream out );

    public void setDataFinished();

}
