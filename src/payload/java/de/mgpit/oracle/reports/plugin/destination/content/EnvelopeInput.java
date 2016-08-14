package de.mgpit.oracle.reports.plugin.destination.content;


public interface EnvelopeInput {

    public boolean dataWanted();

    public int read();

    public void dataFinished();

}
