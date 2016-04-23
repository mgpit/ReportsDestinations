package de.mgpit.oracle.reports.plugin.destination.content.transformers;

import java.io.InputStream;

import de.mgpit.oracle.reports.plugin.destination.content.ContentModifier;
import oracle.reports.RWException;

/**
 * Abstract superclass for {@link ContentModifier}s which will transform the InputStream. 
 *  
 * @author mgpit
 *
 */
public abstract class ContentTransformer extends ContentModifier {

    final protected InputStream applyModification( InputStream in ) throws RWException {
        InputStream transformed = this.transform( in );
        return transformed;
    }
    
    protected abstract InputStream transform( InputStream content ) throws RWException;

}
