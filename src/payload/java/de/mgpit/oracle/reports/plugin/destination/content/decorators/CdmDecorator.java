package de.mgpit.oracle.reports.plugin.destination.content.decorators;


import java.io.InputStream;

import de.mgpit.oracle.reports.plugin.destination.cdm.Cdm;
import de.mgpit.oracle.reports.plugin.destination.cdm.CdmDecoratedInputStream;
import de.mgpit.oracle.reports.plugin.destination.content.ContentModificationPlugin;
import oracle.reports.RWException;

public abstract class CdmDecorator implements ContentModificationPlugin {

    public CdmDecorator() {
    };

    public InputStream wrap( final InputStream in ) throws RWException {
        return new CdmDecoratedInputStream( in, getCdm() );
    }

    protected abstract Cdm getCdm();
}
