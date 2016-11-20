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


import javax.activation.MimeType;

/**
 * A Transformation used in the context of an Oracle&reg; Reports distribution.
 * <p>
 * Modifies the content of a Oracle&reg; Reports report on distribution of the generated report file(s).
 * Typical applications include
 * <ul>
 * <li>Filtering of the input stream</li>
 * <li>Encoding the input stream</li>
 * <li>or changing the encoding of the input stream on read/copy</li>
 * <li>Applying headers</li>
 * <li>Applying envelopes</li>
 * <li>...</li>
 * </ul>
 * on read or copy (writing to the output).
 * 
 * @author mgp
 */
public interface Modifier {

    /**
     * The prefix to use for the <em>name</em> of the properties of a {@code Destination} definition
     * in the {@code <reportservername>.conf} file used to declare content modifiers.  
     */
    public static String PROPERTY_NAME_PREFIX = "modifier.";

    public MimeType mimetype();

    public String fileExtension();

}
