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
import de.mgpit.oracle.reports.plugin.destination.content.types.Header;

/**
 * An {@code InputStream} decorated with a {@code Header}.
 *  
 * @author mgp
 *
 */
public class HeaderDecoratedInputStream extends FilterInputStream {

    /**
     * Envelope for wrapping.
     */
    private final Header header;

    public HeaderDecoratedInputStream( InputStream toBeDecorated, Header header ) {
        super( toBeDecorated );
        U.assertNotNull( toBeDecorated, "Cannot prepend a null InputStream!" );
        U.assertNotNull( header, "Cannot instantiate without properties!" );
        this.header = header;
    }

    public int read() throws IOException {
        final int aByte;
        if ( header.wantsData() ) {
            aByte = in.read();
            if ( aByte == Magic.END_OF_STREAM ) {
                header.dataFinished();
                return this.read();
            }
        } else {
            aByte = header.read();
        }
        return aByte;
    }

    public synchronized int available() throws IOException {
        return header.wantsData() ? in.available() : 0;
    }
}
