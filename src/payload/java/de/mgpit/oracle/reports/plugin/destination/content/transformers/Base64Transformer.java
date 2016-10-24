/*
 * Copyright 2016 Marco Pauls www.mgp-it.de
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

/**
 * @license APACHE-2.0
 */
package de.mgpit.oracle.reports.plugin.destination.content.transformers;


import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import org.apache.commons.codec.binary.Base64InputStream;
import org.apache.commons.codec.binary.Base64OutputStream;

import de.mgpit.oracle.reports.plugin.destination.content.types.InputTransformation;
import de.mgpit.oracle.reports.plugin.destination.content.types.OutputTransformation;
import oracle.reports.RWException;

/**
 * An {@code Transformation} for BASE64 encoding a {@code Stream}.
 *  
 * @author mgp
 *
 */
public class Base64Transformer implements InputTransformation, OutputTransformation {
    private static final boolean AS_ENCODING_STREAM = true;

    public Base64Transformer() {
    }

    /**
     * Applies the BASE64 encoding on an {@code InputStream}.
     */
    public InputStream forInput( final InputStream content, final Properties parameters ) throws RWException {
        Base64InputStream base64Input = new org.apache.commons.codec.binary.Base64InputStream( content, AS_ENCODING_STREAM );
        return base64Input;
    }

    /**
     * Applies the BASE64 encoding on an {@code OutputStream}.
     */
    public OutputStream forOutput( final OutputStream content, final Properties parameters ) throws RWException {
        Base64OutputStream base64Output = new org.apache.commons.codec.binary.Base64OutputStream( content, AS_ENCODING_STREAM );
        return base64Output;
    }

    /**
     * Gets the mime type of the data a produced by this tranformation.
     * 
     * @return string denoting the mime type 
     */
    public String mimetype() {
        return "text/plain";
    }

    /**
     * Gets the file extension which should be used for filenames of files storing data produced by this transformation.
     * 
     * @return string denoting the file extension
     */
    public String fileExtension() {
        return "ascii";
    }

}
