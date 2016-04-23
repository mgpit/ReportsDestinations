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

public class CdmDecorator extends ContentDecorator {

    protected InputStream decorate( InputStream in ) throws RWException {
        InputStream decorated = in;
        CircularByteBuffer cbb = new CircularByteBuffer(CircularByteBuffer.INFINITE_SIZE);
        OutputStream out = cbb.getOutputStream();

        String prolog = "<?xml?><cdm><info>Foo</info><data>";
        String epilog = "</data>";

        try {
            out.write( prolog.getBytes() );
            IOUtility.copyFromTo( in, out );
            out.write( epilog.getBytes() );
            
            decorated = cbb.getInputStream();
        } catch ( IOException ioex ) {
            throw Utility.newRWException( ioex );
        }

        return decorated;
    }

}
