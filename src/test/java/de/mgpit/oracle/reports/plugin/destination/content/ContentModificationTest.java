package de.mgpit.oracle.reports.plugin.destination.content;


import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;

import de.mgpit.oracle.reports.plugin.destination.content.decorators.CdmDecorator;
import de.mgpit.oracle.reports.plugin.destination.content.transformers.Base64Transformer;
import junit.framework.TestCase;
import oracle.reports.RWException;

public class ContentModificationTest extends TestCase {

    public void testContentModification() {
        boolean noExceptionOccured = true;

        ContentModifier cdmModifier = new CdmDecorator();
        ContentModifier base64Modfier = new Base64Transformer();
        base64Modfier.followedBy( cdmModifier );

        ByteArrayInputStream stringInput = null;
        try {
            stringInput = new ByteArrayInputStream( "Lorem Ipsum Dolor Si Amet".getBytes( "UTF-8" ) );
        } catch ( UnsupportedEncodingException unsupportedEndoding ) {
            noExceptionOccured = false;
        }
        assertTrue( noExceptionOccured );
        assertNotNull( stringInput );

        try {
            base64Modfier.modify( stringInput, System.out );
        } catch ( RWException e ) {
            noExceptionOccured = false;
        }

        assertTrue( noExceptionOccured );
    }

}
