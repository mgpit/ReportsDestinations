/* Copyright 2016 Marco Pauls www.mgp-it.de
*
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
* use this file except in compliance with the License. You may obtain a copy of
* the License at
*
 * http://www.apache.org/licenses/LICENSE-2.0
*
 * Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
* WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
* License for the specific language governing permissions and limitations under
* the License. */

/**
* @license APACHE-2.0
*/
package de.mgpit.oracle.reports.plugin.commons.driver;


import java.io.IOException;
import java.io.OutputStream;

import org.apache.log4j.Logger;

import com.ibm.mq.MQC;
import com.ibm.mq.MQEnvironment;
import com.ibm.mq.MQException;
import com.ibm.mq.MQMessage;
import com.ibm.mq.MQPutMessageOptions;
import com.ibm.mq.MQQueue;
import com.ibm.mq.MQQueueManager;

import de.mgpit.oracle.reports.plugin.commons.U;

/**
 * Implements a Websphere MQ<sup>&reg;</sup> driver for
 * {@link de.mgpit.oracle.reports.plugin.destination.mq.MQDestination
 * MQDestination}
 *
 * @author mgp
 *
 */
public class WebsphereMQ extends MQ {
    private static final Logger LOG = Logger.getRootLogger();

    public WebsphereMQ( Configuration configuration ) {
        super( configuration );
    }

    private MQQueueManager queueManager = null;
    private MQQueue destinationQueue = null;

    public void connect() throws Exception {
        try {
            LOG.info( "Connecting with " + configuration.toString() );
            
            /*
             * From the {@code MQQueueManager(String) Javadoc:
             * <quote>
             * Creates a connection to the named queue manager.
             * The host name, channel name and port to use during the connection request are specified
             * in the MQEnvironmentclass.
             * This must be done before calling this constructor.
             * </quote>
             */

            // Hence ...
            setMQEnvironment();
            // ... could have used the MQQueueManager(String,Hashtable) constructor alternatively ...

            final String queueManagerName = configuration.getQueueManagerName();
            final String queueName = configuration.getQueueName();

            final int openOptions = MQC.MQOO_FAIL_IF_QUIESCING | MQC.MQOO_OUTPUT;
            queueManager = new MQQueueManager( queueManagerName );
            destinationQueue = queueManager.accessQueue( queueName, openOptions, null, null, null );

        } catch ( MQException mqex ) {
            LOG.fatal( "MQException on opening queue " + configuration.toString() + ". Reason code: " + mqex.reasonCode
                    + " Completion code: " + mqex.completionCode, mqex );
            throw mqex;
        }
    }

    /**
     * Applies the {@link MQ.Configuration Configuration} to the {@link MQEnvironment}.
     */
    protected void setMQEnvironment() {
        final String hostname = configuration.getHostName();
        final int port = configuration.getPort();
        final String channel = configuration.getChannelName();
        final String userId = configuration.getUserId();

        if ( !U.isEmpty( hostname ) ) {
            MQEnvironment.hostname = hostname;
        }
        if ( !U.isEmpty( channel ) ) {
            MQEnvironment.channel = channel;
        }
        if ( port > 0 ) {
            MQEnvironment.port = port;
        }
        if ( !U.isEmpty( userId ) ) {
            MQEnvironment.userID = userId;
        }
    }

    public void disconnect() throws Exception {
        try {
            destinationQueue.close();
            queueManager.close();
        } catch ( MQException mqex ) {
            LOG.error( "MQException on closing queue " + configuration.toString() + ". Reason code: " + mqex.reasonCode
                    + " Completion code: " + mqex.completionCode, mqex );
            throw mqex;
        }
    }

    public OutputStream newMessage() throws Exception {
        return new OutputStream() {
            private final MQMessage mqMessage;
            private boolean finished;

            {
                mqMessage = new MQMessage();
                mqMessage.correlationId = MQC.MQCI_NONE;
                mqMessage.priority = 1;
                finished = false;
            }

            public void write( int b ) throws IOException {
                /*
                 * TODO: Maybe the delegation has to be replaced with writeChar
                 * and the put Message options have to be set to String ... Or
                 * we must build a String and then writeString(String) it to the
                 * MQ Message.
                 */
                mqMessage.write( b );
            }

            /**
             * Flushes this Output Stream.
             * <p>
             * Does nothing. Any data buffered must be retained until this
             * Stream is closed.
             *
             * @see java.io.OutputStream#flush()
             */
            public void flush() throws IOException {
                // Don't flush ...
            }

            /**
             * Flushes the data buffered in this Output Stream.
             * <p>
             * This will put the data buffered to the Websphere
             * MQ<sup>&reg;</sup> queue.
             *
             * @throws IOException
             */
            private void finishWrite() throws IOException {
                if ( !finished ) {
                    MQPutMessageOptions putMessageOptions = new MQPutMessageOptions(); // Default: MQC.MQPMO_NO_SYNCPOINT, d.h. kein explizites Commit notwendig
                    try {
                        WebsphereMQ.this.destinationQueue.put( mqMessage, putMessageOptions ); // 3. Durch MQPutMessageOptions zuverlässig Commit vermeiden
                    } catch ( MQException mqex ) {
                        final String message = "MQException on flushing Message" + ". Reason code: " + mqex.reasonCode
                                + " Completion code: " + mqex.completionCode;
                        LOG.error( message, mqex );
                        throw new IOException( message );
                    }
                    super.flush();
                    finished = true;
                }
            }

            public void close() throws IOException {
                finishWrite();
                super.close();
            }
        };
    }

}