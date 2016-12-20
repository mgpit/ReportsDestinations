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
package de.mgpit.oracle.reports.plugin.destination.content;


import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Properties;

import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;

import de.mgpit.oracle.reports.plugin.destination.content.types.Content;
import de.mgpit.oracle.reports.plugin.destination.content.types.Header;

/**
 * A simple Cdm.
 * 
 * @author mgp
 *
 */
public abstract class AbstractXmlHeader implements Header {

    private ByteArrayInputStream headersData;
    private long byteLength = Content.UNDEFINED_LENGTH;

    public void build( Properties parameters ) {
        try {

            final String content = getHeaderAsStringPropulatedWith( parameters );
            final byte[] contentBytes = content.getBytes( encoding().name() );
            byteLength = contentBytes.length;
            headersData = new ByteArrayInputStream( contentBytes );
        } catch ( Exception any ) {
            throw new RuntimeException( "Runtime error", any );
        }
    }

    public long lengthInBytes() {
        return this.byteLength;
    }

    /**
     * Gets the data to be put before the payload.
     * 
     * @return {@InputStream} providing the data to be but before the payload.
     */
    public InputStream get() {
        return headersData;
    }

    protected abstract String getHeaderAsStringPropulatedWith( Properties parameters ) throws Exception;

    private Charset encoding;

    public Charset encoding() {
        if ( encoding == null ) {
            try {
                encoding = Charset.forName( "UTF-8" );
            } catch ( IllegalCharsetNameException illegal ) {
                // NOOP
            } catch ( UnsupportedCharsetException unsupperted ) {
                // NOOP
            }
        }
        return encoding;
    }

    private MimeType mimetype;

    public MimeType mimetype() {
        if ( mimetype == null ) {
            try {
                mimetype = new MimeType( "application/xml" );
            } catch ( MimeTypeParseException unparsable ) {
                mimetype = new MimeType();
            }
        }
        return this.mimetype;
    }

    public String fileExtension() {
        return "xml";
    }
}
