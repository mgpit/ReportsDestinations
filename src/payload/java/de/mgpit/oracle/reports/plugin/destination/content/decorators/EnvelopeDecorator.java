package de.mgpit.oracle.reports.plugin.destination.content.decorators;


import java.io.InputStream;
import java.util.Properties;

import de.mgpit.oracle.reports.plugin.destination.content.EnvelopeInput;
import de.mgpit.oracle.reports.plugin.destination.content.ContentTransformationPlugin;
import de.mgpit.oracle.reports.plugin.destination.content.io.EnvelopeWrappingInputStream;
import oracle.reports.RWException;

public abstract class EnvelopeDecorator implements ContentTransformationPlugin {

    public EnvelopeDecorator() {
    };

    public InputStream wrap( final InputStream in, final Properties parameters ) throws RWException {
        return new EnvelopeWrappingInputStream( in, getCdm( parameters ) );
    }

    protected abstract EnvelopeInput getCdm( Properties parameters );

    public String mimetype() {
        return "application/xml";
    }

    public String fileExtension() {
        return "xml";
    }
}
