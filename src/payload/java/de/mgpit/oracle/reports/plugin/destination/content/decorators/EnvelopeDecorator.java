package de.mgpit.oracle.reports.plugin.destination.content.decorators;


import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import de.mgpit.oracle.reports.plugin.destination.content.io.EnvelopeDecoratedInputStream;
import de.mgpit.oracle.reports.plugin.destination.content.io.EnvelopeDecoratedOutputStream;
import de.mgpit.oracle.reports.plugin.destination.content.types.Envelope;
import de.mgpit.oracle.reports.plugin.destination.content.types.InputTransformation;
import de.mgpit.oracle.reports.plugin.destination.content.types.OutputTransformation;
import oracle.reports.RWException;

public abstract class EnvelopeDecorator implements InputTransformation, OutputTransformation {

    public EnvelopeDecorator() {
    }

    public InputStream forInput( final InputStream in, final Properties parameters ) throws RWException {
        return new EnvelopeDecoratedInputStream( in, getEnvelope( parameters ) );
    }
    
    public OutputStream forOutput( final OutputStream out, final Properties parameters ) throws RWException {
        return new EnvelopeDecoratedOutputStream( out, getEnvelope( parameters ) );
    }

    protected abstract Envelope getEnvelope( Properties parameters );

    public String mimetype() {
        return "application/xml";
    }

    public String fileExtension() {
        return "xml";
    }
}
