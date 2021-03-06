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
package de.mgpit.oracle.reports.plugin.destination.content.types;


import java.nio.charset.Charset;
import java.util.Properties;

import javax.activation.MimeType;

/**
 * 
 * Content.
 * <p>
 * Used by {@link Modifier}s.
 * 
 * @author mgp
 *
 */
public interface Content {
    public static final String PROPERTY_NAME_PREFIX = "content.";
    public static final long UNDEFINED_LENGTH = -1;

    /**
     * Gets this {@code Content}'s content length in number of bytes.
     * <p>
     * This does <strong>not</strong> contain the number of bytes of an eventually payload, though!
     * 
     * @return number of bytes or {{@link #UNDEFINED_LENGTH} if this content hasn't been built, yet.
     */
    public long lengthInBytes();

    /**
     * Builds this Content.
     * <p>
     * The {@code Content} may have variable data. Parameters for populating this data can be passed as {@code Properties}.
     * 
     * @param parameters
     *            Properties for populating.
     * @throws Exception
     *             on errors during the build.
     */
    public void build( Properties parameters ) throws Exception;

    /**
     * Gets this {@code Content}'s encoding.
     * <p>
     * The encoding should be set appropriately and must be used for converting between {@link java.lang.Character charaters}
     * and {@link java.lang.Byte bytes} when writing to or reading from a stream.
     * 
     * @return this {@code Content}'s encoding.
     */
    public Charset encoding();

    /**
     * Gets this {@code Content}'s mime type.
     * 
     * @return this {@code Content}'s mime type.
     */
    public MimeType mimetype();

    /**
     * Gets this {@code Content}'s mime type.
     * 
     * @return this {@code Content}'s file extension.
     */
    public String fileExtension();

}
