package de.mgpit.oracle.reports.plugin.destination.content.types;

import java.io.IOException;
import java.io.OutputStream;

public interface Envelope extends Content {

    public boolean dataWanted();

    public void setDataFinished();

}
