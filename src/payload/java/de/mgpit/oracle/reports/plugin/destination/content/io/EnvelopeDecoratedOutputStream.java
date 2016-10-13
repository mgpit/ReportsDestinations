package de.mgpit.oracle.reports.plugin.destination.content.io;


import java.io.BufferedOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import de.mgpit.oracle.reports.plugin.commons.Magic;
import de.mgpit.oracle.reports.plugin.destination.content.Envelope;

public class EnvelopeDecoratedOutputStream extends FilterOutputStream {

    private final Envelope envelope;

    public EnvelopeDecoratedOutputStream( final OutputStream toWrap, final Envelope envelope ) {
        super( toWrap );
        this.envelope = envelope;
    }

    public void write( int b ) throws IOException {
        if ( envelope.dataWanted() ) {
            out.write( b );
        } else {
            while ( !envelope.dataWanted() ) {
                final int nextByteFromEnvelope = envelope.read();
                if ( nextByteFromEnvelope == Magic.END_OF_STREAM ) {
                    throw new IOException( "Accessinbg envelope beyond end!" );
                }
                out.write( nextByteFromEnvelope );
            }
        }
    }

    public void flush() throws IOException {
        if ( !envelope.dataWanted() ) {
            throw new IOException( "Cannot flush an Envelope which is not in state \"data wanted\"!" );
        }
        envelope.setDataFinished();
        for ( final int nextByteFromEnvelope = envelope.read(); nextByteFromEnvelope != Magic.END_OF_STREAM; ){
           out.write( nextByteFromEnvelope ); 
        }
        out.flush();
    }
    

    /*
     * The inherited close does the right thing ...
     * <ul>
     *  <li>flushing...</li>
     *  <li>and then closing the wrapped <code>out</code>
     * </ul>
     */

}
