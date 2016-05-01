package de.mgpit.oracle.reports.plugin.destination.cdm.schema;


import java.io.StringWriter;

import javax.swing.text.MaskFormatter;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import de.mgpit.oracle.reports.plugin.commons.Units;
import de.mgpit.oracle.reports.plugin.destination.cdm.schema.Cdmdoc;
import de.mgpit.oracle.reports.plugin.destination.cdm.schema.Content;
import de.mgpit.oracle.reports.plugin.destination.cdm.schema.ObjectFactory;
import junit.framework.TestCase;

public class MarshallTest extends TestCase {

    public void testGenerateXML() {

        boolean noExceptionOccured = true;

        ObjectFactory of = new ObjectFactory();
        Cdmdoc cdmdoc = of.createCdmdoc();
        Content content = of.createContent();
        content.setLength( 1000 );
        content.setData( "Lorem Ipsum Dolor si Amet" );
        cdmdoc.setContent( content );

        JAXBContext jaxbContext = null;
        Marshaller jaxbMarshaller = null;

        try {
            jaxbContext = JAXBContext.newInstance( "de.mgpit.oracle.reports.plugin.destination.cdm.schema" );
        } catch ( JAXBException e ) {
            noExceptionOccured = false;
        }
        assertTrue( noExceptionOccured );
        assertNotNull( jaxbContext );

        try {
            jaxbMarshaller = jaxbContext.createMarshaller();
            jaxbMarshaller.setProperty( Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.FALSE );
            // jaxbMarshaller.setProperty( Marshaller.JAXB_FRAGMENT, Boolean.FALSE );
        } catch ( JAXBException some ) {
            some.printStackTrace();
            noExceptionOccured = false;
        }
        assertTrue( noExceptionOccured );
        assertNotNull( jaxbMarshaller );

        StringWriter xmlStream = new StringWriter( Units.ONE_KILOBYTE / 4 );
        try {
            jaxbMarshaller.marshal( cdmdoc, xmlStream );
        } catch ( JAXBException e ) {
            noExceptionOccured = false;
        }
        assertTrue( noExceptionOccured );
        String expected = "<?xml version = '1.0' encoding = 'UTF-8'?><cdmdoc><content><length>1000</length><data>Lorem Ipsum Dolor si Amet</data></content></cdmdoc>";
        String actual = xmlStream.toString();
        // System.out.println( actual );
        assertEquals( expected, actual );

    }

}
