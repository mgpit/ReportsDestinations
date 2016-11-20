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


import de.mgpit.oracle.reports.plugin.destination.content.types.Content;
import de.mgpit.oracle.reports.plugin.destination.content.types.WithModel;

/**
 * An {@code EnvelopeDecorator} with pluggable {@code Content}
 * 
 * @author mgp
 *
 */
public class PluggableContentEnvelopeDecorator extends EnvelopeDecorator implements WithModel {

    private Content contentModel;

    public Content getContent() {
        return getContentModel();
    }

    private Content getContentModel() {
        return this.contentModel;
    }

    public void setContentModel( Content content ) {
        this.contentModel = content;
    }

}
