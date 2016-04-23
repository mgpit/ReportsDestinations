package de.mgpit.oracle.reports.plugin.destination.content.decorators;


import java.io.InputStream;

import de.mgpit.oracle.reports.plugin.destination.content.ContentModifier;
import oracle.reports.RWException;

/**
 * Abstract superclass for {@link ContentModifier}s which will decorate the InputStream.
 * 
 * @author mgpit
 *
 */
public abstract class ContentDecorator extends ContentModifier {

    final protected InputStream applyModification( InputStream in ) throws RWException {
        InputStream decorated =  this.decorate( in );
        return decorated;
    }

    protected abstract InputStream decorate( InputStream in ) throws RWException;

}
