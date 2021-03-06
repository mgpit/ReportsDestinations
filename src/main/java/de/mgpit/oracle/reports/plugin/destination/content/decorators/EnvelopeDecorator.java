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
package de.mgpit.oracle.reports.plugin.destination.content.decorators;


import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import de.mgpit.oracle.reports.plugin.destination.content.io.EnvelopeDecoratedInputStream;
import de.mgpit.oracle.reports.plugin.destination.content.io.EnvelopeDecoratedOutputStream;
import de.mgpit.oracle.reports.plugin.destination.content.types.Envelope;
import oracle.reports.RWException;
import oracle.reports.utility.Utility;

/**
 * A Decorator for applying an {@link Envelope} around the content of a Stream.
 * 
 * @author mgp
 *
 */
public abstract class EnvelopeDecorator extends ContentDecorator {

    public EnvelopeDecorator() {
    }

    /**
     * Applies an {@code Envelope} to an {@code InputStream}.
     */
    public InputStream forInput( final InputStream in, final Properties parameters ) throws RWException {
        return new EnvelopeDecoratedInputStream( in, getEnvelope( parameters ) );
    }

    /**
     * Applies an {@code Envelope} to an {@code OutputStream}.
     */
    public OutputStream forOutput( final OutputStream out, final Properties parameters ) throws RWException {
        return new EnvelopeDecoratedOutputStream( out, getEnvelope( parameters ) );
    }

    /**
     * Gets the Envelope.
     * 
     * @return an Envelope
     */
    protected Envelope getEnvelope( Properties parameters ) throws RWException {
        try {
            Envelope envelope = (Envelope) getContent();
            envelope.build( parameters );
            return envelope;
        } catch ( Exception toBeWrapped ) {
            throw Utility.newRWException( toBeWrapped );
        }
    }

}
