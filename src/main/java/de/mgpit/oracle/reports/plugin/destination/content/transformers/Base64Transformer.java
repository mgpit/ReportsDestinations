package de.mgpit.oracle.reports.plugin.destination.content.transformers;


import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Base64InputStream;
import de.mgpit.oracle.reports.plugin.commons.IOUtility;
import oracle.reports.RWException;
import oracle.reports.utility.Utility;

public class Base64Transformer extends AbstractContentTransformer {
    private static final boolean FOR_ENCODING = true;

    protected InputStream transform( final InputStream content ) throws RWException {
        Base64InputStream base64Input = new org.apache.commons.codec.binary.Base64InputStream( content, FOR_ENCODING );
        return base64Input;
    }

}
