package de.mgpit.oracle.reports.plugin.destination.content.decorators;


import java.io.InputStream;

import de.mgpit.oracle.reports.plugin.commons.U;
import de.mgpit.oracle.reports.plugin.destination.content.ContentModifier;
import oracle.reports.RWException;

/**
 * Abstract superclass for {@link ContentModifier}s which will decorate the InputStream.
 * 
 * @author mgpit
 *
 */
public abstract class AbstractContentDecorator extends ContentModifier {

    final protected InputStream applyModification( InputStream in ) throws RWException {
        U.Rw.assertNotNull( in );
        InputStream decorated =  this.decorate( in );
        return decorated;
    }

    protected abstract InputStream decorate( InputStream in ) throws RWException;

}
