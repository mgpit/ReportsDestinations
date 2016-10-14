package de.mgpit.oracle.reports.plugin.destination.content.io;


import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

import de.mgpit.oracle.reports.plugin.commons.Magic;
import de.mgpit.oracle.reports.plugin.commons.U;
import de.mgpit.oracle.reports.plugin.destination.content.types.Header;

public class HeaderDecoratedInputStream extends FilterInputStream {

    /**
     * Envelope for wrapping.
     */
    private final Header header;

    public HeaderDecoratedInputStream( InputStream toBeDecorated, Header header ) {
        super( toBeDecorated );
        U.assertNotNull( toBeDecorated, "Cannot prepend a null InputStream!" );
        U.assertNotNull( header, "Cannot instantiate without properties!" );
        this.header = header;
    }

    public int read() throws IOException {
        final int aByte;
        if ( header.dataWanted() ) {
            aByte = in.read();
            if ( aByte == Magic.END_OF_STREAM ) {
                header.setDataFinished();
                return this.read();
            }
        } else {
            aByte = header.read();
        }
        return aByte;
    }

    public synchronized int available() throws IOException {
        return header.dataWanted() ? in.available() : 0;
    }
}
