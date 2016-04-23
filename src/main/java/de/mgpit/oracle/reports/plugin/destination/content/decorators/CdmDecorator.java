package de.mgpit.oracle.reports.plugin.destination.content.decorators;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.Ostermiller.util.CircularByteBuffer;

import de.mgpit.oracle.reports.plugin.commons.IOUtility;
import de.mgpit.oracle.reports.plugin.commons.Units;
import oracle.reports.RWException;
import oracle.reports.utility.Utility;

public class CdmDecorator extends AbstractContentDecorator {

    protected InputStream decorate( InputStream in ) throws RWException {
        InputStream decorated = in;
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        String prolog = "<?xml?><cdm><info>Foo</info><data>";
        String epilog = "</data>";

        try {
            out.write( prolog.getBytes() );
            IOUtility.copyFromTo( in, out );
            out.write( epilog.getBytes() );
            decorated = new ByteArrayInputStream( out.toByteArray() );
        } catch ( IOException ioex ) {
            throw Utility.newRWException( ioex );
        } finally {
            try {
                out.close();
            } catch ( Exception e ) {
                // NOOP
            }
        }
        return decorated;
    }

}
