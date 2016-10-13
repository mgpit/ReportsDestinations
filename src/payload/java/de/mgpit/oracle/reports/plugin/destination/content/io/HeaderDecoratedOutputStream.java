package de.mgpit.oracle.reports.plugin.destination.content.io;


import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import de.mgpit.oracle.reports.plugin.commons.Magic;
import de.mgpit.oracle.reports.plugin.commons.U;
import de.mgpit.oracle.reports.plugin.destination.content.Header;

public class HeaderDecoratedOutputStream extends FilterOutputStream {

    private final Header header;

    public HeaderDecoratedOutputStream( final OutputStream toWrap, Header header ) {
        super( toWrap );
        U.assertNotNull( toWrap, "Cannot prepend a null OutputStream!" );
        U.assertNotNull( header, "Cannot instantiate without Header!" );
        this.header = header;
    }

    public void write( int b ) throws IOException {
        if ( !header.dataWanted() ) {
            header.writeToOut( out );
        }

        if ( header.dataWanted() ) {
            out.write( b );
        } else {
            throw new IOException( "Cannot write after a Header which is not in state \"data wanted\"!" );
        }
    }

    public void flush() throws IOException {
        if ( !header.dataWanted() ) {
            throw new IOException( "Cannot flush a Header which is not in state \"data wanted\"!" );
        }
        header.setDataFinished();
        out.flush();
    }

    /*
     * The inherited close does the right thing ...
     * <ul>
     * <li>flushing...</li>
     * <li>and then closing the wrapped <code>out</code>
     * </ul>
     */
}
