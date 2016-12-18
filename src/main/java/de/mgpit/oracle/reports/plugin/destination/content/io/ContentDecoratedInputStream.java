package de.mgpit.oracle.reports.plugin.destination.content.io;


import java.io.IOException;
import java.io.InputStream;

import de.mgpit.oracle.reports.plugin.commons.U;
import de.mgpit.oracle.reports.plugin.destination.content.types.Content;

public abstract class ContentDecoratedInputStream extends InputStream {
    public static final int CONTENT_NEW = 0;

    protected int currentState = CONTENT_NEW;
    /**
     * Content for wrapping.
     */
    private final Content decorator;
    /**
     * Data provided by the decoration {@code Content}
     */
    protected InputStream decorationData;

    /**
     * Input Stream to be wrapped
     */
    InputStream in;

    public ContentDecoratedInputStream( InputStream toBeDecorated, Content decoration ) {
        this.in = toBeDecorated;
        this.decorator = decoration;
        U.assertNotNull( toBeDecorated, "Cannot wrap a null InputStream!" );
        U.assertNotNull( decoration, "Cannot wrap with a null Envelope!" );
        currentState = CONTENT_NEW;
    }

    protected Content getDecorator() {
        return decorator;
    }

    protected void closeData() {
        if ( decorationData != null ) {
            try {
                decorationData.close();
            } catch ( IOException ignore ) {}
        }
    }

    protected void setDataForDecoration( InputStream data ) {
        this.decorationData = data;
    }

    public synchronized int available() throws IOException {
        return inPayload() ? in.available() : 0;
    }

    protected abstract boolean inPayload();

    protected abstract void nextState();

    /**
     * Closes this input stream and releases any system resources
     * associated with the stream.
     * This
     * method simply performs <code>in.close()</code>.
     *
     * @exception IOException
     *                if an I/O error occurs.
     * @see java.io.FilterInputStream#in
     */
    public void close() throws IOException {
        closeData();
        in.close();
    }

}