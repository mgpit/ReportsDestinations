package de.mgpit.oracle.reports.plugin.destination.content;


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

/**
 * A simple Cdm.
 * 
 * @author mgp
 *
 */
public class SimpleCdm implements Envelope {

    private static final String DATA_ELEMENT = "<data>";

    private static final int UNDEFINED = 0;
    private static final int IN_HEADER = 1;
    private static final int IN_DATA = 2;
    private static final int IN_FOOTER = 3;
    private static final HashMap STATE_NAMES;

    private int currentState = UNDEFINED;
    private ByteArrayInputStream headersBytes;
    private ByteArrayInputStream footersBytes;

    public void build( Properties parameters ) {
        final SimpleDateFormat dateFormat = new SimpleDateFormat( "YYYY.MM.dd HH:mm:ss" );
        final String now = dateFormat.format( new Date() );

        final DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        final DocumentBuilder cdmBuilder;
        final Document cdm;
        try {
            cdmBuilder = builderFactory.newDocumentBuilder();
            cdm = cdmBuilder.newDocument();

            Element cdmdoc = cdm.createElement( "cdmdoc" );
            cdmdoc.setAttribute( "created", now );
            cdm.appendChild( cdmdoc );

            Element info = cdm.createElement( "info" );
            info.appendChild( cdm.createTextNode( "Lorem Ipsum Dolor Sit Amet" ) );

            cdmdoc.appendChild( info );
            Element data = cdm.createElement( "data" );
            cdmdoc.appendChild( data );

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();

            transformer.setOutputProperty( OutputKeys.INDENT, "yes" );

            DOMSource source = new DOMSource( cdm );
            StringWriter string = new StringWriter();
            Result stringResult = new StreamResult( string );

            transformer.transform( source, stringResult );

            final String content = stringResult.toString();
            final int indexOfDataElement = content.lastIndexOf( DATA_ELEMENT );
            if ( indexOfDataElement == Magic.CHARACTER_NOT_FOUND ) {
                throw new Exception( U.classname( this ) + " is corrupt" );
            }
            final int endOfHeaderPosition = indexOfDataElement + DATA_ELEMENT.length();

            final String headerContent = content.substring( 0, endOfHeaderPosition );
            final String footerContent = content.substring( endOfHeaderPosition );
            this.headersBytes = new ByteArrayInputStream( headerContent.getBytes() );
            this.footersBytes = new ByteArrayInputStream( footerContent.getBytes() );

            // DocumentFragment foo = cdm.createDocumentFragment();
            // foo.appendChild( info );
            // DOMSource fragment = new DOMSource( foo );
            //
            // System.out.println( "\n\n\n" );
            // transformer.setOutputProperty( OutputKeys.OMIT_XML_DECLARATION, "yes" );
            // transformer.transform( fragment, console );
        } catch ( Exception any ) {
            throw new RuntimeException( "Runtime error", any );
        }

        currentState = IN_HEADER;
    }

    public boolean dataWanted() {
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
            case IN_HEADER:
                aByte = readHeader();
                if ( aByte == Magic.END_OF_STREAM ) {
                    currentState = IN_DATA;
                    aByte = this.read();
                }
                break;
            case IN_DATA:
                break;
            case IN_FOOTER:
                aByte = readFooter();
                break;
            default:
                aByte = Magic.END_OF_STREAM;
            }
        }
        return aByte;
    }

    private int readFooter() {
        return this.footersBytes.read();
    };

    private int readHeader() {
        return this.headersBytes.read();
    }

    public void setDataFinished() {
        if ( currentState != IN_DATA ) {
            throw new IllegalStateException( "Envelope currently does NOT consume data!" );
        }
        currentState = IN_FOOTER;
    }

    public void writeToOut( OutputStream out ) throws IOException {
        switch ( currentState ) {
        case UNDEFINED:
            throw new IllegalStateException( "Envelope has not been built!" );
        case IN_HEADER:
            for ( int nextByte = readHeader(); nextByte != Magic.END_OF_STREAM; nextByte = readHeader() ) {
                out.write( nextByte );
            }
            currentState = IN_DATA;
            break;
        case IN_DATA:
            throw new IllegalStateException( "Envelope currently wants to consume data!" );

        case IN_FOOTER:
            for ( int nextByte = readFooter(); nextByte != Magic.END_OF_STREAM; nextByte = readFooter() ) {
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
        STATE_NAMES.put( new Integer( IN_HEADER ), "In Header section" );
        STATE_NAMES.put( new Integer( IN_FOOTER ), "In Footer section" );
    }
}
