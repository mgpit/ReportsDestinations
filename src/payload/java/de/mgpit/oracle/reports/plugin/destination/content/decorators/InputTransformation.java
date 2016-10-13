package de.mgpit.oracle.reports.plugin.destination.content.decorators;


import java.io.InputStream;
import java.util.Properties;

import oracle.reports.RWException;

public interface InputTransformation {

    public InputStream forInput( final InputStream in, final Properties parameters ) throws RWException;
}
