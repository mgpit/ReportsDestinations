/*
 * Copyright 2016 Marco Pauls www.mgp-it.de
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

/**
 * @license APACHE-2.0
 */
package de.mgpit.oracle.reports.plugin.destination.content.io;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import org.apache.log4j.Logger;

import de.mgpit.oracle.reports.plugin.commons.U;
import de.mgpit.oracle.reports.plugin.commons.Units;
import de.mgpit.oracle.reports.plugin.commons.io.IOUtility;
import de.mgpit.oracle.reports.plugin.destination.content.types.BufferingHeader;
import de.mgpit.oracle.reports.plugin.destination.content.types.Header;

/**
 * @author mgp
 *
 */
public class BufferingHeaderOutputStream extends OutputStream {

    private final OutputStream out;
    private final Header header;
    private final WriteBuffer writeBuffer;

    private final Properties parameters;
    private boolean finished;

    public BufferingHeaderOutputStream( OutputStream toWrap, Header header, Properties parameters ) {
        super();
        U.assertNotNull( toWrap, "Cannot prepend a null OutputStream!" );
        U.assertNotNull( header, "Cannot instantiate without Header!" );
        this.out = toWrap;
        this.header = header;
        this.parameters = parameters;
        this.writeBuffer = new WriteBuffer();
        this.finished = false;
    }

    public void write( int b ) throws IOException {
        writeBuffer.write( b );
    }

    /**
     * Flushes this output stream.
     * <p>
     * Will flush the {@code Header} to the wrapped {@code Outputstream} and
     * then flush the wrapped {@code Outputstream} itself.
     * 
     * @see java.io.OutputStream#flush()
     */
    public void flush() throws IOException {
        out.flush();
    }
    
    private void finishWrite() throws IOException {
        if ( !finished ) {
            writeHeader();
            writeBuffer.flushTo( out );
            out.flush();
            finished = true;
        }        
    }

    // TODO: Delegate this to the Header??? e.g. Header::writeToOut(OutputStream)?
    private void writeHeader() throws IOException {
        parameters.put( BufferingHeader.SIZE_PROPERTY, writeBuffer.byteCount() );
        try {
            header.build( parameters );
        } catch ( Exception toBeWrapped ) {
            IOException ioex = new IOException( "Cannot Write Header: " + toBeWrapped.getMessage() );
            ioex.initCause( toBeWrapped );
            throw ioex;
        }
        final InputStream source = header.get();
        IOUtility.copyFromTo( source, out );
        try {
            source.close();
        } catch ( Exception ignore ) {}
    }

    /**
     * Closes this output stream.
     * <p>
     * Will flush this output stream and then close the wrapped {@code Outputstream}.
     * 
     * @see java.io.OutputStream#close()
     */
    public void close() throws IOException {
        try {
            flush();
            finishWrite();
        } catch ( IOException ignored ) {}
        out.close();
    }

    private class WriteBuffer {
        private static final int IN_MEMORY_BUFFER_SIZE = Units.ONE_MEGABYTE*4;
        private OutputStream buffer;
        private int bytesWritten;
        private boolean bufferingToFile;
        File tmpFile;

        public WriteBuffer() {
            this.bufferingToFile = false;
            this.buffer = new ByteArrayOutputStream( IN_MEMORY_BUFFER_SIZE );
            this.bytesWritten = 0;
        }

        private void write( int b ) throws IOException {
            buffer.write( b );
            bytesWritten++;
            if ( !bufferingToFile && bytesWritten > IN_MEMORY_BUFFER_SIZE ) {
                switchToFileBasedBuffer();
            }
        }

        private void switchToFileBasedBuffer() throws IOException {
            Logger.getRootLogger().info( "Switching from In Memory Buffer to File Buffer at size " + U.w( bytesWritten ) );
            final String prefix = "FrameworkHeaderOutputStream_";
            final String suffix = ".buf";
            tmpFile = File.createTempFile( prefix, suffix );
            final InputStream source = out2in( buffer );
            OutputStream destination = new FileOutputStream( tmpFile );
            IOUtility.copyFromTo( source, destination );
            try {
                source.close();
            } catch ( IOException ignore ) {}
            buffer = destination;
            bufferingToFile = true;
        }

        private InputStream out2in( OutputStream some ) {
            final ByteArrayOutputStream byteOut = (ByteArrayOutputStream) some;
            final InputStream byteIn = new ByteArrayInputStream( byteOut.toByteArray() );
            return byteIn;
        }

        public String byteCount() {
            return String.valueOf( bytesWritten );
        }

        private void flushTo( OutputStream destination ) throws IOException {
            buffer.close();
            final InputStream source = bufferingToFile ? IOUtility.inputStreamFromFile( tmpFile ) : out2in( buffer );
            IOUtility.copyFromTo( source, destination );
            if ( bufferingToFile ) {
                try {
                    tmpFile.delete();
                } catch ( Exception ignore ) {}
            }
        }
    }
}
