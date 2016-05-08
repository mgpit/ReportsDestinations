package de.mgpit.oracle.reports.plugin.destination.content.transformers;

import java.io.InputStream;

import de.mgpit.oracle.reports.plugin.commons.U;
import de.mgpit.oracle.reports.plugin.destination.content.ContentModifier;
import oracle.reports.RWException;

/**
 * Abstract superclass for {@link ContentModifier}s which will transform the InputStream. 
 *  
 * @author mgp
 *
 */
public abstract class AbstractContentTransformer extends ContentModifier {

    final protected InputStream applyModification( final InputStream in ) throws RWException {
        U.Rw.assertNotNull( in );
        InputStream transformed = this.transform( in );
        return transformed;
    }
    
    protected abstract InputStream transform( InputStream content ) throws RWException;

}
