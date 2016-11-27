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


import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

import de.mgpit.oracle.reports.plugin.commons.Magic;
import de.mgpit.oracle.reports.plugin.commons.U;
import de.mgpit.oracle.reports.plugin.destination.content.types.Envelope;

/**
 * An {@code InputStream} decorated with an {@code Envelope}.
 * 
 * @author mgp
 *
 */
public class EnvelopeDecoratedInputStream extends FilterInputStream {

    /**
     * Envelope for wrapping.
     */
    private final Envelope envelope;

    public EnvelopeDecoratedInputStream( InputStream toBeDecorated, Envelope envelope ) {
        super( toBeDecorated );
        U.assertNotNull( toBeDecorated, "Cannot wrap a null InputStream!" );
        U.assertNotNull( envelope, "Cannot wrap with a null Envelope!" );
        this.envelope = envelope;
    }

    public int read() throws IOException {
        final int aByte;
        if ( envelope.wantsData() ) {
            aByte = in.read();
            if ( aByte == Magic.END_OF_STREAM ) {
                envelope.dataFinished();
                return this.read();
            }
        } else {
            aByte = envelope.read();
        }
        return aByte;
    }

    public synchronized int available() throws IOException {
        return envelope.wantsData() ? in.available() : 0;
    }
}
