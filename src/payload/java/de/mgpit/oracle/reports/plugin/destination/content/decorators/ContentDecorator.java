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

import javax.activation.MimeType;

import de.mgpit.oracle.reports.plugin.destination.content.types.Content;
import de.mgpit.oracle.reports.plugin.destination.content.types.InputModifier;
import de.mgpit.oracle.reports.plugin.destination.content.types.OutputModifier;
import de.mgpit.oracle.reports.plugin.destination.content.types.WithModel;
import oracle.reports.RWException;

/**
 * 
 * @author mgp
 *
 */
public abstract class ContentDecorator implements InputModifier, OutputModifier {

    /**
     * Creates a new ContentDecorator.
     */
    public ContentDecorator() {
        super();
    }

    
    /**
     * Decorates the {@code InputStream} given.
     * 
     * @param in
     *            the {@code InputStream} to decorate/wrap
     * @param parameters
     *            {@Properties} containing the values needed by the decorating {@code InputStream}'s {@code Content} model
     * @return the decorating {@code InputStream}
     * @throws RWException
     */
    public abstract InputStream forInput( final InputStream in, final Properties parameters ) throws RWException;

    /**
     * Applies the {@code Content} to an {@code OutputStream}.
     */
    public abstract OutputStream forOutput( final OutputStream out, final Properties parameters ) throws RWException;
    
    /**
     * Gets the {@code Content} of this decorator.
     * <p>
     * This is the content which will be added to the decorated object in some way,
     * e.g. as a header or as an envelope.
     * 
     * @return {@code Content} of this decorator
     */
    protected abstract Content getContent();

    /**
     * Gets the content model's mime type.
     * 
     * @return string denoting the mime type
     */
    public MimeType mimetype() {
        return getContent().mimetype();
    }

    /**
     * Gets the file extension which should be used for filenames of files storing data produced by this decorator.
     * 
     * @return string denoting the file extension
     */
    public String fileExtension() {
        return getContent().fileExtension();
    }

}