package de.mgpit.oracle.reports.plugin.destination.cdm;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import de.mgpit.oracle.reports.plugin.destination.cdm.schema.Cdmdoc;
import de.mgpit.oracle.reports.plugin.destination.cdm.schema.Content;
import de.mgpit.oracle.reports.plugin.destination.cdm.schema.ObjectFactory;

public class MarshallTest {
    
    public static void main( String[] args ) {
        
        
        try {
            
            ObjectFactory of = new ObjectFactory();
            
            Cdmdoc cdmdoc = of.createCdmdoc();
            Content content = of.createContent();
            content.setLength( 1000 );
            content.setPayload( "Lorem Ipsum Dolor si Amet" );
            cdmdoc.setContent( content );
            
            JAXBContext jaxbContext;
            jaxbContext = JAXBContext.newInstance( "de.mgpit.oracle.reports.plugin.destination.cdm.schema");
            Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
            jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            
            jaxbMarshaller.marshal( cdmdoc, System.out );
            
        } catch ( JAXBException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        
    }
}
