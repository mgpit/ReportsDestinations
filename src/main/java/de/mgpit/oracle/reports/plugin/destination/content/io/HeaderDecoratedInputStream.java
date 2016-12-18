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


import java.io.IOException;
import java.io.InputStream;

import de.mgpit.oracle.reports.plugin.commons.Magic;
import de.mgpit.oracle.reports.plugin.destination.content.types.Header;

/**
 * An {@code InputStream} decorated with a {@code Header}.
 * 
 * @author mgp
 *
 */
public class HeaderDecoratedInputStream extends ContentDecoratedInputStream {

    public HeaderDecoratedInputStream( InputStream toBeDecorated, Header header ) {
        super( toBeDecorated, header );
    }

    public int read() throws IOException {
        switch ( currentState ) {
        case ContentDecoratedInputStream.CONTENT_NEW:
            openHeader();
            nextState();
            return read();
        case HEADER_OPENED: {
            final int aByte = decorationData.read();
            if ( aByte == Magic.END_OF_STREAM ) {
                closeData();
                nextState();
                return read();
            } else {
                return aByte;
            }
        }
        case HEADER_PAYLOAD: {
            final int aByte = in.read();
            if ( aByte == Magic.END_OF_STREAM ) {
                nextState();
                return read();
            } else {
                return aByte;
            }
        }
        case HEADER_CLOSED: {
            return Magic.END_OF_STREAM;
        }
        default:
            return Magic.END_OF_STREAM;
        }
    }

    protected Header getHeader() {
        return (Header) getDecorator();
    }

    private void openHeader() {
        setDataForDecoration( getHeader().get() );
    }

    protected boolean inPayload() {
        return currentState == HEADER_PAYLOAD;
    }

    protected void nextState() {
        switch ( currentState ) {
        case ContentDecoratedInputStream.CONTENT_NEW:
            currentState = HEADER_OPENED;
            break;
        case HEADER_OPENED:
            currentState = HEADER_PAYLOAD;
            break;
        case HEADER_PAYLOAD:
            currentState = HEADER_CLOSED;
            break;
        }
    }

    private static final int HEADER_OPENED = 10;
    private static final int HEADER_PAYLOAD = 20;
    private static final int HEADER_CLOSED = 40;

    private static String name( int state ) {
        final String stateName;
        switch ( state ) {
        case ContentDecoratedInputStream.CONTENT_NEW:
            stateName = "New Header";
            break;
        case HEADER_OPENED:
            stateName = "Header Opened";
            break;
        case HEADER_PAYLOAD:
            stateName = "Header Payload";
            break;
        case HEADER_CLOSED:
            stateName = "Header Closed";
            break;
        default:
            stateName = "Unknown";
            break;
        }
        return stateName;
    }
}
