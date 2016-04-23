package de.mgpit.oracle.reports.plugin.destination.content.transformers;


import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

import org.apache.commons.codec.binary.Base64OutputStream;

import de.mgpit.oracle.reports.plugin.commons.IOUtility;
import de.mgpit.oracle.reports.plugin.commons.Units;
import oracle.reports.RWException;
import oracle.reports.utility.Utility;

public class Base64Transformer extends ContentTransformer {

    protected InputStream transform( InputStream content ) throws RWException{
        ByteArrayOutputStream base64Bytes = new ByteArrayOutputStream( Units.HALF_A_MEAGABYTE );
        Base64OutputStream base64Out = new Base64OutputStream( base64Bytes );
        try {
            IOUtility.copyFromTo( content, base64Out );
        } catch ( IOException ioex ) {
            throw Utility.newRWException( ioex );
        }
        
        InputStream transformed = new ByteArrayInputStream( base64Bytes.toByteArray() );
        return transformed;
        
    }

}
