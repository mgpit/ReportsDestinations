package de.mgpit.oracle.reports.plugin.destination.content.cdm;


import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

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
import org.w3c.dom.Text;

/**
 * A simple Cdm.
 * 
 * @author mgp
 *
 */
public class SimpleCdm extends AbstractCdm {

    private static final String DATA_ELEMENT = "<data>";

    public String getEnvelopeAsStringPopulatedWith( Properties parameters ) throws Exception {
        final SimpleDateFormat dateFormat = new SimpleDateFormat( "yyyy.MM.dd HH:mm:ss" );
        final String now = dateFormat.format( new Date() );

        final DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        final DocumentBuilder cdmBuilder;
        final Document currentDocument;
        cdmBuilder = builderFactory.newDocumentBuilder();
        currentDocument = cdmBuilder.newDocument();

        Element cdmdoc = currentDocument.createElement( "cdmdoc" );
        cdmdoc.setAttribute( "created", now );
        currentDocument.appendChild( cdmdoc );

        Element address = currentDocument.createElement( "address" );
        
        boolean hasAddress = false; 
        for ( int lineNumber = 1; lineNumber < 7; lineNumber++) {
            final String key = "address_line_" + lineNumber;
            if ( parameters.containsKey( key )) {
                Element addressLine = currentDocument.createElement( key );
                Text text = currentDocument.createTextNode( parameters.getProperty( key, "" ) );
                addressLine.appendChild( text );
                address.appendChild( addressLine );
                hasAddress = true;
            }
        }
        if ( hasAddress) {
            cdmdoc.appendChild( address );
        }
        
        Element data = currentDocument.createElement( "data" );
        cdmdoc.appendChild( data );
        /* A data element without children would result in <data/> */
        data.appendChild( currentDocument.createTextNode( "" ) );

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();

        transformer.setOutputProperty( OutputKeys.INDENT, "yes" );

        DOMSource xmlSource = new DOMSource( currentDocument );
        StringWriter outputTargetTarget = new StringWriter();
        Result outputTarget = new StreamResult( outputTargetTarget );

        transformer.transform( xmlSource, outputTarget );

        return outputTargetTarget.toString();

    }

    protected String getSplitAtToken() {
        return DATA_ELEMENT;
    }

}
