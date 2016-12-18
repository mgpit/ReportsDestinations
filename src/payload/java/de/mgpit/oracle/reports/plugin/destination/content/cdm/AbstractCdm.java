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
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
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

    private static final SimpleDateFormat UNIFIER_PREFIX_FORMAT = new SimpleDateFormat( "yyyyMMddhhmmssSSS" );
    private static final DecimalFormat UNIFIER_SUFFIX_FORMAT = new DecimalFormat( "000" );
    private static int UNIFIER_COUNTER = 0;

    private ByteArrayInputStream envelopesDataBeforePayload;
    private ByteArrayInputStream envelopesDataAfterPayload;

    /**
     * 
     * @return a new Unifier
     */
    public static synchronized String getUnifier() {
        final Date now = Calendar.getInstance().getTime();
        final String unifierPrefix = UNIFIER_PREFIX_FORMAT.format( now );
        final String unifierSuffix = UNIFIER_SUFFIX_FORMAT.format( (double) UNIFIER_COUNTER );

        UNIFIER_COUNTER = (UNIFIER_COUNTER < 999) ? UNIFIER_COUNTER + 1 : 0;

        return unifierPrefix.concat( unifierSuffix );
    }

    private String text;

    public String getText() {
        return this.text;
    }

    /**
     * Builds this CDM.
     * <p>
     * The CDM may have variable content. Parameters for populating this content can be passed as {@code Properties}.
     * 
     * @param parameters
     *            Properties for populating.
     * 
     * @throws Exception
     *             on errors during the build.
     */
    public void build( Properties parameters ) throws Exception {
        text = getEnvelopeAsStringPopulatedWith( parameters );
        final String splitToken = getSplitAtToken();
        final int lastIndexOfSplitToken = text.lastIndexOf( splitToken );
        if ( lastIndexOfSplitToken == Magic.SUBSTRING_NOT_FOUND ) {
            throw new CdmCorruptException( U.classname( this ) + " is corrupt. " + splitToken + " not found!" );
        }
        final int firstIndexOfSplitToken = text.indexOf( splitToken );
        if ( firstIndexOfSplitToken != lastIndexOfSplitToken ) {
            throw new CdmCorruptException( U.classname( this ) + " is corrupt. " + splitToken + " contained two times at least!" );
        }

        final int cuttingPosition = lastIndexOfSplitToken;

        final String beforeContent = text.substring( 0, cuttingPosition );
        final String afterContent = text.substring( cuttingPosition );
        envelopesDataBeforePayload = new ByteArrayInputStream( beforeContent.getBytes() );
        envelopesDataAfterPayload = new ByteArrayInputStream( afterContent.getBytes() );
    }

    /**
     * Gets the data to be put before the payload.
     * 
     * @return {@InputStream} providing the data to be but before the payload.
     */
    public InputStream getBeforePayload() {
        return envelopesDataBeforePayload;
    }

    /**
     * Gets the data to be put after the payload.
     * 
     * @return {@InputStream} providing the data to be but after the payload.
     */
    public InputStream getAfterPayload() {
        return envelopesDataAfterPayload;
    }

    protected abstract String getEnvelopeAsStringPopulatedWith( Properties parameters ) throws Exception;

    protected abstract String getSplitAtToken();

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

    public static class CdmCorruptException extends Exception {
        private static final long serialVersionUID = 1L;

        public CdmCorruptException( String message ) {
            super( message );
        }

        public CdmCorruptException( String message, Throwable cause ) {
            super( message, cause );
        }
    }

}
