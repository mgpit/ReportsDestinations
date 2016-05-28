package de.mgpit.oracle.reports.plugin.destination.cdm.schema;


import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import de.mgpit.oracle.reports.plugin.commons.Units;
import junit.framework.TestCase;

public class MarshallTest extends TestCase {

    public void testGenerateXML() {

        boolean noExceptionOccured = true;

        ObjectFactory of = new ObjectFactory();

        Cdmdoc cdmdoc = null;
        Content content = null;
        try {
            cdmdoc = of.createCdmdoc();
            content = of.createContent();
            content.setLength( 1000 );
            content.setData( "Lorem Ipsum Dolor si Amet" );
            cdmdoc.setContent( content );
        } catch ( JAXBException e1 ) {
            noExceptionOccured = false;
        }
        assertTrue( noExceptionOccured );

        JAXBContext cdmdocContext = null;
        Marshaller cdmdoc2xml = null;

        try {
            cdmdocContext = JAXBContext.newInstance( "de.mgpit.oracle.reports.plugin.destination.cdm.schema" );
        } catch ( JAXBException e ) {
            noExceptionOccured = false;
        }
        assertTrue( noExceptionOccured );
        assertNotNull( cdmdocContext );

        try {
            cdmdoc2xml = cdmdocContext.createMarshaller();
            cdmdoc2xml.setProperty( Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.FALSE );
            cdmdoc2xml.setProperty( Marshaller.JAXB_FRAGMENT, Boolean.TRUE );

        } catch ( JAXBException some ) {
            some.printStackTrace();
            noExceptionOccured = false;
        }
        assertTrue( noExceptionOccured );
        assertNotNull( cdmdoc2xml );
        assertNotNull( cdmdoc );

        StringWriter xmlStream = new StringWriter( Units.ONE_KILOBYTE / 4 );
        try {
            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();

            // root elements
            Document doc = docBuilder.newDocument();
            Element rootElement = doc.createElement( "loremipsum" );
            doc.appendChild( rootElement );
            cdmdoc2xml.marshal( content, rootElement );
            cdmdoc2xml.marshal( content, rootElement );
            cdmdoc2xml.marshal( content, rootElement );
            cdmdoc2xml.marshal( content, rootElement );

            // output DOM XML to console
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty( OutputKeys.INDENT, "yes" );
            DOMSource source = new DOMSource( doc );
            StreamResult console = new StreamResult( System.out );
            transformer.transform( source, console );

        } catch ( JAXBException e ) {
            noExceptionOccured = false;
        } catch ( ParserConfigurationException e ) {
            noExceptionOccured = false;
        } catch ( TransformerConfigurationException e ) {
            noExceptionOccured = false;
        } catch ( TransformerFactoryConfigurationError e ) {
            noExceptionOccured = false;
        } catch ( TransformerException e ) {
            noExceptionOccured = false;
        }
        assertTrue( noExceptionOccured );
        //String expected = "<?xml version = '1.0' encoding = 'UTF-8'?><cdmdoc><content><length>1000</length><data>Lorem Ipsum Dolor si Amet</data></content></cdmdoc>";
        //String actual = xmlStream.toString();
        // assertEquals( expected, actual );
    }

}
