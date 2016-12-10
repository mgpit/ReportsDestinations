package de.mgpit.oracle.reports.plugin.destination.content.cdm;


import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import de.mgpit.xml.XML;
import de.mgpit.xml.XML.XMLFragment;

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

        XML cdm = XML.newDocument();
        cdm.add( "cdmdoc" )
           .attribute( "created", now )
           .nest();

        XMLFragment address = XML.newFragment( cdm );
        address.add( "address" );
        boolean hasAddress = false;
        address.nest();
        for ( int lineNumber = 1; lineNumber < 7; lineNumber++ ) {
            final String key = "address_line_" + lineNumber;
            if ( parameters.containsKey( key ) ) {
                address.add( key )
                       .withData( parameters.getProperty( key, "" ) );
                hasAddress = true;
            }
        }
        // address.up();
        if ( hasAddress ) {
            cdm.add( address );
        }
        cdm.add( "fizz" )
           .nest()
           .add( "buzz" )
           .nest()
           .add( "foo" )
           .add( "bar" )
           .withData( "Lorem Ipsum" )
           .unnest()
           .add( "doe" ).withData( "Dolor sit amet" )
           .unnest()
           .add( "data" )
           .withData( "" );

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();

        transformer.setOutputProperty( OutputKeys.INDENT, "yes" );

        DOMSource xmlSource = new DOMSource( cdm.get() );
        StringWriter outputTargetContent = new StringWriter();
        Result outputTarget = new StreamResult( outputTargetContent );

        transformer.transform( xmlSource, outputTarget );
        System.out.println( outputTargetContent.toString() );
        return outputTargetContent.toString();

    }

    protected String getSplitAtToken() {
        return DATA_ELEMENT;
    }

}
