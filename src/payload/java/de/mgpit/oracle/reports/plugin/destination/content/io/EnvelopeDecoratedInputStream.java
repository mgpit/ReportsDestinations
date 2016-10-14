package de.mgpit.oracle.reports.plugin.destination.content.io;


import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

import de.mgpit.oracle.reports.plugin.commons.Magic;
import de.mgpit.oracle.reports.plugin.commons.U;
import de.mgpit.oracle.reports.plugin.destination.content.types.Envelope;

public class EnvelopeDecoratedInputStream extends FilterInputStream {

    /**
     * Envelope for wrapping.
     */
    private final Envelope envelope;

    public EnvelopeDecoratedInputStream( InputStream toBeDecorated, Envelope envelope ) {
        super( toBeDecorated );
        U.assertNotNull( toBeDecorated, "Cannot wrap a null InputStream!" );
        U.assertNotNull( envelope, "Cannot wrap with a null Envelope!" );
        this.envelope = envelope;
    }

    public int read() throws IOException {
        final int aByte;
        if ( envelope.dataWanted() ) {
            aByte = in.read();
            if ( aByte == Magic.END_OF_STREAM ) {
                envelope.setDataFinished();
                return this.read();
            }
        } else {
            aByte = envelope.read();
        }
        return aByte;
    }

    public synchronized int available() throws IOException {
        return envelope.dataWanted() ? in.available() : 0;
    }
}
