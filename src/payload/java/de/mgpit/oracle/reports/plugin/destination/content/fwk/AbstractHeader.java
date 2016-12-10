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
package de.mgpit.oracle.reports.plugin.destination.content.fwk;


import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Properties;

import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import de.mgpit.oracle.reports.plugin.commons.Magic;
import de.mgpit.oracle.reports.plugin.commons.U;
import de.mgpit.oracle.reports.plugin.destination.content.types.Envelope;
import de.mgpit.oracle.reports.plugin.destination.content.types.Header;

/**
 * A simple Cdm.
 * 
 * @author mgp
 *
 */
public abstract class AbstractHeader implements Header {

    private static final int UNDEFINED = 0;
    private static final int IN_HEADER = 1;
    private static final int AT_END = 4;
    private static final HashMap STATE_NAMES;

    private int currentState = UNDEFINED;
    private ByteArrayInputStream headersBytes;

    public void build( Properties parameters ) {
        try {

            final String content = getHeaderAsStringPropulatedWith( parameters );
            this.headersBytes = new ByteArrayInputStream( content.getBytes() );

        } catch ( Exception any ) {
            throw new RuntimeException( "Runtime error", any );
        }

        currentState = IN_HEADER;
    }

    protected abstract String getHeaderAsStringPropulatedWith( Properties parameters ) throws Exception;

    public boolean wantsData() {
        boolean wanted = false;
        synchronized (this) {
            switch ( currentState ) {
            case AT_END:
                wanted = true;
                break;
            default:
                wanted = false;
            }
            return wanted;
        }
    }

    public int read() {
        int aByte = Magic.END_OF_STREAM;
        synchronized (this) {
            switch ( currentState ) {
            case UNDEFINED:
                throw new IllegalStateException( "Header has not been built!" );
            case IN_HEADER:
                aByte = readHeader();
                if ( aByte == Magic.END_OF_STREAM ) {
                    currentState = AT_END;
                    aByte = this.read();
                }
                break;
            case AT_END:
                /* Header is consumed */
                aByte = Magic.END_OF_STREAM;
                break;
            default:
                aByte = Magic.END_OF_STREAM;
            }
        }
        return aByte;
    }

    private int readHeader() {
        return this.headersBytes.read();
    }

    public void dataFinished() {
        if ( currentState != AT_END ) {
            throw new IllegalStateException( "Header currently does NOT consume data!" );
        }
    }

    public void writeToOut( OutputStream out ) throws IOException {
        switch ( currentState ) {
        case UNDEFINED:
            throw new IllegalStateException( "Header has not been built!" );
        case IN_HEADER:
            for ( int nextByte = readHeader(); nextByte != Magic.END_OF_STREAM; nextByte = readHeader() ) {
                out.write( nextByte );
            }
            currentState = AT_END;
            break;
        case AT_END:
            throw new IllegalStateException( "Header currently wants to consume data!" );
        }
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

    public static String statename( int stateCode ) {
        return U.coalesce( (String) STATE_NAMES.get( new Integer( stateCode ) ), "???Unknown???" );
    }

    static {
        STATE_NAMES = new HashMap( 11 );
        STATE_NAMES.put( new Integer( UNDEFINED ), "Undefined" );
        STATE_NAMES.put( new Integer( AT_END ), "Data/Payload wanted" );
        STATE_NAMES.put( new Integer( IN_HEADER ), "In Header section" );
    }
}
