package de.mgpit.oracle.reports.plugin.destination.content;


import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
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
import de.mgpit.oracle.reports.plugin.destination.content.types.Content;

/**
 * A simple Cdm.
 * 
 * @author mgp
 *
 */
public class SimpleCdm implements Content {
    private Properties properties;
    private String content;
    private boolean built = false;

    public SimpleCdm( Properties properties ) {
        this.properties = properties;
        build();
    }

    private void build() {
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

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();

            transformer.setOutputProperty( OutputKeys.INDENT, "yes" );

            DOMSource source = new DOMSource( cdm );
            StringWriter string = new StringWriter();
            Result stringResult = new StreamResult( string );

            transformer.transform( source, stringResult );
            this.content = stringResult.toString();

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

        built = true;
    }

    public int read() {
        // TODO Auto-generated method stub
        U.assertTrue( built, "Cannot read an unbuilt Cdm!" );
        return Magic.END_OF_STREAM;
    }

    public boolean dataWanted() {

        return false;
    }

    public void setDataFinished() {
        // TODO Auto-generated method stub

    }

    public void writeToOut( OutputStream out ) throws IOException {
        // TODO Auto-generated method stub

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
