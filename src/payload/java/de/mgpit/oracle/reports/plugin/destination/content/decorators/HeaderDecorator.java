package de.mgpit.oracle.reports.plugin.destination.content.decorators;


import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import de.mgpit.oracle.reports.plugin.destination.content.io.HeaderDecoratedInputStream;
import de.mgpit.oracle.reports.plugin.destination.content.io.HeaderDecoratedOutputStream;
import de.mgpit.oracle.reports.plugin.destination.content.types.Header;
import de.mgpit.oracle.reports.plugin.destination.content.types.InputTransformation;
import de.mgpit.oracle.reports.plugin.destination.content.types.OutputTransformation;
import oracle.reports.RWException;

public abstract class HeaderDecorator implements InputTransformation, OutputTransformation {

    public HeaderDecorator() {
    }

    public InputStream forInput( InputStream in, Properties parameters ) throws RWException {
        return new HeaderDecoratedInputStream( in, getHeader( parameters ) );
    }

    public OutputStream forOutput( OutputStream out, Properties parameters ) throws RWException {
        return new HeaderDecoratedOutputStream( out, getHeader( parameters ) );
    }
    
    protected abstract Header getHeader( Properties parameters );

    public String mimetype() {
        return "application/xml";
    }

    public String fileExtension() {
        return "xml";
    }

}
