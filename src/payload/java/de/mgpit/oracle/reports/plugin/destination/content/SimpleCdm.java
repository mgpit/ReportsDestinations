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
public class SimpleCdm extends AbstractCdm {

    private static final String DATA_ELEMENT = "<data>";

    public String getPopulatedCdmAsString( Properties parameters ) throws Exception {
        final SimpleDateFormat dateFormat = new SimpleDateFormat( "yyyy.MM.dd HH:mm:ss" );
        final String now = dateFormat.format( new Date() );

        final DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        final DocumentBuilder cdmBuilder;
        final Document cdm;
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
        /* A data element without children would result in <data/> */
        data.appendChild( cdm.createTextNode( "" ) );

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();

        transformer.setOutputProperty( OutputKeys.INDENT, "yes" );

        DOMSource xmlSource = new DOMSource( cdm );
        StringWriter outputTargetTarget = new StringWriter();
        Result outputTarget = new StreamResult( outputTargetTarget );

        transformer.transform( xmlSource, outputTarget );

        return outputTargetTarget.toString();

    }

    protected String getSplitAtTag() {
        return DATA_ELEMENT;
    }

}
