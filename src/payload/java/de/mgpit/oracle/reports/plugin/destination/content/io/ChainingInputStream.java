package de.mgpit.oracle.reports.plugin.destination.content.io;


import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import de.mgpit.oracle.reports.plugin.commons.Magic;
import de.mgpit.oracle.reports.plugin.commons.U;

public class ChainingInputStream extends InputStream {

    /**
     * Input to be wrapped.
     */
    private final InputStream nextInput;
    /**
     * Envelope for wrapping.
     */
    private final Properties properties;

    public ChainingInputStream( InputStream input, Properties properties ) {
        U.assertNotNull( input, "Cannot prepend a null InputStream!" );
        U.assertNotNull( properties, "Cannot instantiate without properties!" );
        this.nextInput = input;
        this.properties = properties;
    }

    protected int next() {
        return Magic.END_OF_STREAM;
    };
    protected boolean atEnd() {
        return true;
    }

    public int read() throws IOException {
        return this.atEnd()?nextInput.read():this.next();
    }

    public synchronized int available() throws IOException {
        return this.atEnd() ? 0 : nextInput.available();
        
    }
}
