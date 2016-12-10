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
package de.mgpit.oracle.reports.plugin.destination.content.cdm;


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

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import de.mgpit.oracle.reports.plugin.commons.Magic;
import de.mgpit.oracle.reports.plugin.commons.U;
import de.mgpit.oracle.reports.plugin.destination.content.types.Envelope;

/**
 * A simple Cdm.
 * 
 * @author mgp
 *
 */
public abstract class AbstractCdm implements Envelope {

    private static final int UNDEFINED = 0;
    private static final int BEFORE_DATA = 1;
    private static final int IN_DATA = 2;
    private static final int AFTER_DATA = 3;
    private static final HashMap STATE_NAMES;

    private int currentState = UNDEFINED;
    private ByteArrayInputStream contentToPutBeforeData;
    private ByteArrayInputStream contentToPutAfterData;

    public void build( Properties parameters ) {
        try {

            final String content = getEnvelopeAsStringPopulatedWith( parameters );
            final String splitToken = getSplitAtToken();
            final int indexOfSplitToken = content.lastIndexOf( splitToken );
            if ( indexOfSplitToken == Magic.SUBSTRING_NOT_FOUND ) {
                throw new Exception( U.classname( this ) + " is corrupt" );
            }
            final int cuttingPosition = indexOfSplitToken + splitToken.length();

            final String beforeContent = content.substring( 0, cuttingPosition );
            final String afterContent = content.substring( cuttingPosition );
            this.contentToPutBeforeData = new ByteArrayInputStream( beforeContent.getBytes() );
            this.contentToPutAfterData = new ByteArrayInputStream( afterContent.getBytes() );

        } catch ( Exception any ) {
            throw new RuntimeException( "Runtime error", any );
        }

        currentState = BEFORE_DATA;
    }

    protected abstract String getEnvelopeAsStringPopulatedWith( Properties parameters ) throws Exception;

    protected abstract String getSplitAtToken();

    public boolean wantsData() {
        boolean wanted = false;
        synchronized (this) {
            switch ( currentState ) {
            case IN_DATA:
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
                throw new IllegalStateException( "Envelope has not been built!" );
            case BEFORE_DATA:
                aByte = readEnvelopeBeforeData();
                if ( aByte == Magic.END_OF_STREAM ) {
                    currentState = IN_DATA;
                    aByte = this.read();
                }
                break;
            case IN_DATA:
                /* Envelope pauses for the payload to be read */
                aByte = Magic.END_OF_STREAM;
                break;
            case AFTER_DATA:
                aByte = readEnvelopeAfterData();
                break;
            default:
                aByte = Magic.END_OF_STREAM;
            }
        }
        return aByte;
    }

    private int readEnvelopeAfterData() {
        return this.contentToPutAfterData.read();
    };

    private int readEnvelopeBeforeData() {
        return this.contentToPutBeforeData.read();
    }

    public void dataFinished() {
        if ( currentState != IN_DATA ) {
            throw new IllegalStateException( "Envelope currently does NOT consume data!" );
        }
        currentState = AFTER_DATA;
    }

    public void writeToOut( OutputStream out ) throws IOException {
        switch ( currentState ) {
        case UNDEFINED:
            throw new IllegalStateException( "Envelope has not been built!" );
        case BEFORE_DATA:
            for ( int nextByte = readEnvelopeBeforeData(); nextByte != Magic.END_OF_STREAM; nextByte = readEnvelopeBeforeData() ) {
                out.write( nextByte );
            }
            currentState = IN_DATA;
            break;
        case IN_DATA:
            throw new IllegalStateException( "Envelope currently wants to consume data!" );
        case AFTER_DATA:
            for ( int nextByte = readEnvelopeAfterData(); nextByte != Magic.END_OF_STREAM; nextByte = readEnvelopeAfterData() ) {
                out.write( nextByte );
            }
            break;
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
        STATE_NAMES.put( new Integer( IN_DATA ), "Data/Payload wanted" );
        STATE_NAMES.put( new Integer( BEFORE_DATA ), "Before Data section" );
        STATE_NAMES.put( new Integer( AFTER_DATA ), "After Data section" );
    }
}