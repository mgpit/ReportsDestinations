package de.mgpit.oracle.reports.plugin.destination.content.transformers;


import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.codec.binary.Base64;

import de.mgpit.oracle.reports.plugin.commons.IOUtility;
import oracle.reports.RWException;
import oracle.reports.utility.Utility;

public class Base64Transformer extends AbstractContentTransformer {

    protected InputStream transform( final InputStream content ) throws RWException{
        try {
            final byte[] binaryData = IOUtility.asByteArray( content );
            final byte[] base64EncodedData = Base64.encodeBase64( binaryData );
            
            return new ByteArrayInputStream( base64EncodedData );
        } catch ( IOException ioex ) {
            throw Utility.newRWException( ioex );
        }
    }

}
