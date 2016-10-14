package de.mgpit.oracle.reports.plugin.destination.content.transformers;


import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import org.apache.commons.codec.binary.Base64InputStream;
import org.apache.commons.codec.binary.Base64OutputStream;

import de.mgpit.oracle.reports.plugin.destination.content.types.InputTransformation;
import de.mgpit.oracle.reports.plugin.destination.content.types.OutputTransformation;
import oracle.reports.RWException;

public class Base64Transformer implements InputTransformation, OutputTransformation {
    private static final boolean AS_ENCODING_STREAM = true;

    public Base64Transformer() {
    }

    public InputStream forInput( final InputStream content, final Properties parameters ) throws RWException {
        Base64InputStream base64Input = new org.apache.commons.codec.binary.Base64InputStream( content, AS_ENCODING_STREAM );
        return base64Input;
    }

    public OutputStream forOutput( final OutputStream content, final Properties parameters ) throws RWException {
        Base64OutputStream base64Output = new org.apache.commons.codec.binary.Base64OutputStream( content, AS_ENCODING_STREAM );
        return base64Output;
    }

    public String mimetype() {
        return "text/plain";
    }

    public String fileExtension() {
        return "ascii";
    }

}
