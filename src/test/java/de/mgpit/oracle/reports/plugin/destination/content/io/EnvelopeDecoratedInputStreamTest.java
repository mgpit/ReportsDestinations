package de.mgpit.oracle.reports.plugin.destination.content.io;


import java.io.ByteArrayInputStream;

import org.apache.commons.codec.binary.Base64InputStream;

import de.mgpit.oracle.reports.plugin.commons.Magic;
import de.mgpit.oracle.reports.plugin.commons.io.IOUtility;
import junit.framework.TestCase;

public class EnvelopeDecoratedInputStreamTest extends TestCase {
    public static final String NL = System.getProperty( "line.separator","\n" );
    
    private static final String expected1 =
            //@formatter: off
            "<?xml version = '1.0' encoding = 'UTF-8'?>"+NL 
          + "<cdmdoc>"+NL 
          + "   <data>Lorem Ipsum Dolor Si amet</data>"+NL 
          + "</cdmdoc>"
            ;
            //@formatter: on
    

    private static final String expected2 =
            //@formatter: off
            "<?xml version = '1.0' encoding = 'UTF-8'?>"+NL 
          + "<cdmdoc>"+NL 
          + "   <data>TG9yZW0gSXBzdW0gRG9sb3IgU2kgYW1ldA=="+NL 
          + "</data>"+NL 
          + "</cdmdoc>"
            ;
            //@formatter: on

    public void testPlainPayload() {
        boolean exceptionOccured = false;
        try {
            ByteArrayInputStream payload = new ByteArrayInputStream( "Lorem Ipsum Dolor Si amet".getBytes() );
            ContentDecoratedInputStream cdmPlainPayloadStream = new EnvelopeDecoratedInputStream( payload, TestHelper.getCdm1() );

            String actual1 = IOUtility.inputAsUTF8String( cdmPlainPayloadStream );
            assertEquals( expected1, actual1 );
        } catch ( Exception any ) {
            exceptionOccured = true;
            any.printStackTrace();
        }
        assertFalse( exceptionOccured );

    }

    public void testBase64Payload() {
        boolean exceptionOccured = false;
        try {
            ByteArrayInputStream payload = new ByteArrayInputStream( "Lorem Ipsum Dolor Si amet".getBytes() );
            Base64InputStream transformedPayload = new Base64InputStream( payload, Magic.ENCODE_WITH_BASE64 );
            ContentDecoratedInputStream cdmBase64PayloadStream = new EnvelopeDecoratedInputStream( transformedPayload,
                    TestHelper.getCdm1() );

            String actual2 = IOUtility.inputAsUTF8String( cdmBase64PayloadStream );
            assertEquals( expected2, actual2 );

        } catch ( Exception any ) {
            exceptionOccured = true;
            any.printStackTrace();
        }
        assertFalse( exceptionOccured );

    }

}
