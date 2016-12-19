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


import java.io.OutputStream;
import java.util.Properties;

import javax.activation.MimeType;

import de.mgpit.oracle.reports.plugin.destination.content.io.BufferingHeaderOutputStream;
import de.mgpit.oracle.reports.plugin.destination.content.types.BufferingHeader;
import de.mgpit.oracle.reports.plugin.destination.content.types.Content;
import de.mgpit.oracle.reports.plugin.destination.content.types.Header;
import de.mgpit.oracle.reports.plugin.destination.content.types.OutputModifier;
import de.mgpit.oracle.reports.plugin.destination.content.types.WithModel;
import oracle.reports.RWException;
import oracle.reports.utility.Utility;

/**
 * @author mgp
 *         <p>
 *
 *         Don't inherit from PluggableContentHeaderDecorator - don't want to implement InputModifier
 */
public class BufferingHeaderDecorator implements WithModel, OutputModifier {

    private BufferingHeader bufferingHeader;

    /*
     * (non-Javadoc)
     * 
     * @see de.mgpit.oracle.reports.plugin.destination.content.types.Modifier#mimetype()
     */
    public MimeType mimetype() {
        return bufferingHeader.mimetype();
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.mgpit.oracle.reports.plugin.destination.content.types.Modifier#fileExtension()
     */
    public String fileExtension() {
        return bufferingHeader.fileExtension();
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.mgpit.oracle.reports.plugin.destination.content.types.OutputModifier#forOutput(java.io.OutputStream, java.util.Properties)
     */
    /**
     * Applies a {@code Header} to an {@code OutputStream}.
     */
    public OutputStream forOutput( OutputStream out, Properties parameters ) throws RWException {
        return new BufferingHeaderOutputStream( out, bufferingHeader, parameters );
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.mgpit.oracle.reports.plugin.destination.content.types.WithModel#setContentModel(de.mgpit.oracle.reports.plugin.destination.content.types.Content)
     */
    public void setContentModel( Content content ) {

        if ( !BufferingHeader.class.isAssignableFrom( content.getClass() ) ) {
            throw new IllegalArgumentException(
                    "Illegal Argument: " + content.getClass().getName() + " is not a " + BufferingHeader.class.getName() );
        }
        this.bufferingHeader = (BufferingHeader) content;
    }
}
