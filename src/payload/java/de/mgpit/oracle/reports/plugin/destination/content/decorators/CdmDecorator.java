package de.mgpit.oracle.reports.plugin.destination.content.decorators;


import java.io.InputStream;
import java.util.Properties;

import de.mgpit.oracle.reports.plugin.destination.content.EnvelopeInput;
import de.mgpit.oracle.reports.plugin.destination.content.ContentModificationPlugin;
import de.mgpit.oracle.reports.plugin.destination.content.io.EnvelopeWrappingInputStream;
import oracle.reports.RWException;

public abstract class CdmDecorator implements ContentModificationPlugin {

    public CdmDecorator() {
    };

    public InputStream wrap( final InputStream in, final Properties parameters ) throws RWException {
        return new EnvelopeWrappingInputStream( in, getCdm() );
    }

    protected abstract EnvelopeInput getCdm();
    
    public String mimetype() {
        return "application/xml";
    }

    public String fileExtension() {
        return "xml";
    }
}
