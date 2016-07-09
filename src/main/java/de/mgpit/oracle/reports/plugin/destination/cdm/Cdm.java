package de.mgpit.oracle.reports.plugin.destination.cdm;


public interface Cdm {

    public boolean dataWanted();

    public int read();

    public void dataFinished();

}
