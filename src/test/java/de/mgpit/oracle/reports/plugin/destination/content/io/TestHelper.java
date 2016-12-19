package de.mgpit.oracle.reports.plugin.destination.content.io;


import java.util.Properties;

import de.mgpit.oracle.reports.plugin.destination.content.AbstractEnvelope;
import de.mgpit.oracle.reports.plugin.destination.content.types.Envelope;
import de.mgpit.xml.XML;

public final class TestHelper {
    private static final Properties EMPTY_PROPERTIES = new Properties();
    
    public static final Envelope getCdm1() throws Exception {
        Envelope cdm = new AbstractEnvelope() {

            protected String getEnvelopeAsStringPopulatedWith( Properties parameters ) throws Exception {
              //@ formatter:off
                XML xml = XML.newDocument()
                    .add( "cdmdoc" ).nest()
                    .add( "data" ).withData( "" ).unnest()
                ;    
                return xml.toString();
            }

            protected String getSplitAtToken() {
                return "</data>";
            }
            
        };
        cdm.build( EMPTY_PROPERTIES );
        return cdm;
    }

    public static final Envelope getCdm2() throws Exception {
        Envelope cdm = new AbstractEnvelope() {

            protected String getEnvelopeAsStringPopulatedWith( Properties parameters ) throws Exception {
                // @ formatter:off
                XML xml = XML.newDocument().add( "cdmdoc" ).nest().add( "data" ).withData( "" ).unnest();
                return xml.toString();
            }

            protected String getSplitAtToken() {
                return "</data>";
            }

        };
        cdm.build( EMPTY_PROPERTIES );
        return cdm;
    }

}
