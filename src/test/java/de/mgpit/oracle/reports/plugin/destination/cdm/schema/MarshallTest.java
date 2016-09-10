package de.mgpit.oracle.reports.plugin.destination.cdm.schema;


import java.io.PrintWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import junit.framework.TestCase;

public class MarshallTest extends TestCase {

    public void testGenerateXML() {

        boolean noExceptionOccured = true;

        ObjectFactory of = new ObjectFactory();

        Cdmdoc cdmdoc = null;
        ContentType content = null;
        try {
            cdmdoc = of.createCdmdoc();
            content = of.createContentType();
            content.setLength( 1000 );
            
            PropertiesType properties = of.createPropertiesType();
            for ( int i = 1; i < 11; i++ ) {
                PropertyType property = of.createPropertyType();
                String indexAsText = String.valueOf( i );
                property.setKey( indexAsText );
                property.setValue( "The value is " + indexAsText );
                properties.getProperty().add( property );
            }
            content.setProperties( properties );
            
            content.setData( "" );
            cdmdoc.setContent( content );
        } catch ( Exception any ) {
            noExceptionOccured = false;
        }
        assertTrue( noExceptionOccured );

        JAXBContext currentContext = null;
        Marshaller cdmdoc2xml = null;

        try {
            currentContext = JAXBContext.newInstance( "de.mgpit.oracle.reports.plugin.destination.cdm.schema" );
            cdmdoc2xml = currentContext.createMarshaller();
            //cdmdoc2xml.setProperty(Marshaller.JAXB_SCHEMA_LOCATION, "");
            cdmdoc2xml.setProperty( Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE );
        } catch ( JAXBException e ) {
            noExceptionOccured = false;
            e.printStackTrace();
        }
        assertTrue( noExceptionOccured );
        assertNotNull( currentContext );
        assertNotNull( cdmdoc2xml );

        try {
            cdmdoc2xml.marshal( cdmdoc, new PrintWriter( System.out ) );
        } catch ( JAXBException e ) {
            noExceptionOccured = false;
            e.printStackTrace();
        }

//        try {
//            cdmdoc2xml.marshal( cdmdoc, new MyHandler() );
//        } catch ( JAXBException e ) {
//            noExceptionOccured = false;
//            e.printStackTrace();
//        }
//
//        assertTrue( noExceptionOccured );

    }

//    public void testFizzBuzz() {
//        CXMLStream xml = new CXMLStream();
//        XMLObjectInput xmlIn = new XMLObjectInput( null );
//    }

    private static class MyHandler extends DefaultHandler {
        protected static String name( String namespaceURI, String localName, String qName ) {
            return "".equals( namespaceURI ) ? qName : "{" + namespaceURI + "}" + localName;
        }

        public void startElement( String namespaceURI, String localName, String qName, Attributes atts ) throws SAXException {
            System.out.print( "<" + name( namespaceURI, localName, qName ) + ">" );
        }

        public void endElement( String namespaceURI, String localName, String qName ) throws SAXException {
            System.out.print( "</" + name( namespaceURI, localName, qName ) + ">" );
        }

        public void characters( char[] ch, int start, int length ) throws SAXException {
            final String content = String.copyValueOf( ch, start, length );
            System.out.print( content );
        }
    }

}
