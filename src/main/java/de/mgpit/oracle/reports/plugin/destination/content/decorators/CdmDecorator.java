package de.mgpit.oracle.reports.plugin.destination.content.decorators;


import java.io.InputStream;

import org.xml.sax.ContentHandler;

import de.mgpit.oracle.reports.plugin.commons.U;
import de.mgpit.oracle.reports.plugin.destination.cdm.Cdm;
import oracle.reports.RWException;

public class CdmDecorator extends AbstractContentDecorator {


    private static class CdmDecoratedInputStream extends InputStream {
        private static int END_OF_STREAM_MAGIC_NUMBER = -1;

        private InputStream cdmDataInput;
        private Cdm cdm;

        public CdmDecoratedInputStream( InputStream payload, Cdm cdm ) {
            U.assertNotNull( payload );
            U.assertNotNull( cdm );
            this.cdmDataInput = payload;
            this.cdm = cdm;
        }

        public int read() {
            return END_OF_STREAM_MAGIC_NUMBER;
        }
    }

    protected InputStream decorate( final InputStream in ) throws RWException {
        return new CdmDecoratedInputStream( in, null );
    }

}
