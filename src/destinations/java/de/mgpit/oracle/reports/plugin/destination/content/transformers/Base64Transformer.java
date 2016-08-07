package de.mgpit.oracle.reports.plugin.destination.content.transformers;


import java.io.InputStream;

import org.apache.commons.codec.binary.Base64InputStream;

import oracle.reports.RWException;

public class Base64Transformer extends AbstractContentTransformer {
    private static final boolean AS_ENCODING_STREAM = true;

    protected InputStream wrap( final InputStream content ) throws RWException {
        Base64InputStream base64Input = new org.apache.commons.codec.binary.Base64InputStream( content, AS_ENCODING_STREAM );
        return base64Input;
    }

}
