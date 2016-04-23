package de.mgpit.oracle.reports.plugin.destination.content;

import java.io.ByteArrayInputStream;

import de.mgpit.oracle.reports.plugin.destination.content.decorators.CdmDecorator;
import de.mgpit.oracle.reports.plugin.destination.content.transformers.Base64Transformer;
import junit.framework.TestCase;
import oracle.reports.RWException;

public class ContentModificationTest extends TestCase {
    
    public void testContentModification() {
        
        ContentModifier cdm = new CdmDecorator();
        ContentModifier base64 = new Base64Transformer();
        base64.followedBy( cdm );
        
        
        ByteArrayInputStream stringInput = new ByteArrayInputStream( "FooBar1".getBytes() ); 
        
        try {
            base64.modify( stringInput, System.out );
        } catch ( RWException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
    }

}
