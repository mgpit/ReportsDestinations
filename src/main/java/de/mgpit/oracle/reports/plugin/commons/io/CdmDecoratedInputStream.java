package de.mgpit.oracle.reports.plugin.commons.io;


import java.io.IOException;
import java.io.InputStream;

import de.mgpit.oracle.reports.plugin.commons.Magic;
import de.mgpit.oracle.reports.plugin.commons.U;
import de.mgpit.oracle.reports.plugin.destination.cdm.Cdm;

public class CdmDecoratedInputStream extends InputStream {

    private InputStream cdmPayload;
    private Cdm cdm;

    public CdmDecoratedInputStream( InputStream payload, Cdm cdm ) {
        U.assertNotNull( payload );
        U.assertNotNull( cdm );
        this.cdmPayload = payload;
        this.cdm = cdm;
    }

    public int read() throws IOException {
        int aByte;
        if ( cdm.dataWanted() ) {
            aByte = cdmPayload.read();
            if ( aByte == Magic.END_OF_STREAM ) {
                cdm.dataFinished();
                return this.read();
            }
        } else {
            aByte = cdm.read();
        }
        return aByte;
    }

    public synchronized int available() throws IOException {
        return cdm.dataWanted() ? cdmPayload.available() : 0;
    }
}
