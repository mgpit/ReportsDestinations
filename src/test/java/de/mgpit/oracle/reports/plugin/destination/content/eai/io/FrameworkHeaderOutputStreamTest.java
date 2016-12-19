package de.mgpit.oracle.reports.plugin.destination.content.eai.io;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Properties;

import de.mgpit.oracle.reports.plugin.commons.Units;
import de.mgpit.oracle.reports.plugin.destination.content.eai.fwk.SimpleFrameworkHeader;
import de.mgpit.oracle.reports.plugin.destination.content.io.BufferingHeaderOutputStream;
import junit.framework.TestCase;

public class FrameworkHeaderOutputStreamTest extends TestCase {

    public static final Properties EMPTY_PARAMS = new Properties();

    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    final static long BYTES_TO_BE_WRITTEN = Units.ONE_KILOBYTE+255;
    public void testWrite() {
        
        CountingOutputStream dummy = new CountingOutputStream();
        SimpleFrameworkHeader header = new SimpleFrameworkHeader();
        BufferingHeaderOutputStream fwkOut = new BufferingHeaderOutputStream( dummy, header, EMPTY_PARAMS );
        boolean exceptionOccured = false;
        try {
            for ( long i = 0; i < BYTES_TO_BE_WRITTEN; i++ ) {
                // if (i > 0 && i%78==0)fwkOut.write( 13 );
                fwkOut.write( 65 + ((int)i % 26) );
            }
            fwkOut.close();
        } catch ( IOException io ) {
            exceptionOccured = true;
        }
        assertTrue( BYTES_TO_BE_WRITTEN <= dummy.getReceived() );
        assertEquals(BYTES_TO_BE_WRITTEN+header.lengthInBytes(), (long)dummy.getReceived() );
        assertFalse( exceptionOccured );
    }

    private class CountingOutputStream extends OutputStream {
        private int received = 0;        

        public void write( int b ) throws IOException {
            received++;
        }

        public int getReceived() {
            return this.received;
        }
    }

}
