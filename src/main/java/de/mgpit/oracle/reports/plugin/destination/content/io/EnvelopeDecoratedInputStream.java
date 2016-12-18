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
import de.mgpit.oracle.reports.plugin.destination.content.types.Envelope;

/**
 * An {@code InputStream} decorated with an {@code Envelope}.
 * 
 * @author mgp
 *
 */
public class EnvelopeDecoratedInputStream extends ContentDecoratedInputStream {

    public EnvelopeDecoratedInputStream( InputStream toBeDecorated, Envelope envelope ) {
        super( toBeDecorated, envelope );
    }

    public int read() throws IOException {
        switch ( currentState ) {
        case ContentDecoratedInputStream.CONTENT_NEW:
            openEnvelopeBefore();
            nextState();
            return read();
        case ENVELOPE_OPENED: {
            final int aByte = decorationData.read();
            if ( aByte == Magic.END_OF_STREAM ) {
                closeData();
                nextState();
                return read();
            } else {
                return aByte;
            }
        }
        case ENVELOPE_PAYLOAD: {
            final int aByte = in.read();
            if ( aByte == Magic.END_OF_STREAM ) {
                openEnvelopeAfter();
                nextState();
                return read();
            } else {
                return aByte;
            }
        }
        case ENVELOPE_CLOSING: {
            final int aByte = decorationData.read();
            if ( aByte == Magic.END_OF_STREAM ) {
                closeData();
                nextState();
            }
            return aByte;
        }
        case ENVELOPE_CLOSED: {
            return Magic.END_OF_STREAM;
        }
        default:
            return Magic.END_OF_STREAM;
        }
    }

    protected Envelope getEnvelope() {
        return (Envelope) getDecorator();
    }

    private void openEnvelopeBefore() {
        setDataForDecoration( getEnvelope().getBeforePayload() );
    }

    private void openEnvelopeAfter() {
        setDataForDecoration( getEnvelope().getAfterPayload() );
    }

    protected boolean inPayload() {
        return currentState == ENVELOPE_PAYLOAD;
    }

    protected void nextState() {
        switch ( currentState ) {
        case ContentDecoratedInputStream.CONTENT_NEW:
            currentState = ENVELOPE_OPENED;
            break;
        case ENVELOPE_OPENED:
            currentState = ENVELOPE_PAYLOAD;
            break;
        case ENVELOPE_PAYLOAD:
            currentState = ENVELOPE_CLOSING;
            break;
        case ENVELOPE_CLOSING:
            currentState = ENVELOPE_CLOSED;
            break;
        }
    }

    static final int ENVELOPE_OPENED = 10;
    static final int ENVELOPE_PAYLOAD = 20;
    static final int ENVELOPE_CLOSING = 30;
    static final int ENVELOPE_CLOSED = 40;

    private static String name( int state ) {
        final String stateName;
        switch ( state ) {
        case ContentDecoratedInputStream.CONTENT_NEW:
            stateName = "New Envelope";
            break;
        case ENVELOPE_OPENED:
            stateName = "Envelope Opened";
            break;
        case ENVELOPE_CLOSING:
            stateName = "Envelope Payload";
            break;
        case ENVELOPE_PAYLOAD:
            stateName = "Envelope Closing";
            break;
        case ENVELOPE_CLOSED:
            stateName = "Envelope Closed";
            break;
        default:
            stateName = "Unknown";
            break;
        }
        return stateName;
    }
}
