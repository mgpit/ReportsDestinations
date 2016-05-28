package de.mgpit.oracle.reports.plugin.destination.content.decorators;


import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import de.mgpit.oracle.reports.plugin.commons.IOUtility;
import de.mgpit.oracle.reports.plugin.commons.U;
import de.mgpit.oracle.reports.plugin.commons.Units;
import de.mgpit.oracle.reports.plugin.destination.cdm.schema.Cdmdoc;
import de.mgpit.oracle.reports.plugin.destination.cdm.schema.Content;
import de.mgpit.oracle.reports.plugin.destination.cdm.schema.ObjectFactory;
import oracle.reports.RWException;
import oracle.reports.utility.Utility;

public class CdmDecorator extends AbstractContentDecorator {


    private static class CdmDecoratedInputStream extends InputStream {
        private static int END_OF_STREAM_MAGIC_NUMBER = -1;

        private InputStream payload;

        public CdmDecoratedInputStream( InputStream payload ) {
            U.assertNotNull( payload );
            this.payload = payload;
        }

        public int read() {
            return END_OF_STREAM_MAGIC_NUMBER;
        }
    }

    protected InputStream decorate( final InputStream in ) throws RWException {
        InputStream decorated = in;

        try {

            final ObjectFactory of = new ObjectFactory();
            final Cdmdoc cdmdoc = of.createCdmdoc();

            final SimpleDateFormat df = new SimpleDateFormat( "yyyyMMddhhmmss" );
            cdmdoc.setUnifier( df.format( new Date() ) );

            final Content content = of.createContent();
            cdmdoc.setContent( content );

            String data = "";
            try {
                data = IOUtility.asUTF8String( in );
            } catch ( IOException ioException ) {
                throw new Utility().newRWException( ioException );
            }
            content.setData( data );
            content.setLength( data.length() );

            JAXBContext jaxbContext = null;
            Marshaller cdmdocMarshaller = null;
            final StringWriter xmlStream = new StringWriter( Units.ONE_KILOBYTE );

            try {
                jaxbContext = JAXBContext.newInstance( "de.mgpit.oracle.reports.plugin.destination.cdm.schema" );
                cdmdocMarshaller = jaxbContext.createMarshaller();
                cdmdocMarshaller.setProperty( Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE );
                cdmdocMarshaller.setProperty( Marshaller.JAXB_ENCODING, "UTF-8" );

                cdmdocMarshaller.marshal( cdmdoc, xmlStream );
                decorated = new ByteArrayInputStream( xmlStream.toString().getBytes( "UTF-8" ) );
            } catch ( JAXBException jaxbException ) {
                throw Utility.newRWException( jaxbException );
            } catch ( UnsupportedEncodingException unsupportedEncoding ) {
                throw Utility.newRWException( unsupportedEncoding );
            }
        } catch ( JAXBException jaxbex ) {
            throw Utility.newRWException( jaxbex );
        }

        return decorated;
    }

}
