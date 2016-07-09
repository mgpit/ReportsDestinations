package de.mgpit.oracle.reports.plugin.destination.content.decorators;


import java.io.InputStream;

import de.mgpit.oracle.reports.plugin.commons.io.CdmDecoratedInputStream;
import de.mgpit.oracle.reports.plugin.destination.cdm.Cdm;
import oracle.reports.RWException;

public abstract class CdmDecorator extends AbstractContentDecorator {

    protected InputStream decorate( final InputStream in ) throws RWException {
        return new CdmDecoratedInputStream( in, getCdm() );                
    }
    
    public abstract Cdm getCdm();

}
