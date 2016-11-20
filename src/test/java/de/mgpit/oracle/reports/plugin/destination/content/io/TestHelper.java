package de.mgpit.oracle.reports.plugin.destination.content.io;


import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Properties;

import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;

import de.mgpit.oracle.reports.plugin.commons.Magic;
import de.mgpit.oracle.reports.plugin.destination.content.types.Envelope;

public class TestHelper {

    public static Envelope getCdm1() {
        return new Envelope() {
            private static final int IN_HEADER = 1;
            private static final int IN_DATA = 2;
            private static final int IN_FOOTER = 3;

            private ByteArrayInputStream header = new ByteArrayInputStream(
                    "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<cdmdoc>\n    <data>\n".getBytes() );
            private ByteArrayInputStream footer = new ByteArrayInputStream( "\n    </data>\n</cdmdoc>".getBytes() );
            private int currentBlock = IN_HEADER;
            
            public void build(Properties parameters ) {
                // NOP
            }

            public boolean dataWanted() {
                boolean wanted = false;
                synchronized (this) {
                    switch ( currentBlock ) {
                    case IN_DATA:
                        wanted = true;
                        break;
                    default:
                        wanted = false;
                    }
                    return wanted;
                }
            }

            public int read() {
                int aByte = Magic.END_OF_STREAM;
                synchronized (this) {
                    switch ( currentBlock ) {
                    case IN_HEADER:
                        aByte = readHeader();
                        if ( aByte == Magic.END_OF_STREAM ) {
                            currentBlock = IN_DATA;
                            aByte = this.read();
                        }
                        break;
                    case IN_DATA:
                        break;
                    case IN_FOOTER:
                        aByte = readFooter();
                        break;
                    default:
                        aByte = Magic.END_OF_STREAM;
                    }
                }
                return aByte;
            }

            private int readFooter() {
                return footer.read();
            };

            private int readHeader() {
                return header.read();
            }

            public void setDataFinished() {
                if ( currentBlock != IN_DATA ) {
                    throw new IllegalStateException( "dataFinished() not allowed when Cdm doesn't want data!" );
                }
                currentBlock = IN_FOOTER;
            }

            public void writeToOut( OutputStream out ) throws IOException {
                switch ( currentBlock ) {
                case IN_HEADER:
                    for ( int nextByte = readHeader(); nextByte != Magic.END_OF_STREAM; nextByte = readHeader() ){
                        out.write(  nextByte );
                    }
                    currentBlock = IN_DATA;
                    break;
                case IN_DATA:
                    throw new IOException( "Cannot write Header if data wanted!" );
                    
                case IN_FOOTER:
                    for ( int nextByte = readFooter(); nextByte != Magic.END_OF_STREAM; nextByte = readFooter() ){
                        out.write( nextByte );
                    }
                    break;
                }

                
            }

            private MimeType mimetype;

            public MimeType mimetype() {
                if ( mimetype == null ) {
                    try {
                        mimetype = new MimeType( "application/xml" );
                    } catch ( MimeTypeParseException unparsable ) {
                        mimetype = new MimeType();
                    }
                }
                return this.mimetype;
            }

            public String fileExtension() {
                return "xml";
            }

        }
        ;
    }

}
