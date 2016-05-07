package de.mgpit.oracle.reports.plugin.destination.cdm.schema;


import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

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
            cdmdoc2xml.setProperty( Marshaller.JAXB_FRAGMENT, new Boolean( true ) );
        } catch ( JAXBException some ) {
            some.printStackTrace();
            noExceptionOccured = false;
        }
        assertTrue( noExceptionOccured );
        assertNotNull( cdmdoc2xml );
        assertNotNull( cdmdoc );

        StringWriter xmlStream = new StringWriter( Units.ONE_KILOBYTE / 4 );
        try {
            cdmdoc2xml.marshal( cdmdoc, xmlStream );
        } catch ( JAXBException e ) {
            noExceptionOccured = false;
        }
        assertTrue( noExceptionOccured );
        String expected = "<?xml version = '1.0' encoding = 'UTF-8'?><cdmdoc><content><length>1000</length><data>Lorem Ipsum Dolor si Amet</data></content></cdmdoc>";
        String actual = xmlStream.toString();
        System.out.println( actual );
        assertEquals( expected, actual );
    }

}
