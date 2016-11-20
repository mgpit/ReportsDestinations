package de.mgpit.oracle.reports.plugin.destination.content.io;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.apache.commons.codec.binary.Base64InputStream;

import de.mgpit.oracle.reports.plugin.commons.Magic;
import de.mgpit.oracle.reports.plugin.commons.io.IOUtility;
import de.mgpit.oracle.reports.plugin.destination.content.io.EnvelopeDecoratedOutputStream;
import junit.framework.TestCase;

public class EnvelopeDecoratedOutputStreamTest extends TestCase {
    
    private static final String expected1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
            "<cdmdoc>\n" + 
            "    <data>\n" + 
            "Lorem Ipsum Dolor Si amet\n" + 
            "    </data>\n" + 
            "</cdmdoc>";
    
    private static final String expected2 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
            "<cdmdoc>\n" + 
            "    <data>\n" + 
            "TG9yZW0gSXBzdW0gRG9sb3IgU2kgYW1ldA==\r\n" + // !!! Base64 encoding applies platform line separator
            "\n" + 
            "    </data>\n" + 
            "</cdmdoc>";
            

    public void testPlainPayload() {
        ByteArrayInputStream payload = new ByteArrayInputStream( "Lorem Ipsum Dolor Si amet".getBytes() );
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        EnvelopeDecoratedOutputStream cdmPlainPayloadStream = new EnvelopeDecoratedOutputStream( out, TestHelper.getCdm1() );

        boolean exceptionOccured = false;
        try {
            IOUtility.copyFromToAndThenClose( payload, cdmPlainPayloadStream );
            String actual1 = new String( out.toByteArray() );
            assertEquals( expected1, actual1 );
        } catch ( Exception any ) {
            exceptionOccured = true;
            any.printStackTrace();
        }
        assertFalse( exceptionOccured );

    }
    
    public void testBase64Payload() {
        ByteArrayInputStream payload = new ByteArrayInputStream( "Lorem Ipsum Dolor Si amet".getBytes() );
        Base64InputStream transformedPayload = new Base64InputStream( payload, Magic.ENCODE_WITH_BASE64 );
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        EnvelopeDecoratedOutputStream cdmPlainPayloadStream = new EnvelopeDecoratedOutputStream( out, TestHelper.getCdm1() );

        boolean exceptionOccured = false;
        try {
            IOUtility.copyFromToAndThenClose( transformedPayload, cdmPlainPayloadStream );
            String actual2 = new String( out.toByteArray() );
            assertEquals( expected2, actual2 );
        } catch ( Exception any ) {
            exceptionOccured = true;
            any.printStackTrace();
        }
        assertFalse( exceptionOccured );

    }    
    
}
