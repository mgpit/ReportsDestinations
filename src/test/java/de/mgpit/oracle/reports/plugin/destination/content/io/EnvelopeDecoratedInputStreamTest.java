package de.mgpit.oracle.reports.plugin.destination.content.io;


import java.io.ByteArrayInputStream;

import org.apache.commons.codec.binary.Base64InputStream;

import de.mgpit.oracle.reports.plugin.commons.Magic;
import de.mgpit.oracle.reports.plugin.commons.io.IOUtility;
import de.mgpit.oracle.reports.plugin.destination.content.io.EnvelopeDecoratedInputStream;
import junit.framework.TestCase;

public class EnvelopeDecoratedInputStreamTest extends TestCase {
    
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
        EnvelopeDecoratedInputStream cdmPlainPayloadStream = new EnvelopeDecoratedInputStream( payload, TestHelper.getCdm1() );

        boolean exceptionOccured = false;
        try {
            String actual1 = IOUtility.inputAsUTF8String( cdmPlainPayloadStream );
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
        EnvelopeDecoratedInputStream cdmBase64PayloadStream = new EnvelopeDecoratedInputStream( transformedPayload, TestHelper.getCdm1() );

        boolean exceptionOccured = false;
        try {
            String actual2 = IOUtility.inputAsUTF8String( cdmBase64PayloadStream );
            assertEquals( expected2, actual2 );
            
        } catch ( Exception any ) {
            exceptionOccured = true;
            any.printStackTrace();
        }
        assertFalse( exceptionOccured );

    }
    
    
}
