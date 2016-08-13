package de.mgpit.oracle.reports.plugin.destination.content.transformers;


import java.io.InputStream;

import org.apache.commons.codec.binary.Base64InputStream;

import de.mgpit.oracle.reports.plugin.destination.content.ContentModificationPlugin;
import oracle.reports.RWException;

public class Base64Transformer implements ContentModificationPlugin {
    private static final boolean AS_ENCODING_STREAM = true;

    public Base64Transformer() {
    }

    public InputStream wrap( final InputStream content ) throws RWException {
        Base64InputStream base64Input = new org.apache.commons.codec.binary.Base64InputStream( content, AS_ENCODING_STREAM );
        return base64Input;
    }
    
    public String mimetype() {
        return "text/plain";
    }

    public String fileExtension() {
        return null;
    }


}
