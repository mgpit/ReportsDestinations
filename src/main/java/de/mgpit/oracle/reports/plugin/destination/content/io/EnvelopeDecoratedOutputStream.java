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


import java.io.BufferedOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import de.mgpit.oracle.reports.plugin.commons.Magic;
import de.mgpit.oracle.reports.plugin.commons.U;
import de.mgpit.oracle.reports.plugin.destination.content.types.Envelope;

/**
 * An {@code OutputStream} decorated with an {@code Envelope}.
 *  
 * @author mgp
 *
 */
public class EnvelopeDecoratedOutputStream extends FilterOutputStream {

    private final Envelope envelope;

    public EnvelopeDecoratedOutputStream( final OutputStream toWrap, final Envelope envelope ) {
        super( toWrap );
        U.assertNotNull( toWrap, "Cannot prepend a null OutputStream!" );
        U.assertNotNull( envelope, "Cannot instantiate without Envelope!" );
        this.envelope = envelope;
    }

    public void write( int b ) throws IOException {
        if ( !envelope.wantsData() ) {
            envelope.writeToOut( out );
        }

        if ( envelope.wantsData() ) {
            out.write( b );
        } else {
            throw new IOException( "Cannot write into an Envelope which is not in state \"data wanted\"!" );
        }
    }

    public void flush() throws IOException {
        if ( !envelope.wantsData() ) {
            throw new IOException( "Cannot flush an Envelope which is not in state \"data wanted\"!" );
        }
        envelope.dataFinished();
        envelope.writeToOut( out );
        out.flush();
    }

    /*
     * The inherited close does the right thing ...
     * <ul>
     * <li>flushing...</li>
     * <li>and then closing the wrapped <code>out</code>
     * </ul>
     */

}
