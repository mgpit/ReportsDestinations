package de.mgpit.oracle.reports.plugin.destination.content.decorators;


import java.io.InputStream;
import java.util.Properties;

import de.mgpit.oracle.reports.plugin.destination.content.ContentTransformationPlugin;
import de.mgpit.oracle.reports.plugin.destination.content.io.ChainingInputStream;
import oracle.reports.RWException;

public abstract class HeaderDecorator implements ContentTransformationPlugin {

    public HeaderDecorator() {
    }

    public InputStream wrap( InputStream in, Properties parameters ) throws RWException {
        return new ChainingInputStream( in, parameters );
    }

    public String mimetype() {
        return "application/xml";
    }

    public String fileExtension() {
        return "xml";
    }

}
