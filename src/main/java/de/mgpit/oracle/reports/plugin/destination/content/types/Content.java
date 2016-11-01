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

import java.io.IOException;
import java.io.OutputStream;

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
    
    /**
     * Reads the contents next byte
     * @return byte read or {@link de.mgpit.oracle.reports.plugin.commons.Magic Magic#END_OF_STREAM}
     */
    public int read();
    
    /**
     * 
     * @return {@code true} if the payload should be provided, now,{@code false} else
     */
    public boolean dataWanted();
    
    /**
     * Get the signal that the payload is finished
     */
    public void setDataFinished();
    
    /**
     * Write this to the given output stream.
     * 
     * @param out the output stream
     * @throws IOException if any error occurs
     */
    public void writeToOut( OutputStream out ) throws IOException;


}
