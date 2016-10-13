package de.mgpit.oracle.reports.plugin.destination.content.decorators;


import java.io.OutputStream;
import java.util.Properties;

import oracle.reports.RWException;

public interface OutputTransformation extends Transformation {

    public OutputStream forOutput( final OutputStream out, final Properties parameters ) throws RWException;

}
