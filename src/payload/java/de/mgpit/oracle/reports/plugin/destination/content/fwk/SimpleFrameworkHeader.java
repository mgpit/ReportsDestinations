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
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;

import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;

import de.mgpit.oracle.reports.plugin.commons.Magic;
import de.mgpit.oracle.reports.plugin.destination.content.types.Header;
import de.mgpit.xml.XML;

/**
 * @author mgp
 *
 */
public class SimpleFrameworkHeader implements Header {

    private ByteArrayInputStream header;
    private int pauseAtPosition = 0;
    private int totalBytes = 0;
    private int bytesBuffered = 0;
    private boolean paused = false;
    private boolean built = false;

    /*
     * (non-Javadoc)
     * 
     * @see de.mgpit.oracle.reports.plugin.destination.content.fwk.AbstractHeader#getHeaderAsStringPropulatedWith(java.util.Properties)
     */
    protected String getHeaderAsStringPropulatedWith( Properties parameters ) throws Exception {
        final Date now = Calendar.getInstance().getTime();
        // @formatter:off
        XML frameworkHeader = XML.newDocument() 
                .add( "framework" ).attribute( "created", now ).nest()
                    .add( "meta" ).nest()
                        .add( "component" ).withData( "Oracle Reports Destination" )
                        .add( "step" ).withData( "1" ).unnest()
                    .add( "data" ).attribute( "size", "" ).unnest()
            ;       
        
        //@formatter:on

        return frameworkHeader.toString();
    }

    public int read() {
        throw new UnsupportedOperationException( "Cannot use a Framework Header on Input" );
    }

    public boolean wantsData() {
        return this.paused;
    }

    public void dataFinished() {
        if ( !paused ) {
            throw new IllegalStateException( "Framework Header currently does NOT consume data!" );
        }
    }

    public void writeToOut( OutputStream out ) throws IOException {
        if ( !built ) {
            throw new IllegalStateException( "Framework Header has not been built!" );
        }
        
        for ( int written = 0; written < pauseAtPosition; written++ ) {
            int next = header.read();
            if ( next == Magic.END_OF_STREAM ) {
                throw new IllegalStateException( "Reading beyond Framework Header" );
            }
            out.write( next );
        }
        paused = true;
    }

    public void build( Properties parameters ) throws Exception {
        final String prefixOfDataOpeningTag = "<data";
       
        final String prefixOfSizeAttribute = "size=\"";
        final int sizeAttributesCharacterLength = prefixOfSizeAttribute.length();
        
        final String text = getHeaderAsStringPropulatedWith( parameters );
        
        final int dataTagPosition = text.lastIndexOf( prefixOfDataOpeningTag );
        final int sizeAttributePosition = text.indexOf( prefixOfSizeAttribute, dataTagPosition );
        final byte[] bytesUpToPausePosition = text.substring( 0, sizeAttributePosition+sizeAttributesCharacterLength ).getBytes();
        
        byte[] textBytes = text.getBytes();
        
        header = new ByteArrayInputStream( textBytes );
        totalBytes = textBytes.length;
        pauseAtPosition = bytesUpToPausePosition.length;
        
        built = true;
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
