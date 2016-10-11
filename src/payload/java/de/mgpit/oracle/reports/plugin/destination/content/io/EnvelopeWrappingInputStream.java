package de.mgpit.oracle.reports.plugin.destination.content.io;


import java.io.IOException;
import java.io.InputStream;

import de.mgpit.oracle.reports.plugin.commons.Magic;
import de.mgpit.oracle.reports.plugin.commons.U;
import de.mgpit.oracle.reports.plugin.destination.content.EnvelopeInput;

public class EnvelopeWrappingInputStream extends InputStream {

    /**
     * Input to be wrapped.
     */
    private InputStream input;
    /**
     * Envelope for wrapping.
     */
    private EnvelopeInput envelope;

    public EnvelopeWrappingInputStream( InputStream input, EnvelopeInput envelope ) {
        U.assertNotNull( input, "Cannot wrap a null InputStream!" );
        U.assertNotNull( envelope, "Cannot wrap with a null Envelope!" );
        this.input = input;
        this.envelope = envelope;
    }

    public int read() throws IOException {
        int aByte;
        if ( envelope.dataWanted() ) {
            aByte = input.read();
            if ( aByte == Magic.END_OF_STREAM ) {
                envelope.dataFinished();
                return this.read();
            }
        } else {
            aByte = envelope.read();
        }
        return aByte;
    }

    public synchronized int available() throws IOException {
        return envelope.dataWanted() ? input.available() : 0;
    }
}
