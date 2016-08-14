package de.mgpit.oracle.reports.plugin.destination.content.decorators;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import de.mgpit.oracle.reports.plugin.commons.Magic;
import de.mgpit.oracle.reports.plugin.destination.content.EnvelopeInput;
import de.mgpit.oracle.reports.plugin.destination.content.io.EnvelopeWrappingInputStream;
import oracle.reports.RWException;

public class SimpleCdmDecorator extends CdmDecorator {
    
    protected EnvelopeInput getCdm() {
        return new EnvelopeInput() {
            private static final int IN_HEADER = 1;
            private static final int IN_DATA = 2;
            private static final int IN_FOOTER = 3;

            private ByteArrayInputStream header = new ByteArrayInputStream(
                    "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<cdmdoc>\n    <data>\n".getBytes() );
            private ByteArrayInputStream footer = new ByteArrayInputStream( "\n    </data>\n</cdmdoc>".getBytes() );
            private int currentBlock = IN_HEADER;

            public boolean dataWanted() {
                boolean wanted = false;
                switch ( currentBlock ) {
                case IN_DATA:
                    wanted = true;
                    break;
                default:
                    wanted = false;
                }
                return wanted;
            }

            public int read() {
                int aByte = Magic.END_OF_STREAM;
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
                return aByte;
            }

            private int readFooter() {
                return footer.read();
            };

            private int readHeader() {
                return header.read();
            }

            public void dataFinished() {
                if ( currentBlock != IN_DATA ) {
                    throw new IllegalStateException( "dataFinished() not allowed when Cdm doesn't want data!" );
                }
                currentBlock = IN_FOOTER;
            }
        };
    }

}
