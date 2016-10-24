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
import de.mgpit.oracle.reports.plugin.destination.content.types.InputTransformation;
import de.mgpit.oracle.reports.plugin.destination.content.types.OutputTransformation;
import oracle.reports.RWException;

/**
 * A Decorator for applying {@link Envelope}s to a Stream.
 * 
 * @author mgp
 *
 */
public abstract class EnvelopeDecorator implements InputTransformation, OutputTransformation {

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
     * Gets an Envelope.
     * 
     * @param parameters
     *            which may be used by the Envelope
     * @return an Envelope
     */
    protected abstract Envelope getEnvelope( Properties parameters );

    /**
     * Gets the envelope's mime type.
     * 
     * @return string denoting the mime type
     */
    public String mimetype() {
        return "application/xml";
    }

    /**
     * Gets the file extension which should be used for filenames of files storing data produced by this decorator.
     * 
     * @return string denoting the file extension
     */
    public String fileExtension() {
        return "xml";
    }
}
