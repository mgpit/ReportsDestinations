package de.mgpit.oracle.reports.plugin.destination.content.transformers;

import java.io.InputStream;

import de.mgpit.oracle.reports.plugin.destination.content.decorators.ContentDecorator;
import oracle.reports.RWException;

public abstract class ContentTransformer extends ContentDecorator {

    protected InputStream decorate( InputStream content ) throws RWException {
        return this.transform( content );
    }
    
    protected abstract InputStream transform( InputStream content ) throws RWException;

}
