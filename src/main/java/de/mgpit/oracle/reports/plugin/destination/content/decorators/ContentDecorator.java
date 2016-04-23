package de.mgpit.oracle.reports.plugin.destination.content.decorators;


import java.io.InputStream;

import de.mgpit.oracle.reports.plugin.destination.content.ContentModifier;
import oracle.reports.RWException;

public abstract class ContentDecorator extends ContentModifier {

    protected InputStream applyModification( InputStream in ) throws RWException{
        return this.decorate( in );

    }

    protected abstract InputStream decorate( InputStream in ) throws RWException;

}
