package de.mgpit.oracle.reports.plugin.commons.driver;


import java.io.IOException;
import java.io.OutputStream;

import org.apache.log4j.Logger;

import com.ibm.mq.MQC;
import com.ibm.mq.MQException;
import com.ibm.mq.MQMessage;
import com.ibm.mq.MQPutMessageOptions;
import com.ibm.mq.MQQueue;
import com.ibm.mq.MQQueueManager;

public class WebsphereMQ extends MQ {

    public WebsphereMQ( Configuration configuration ) {
        super( configuration );
    }

    private MQQueueManager queueManager = null;
    private MQQueue destinationQueue = null;

    public void connect() throws Exception {
        try {
            int openOptions = MQC.MQOO_FAIL_IF_QUIESCING | MQC.MQOO_OUTPUT;

            String queueManagerName = configuration.getQueueManagerName();
            String queueName = configuration.getQueueName();

            queueManager = new MQQueueManager( queueManagerName );
            destinationQueue = queueManager.accessQueue( queueName, openOptions, null, null, null );

        } catch ( MQException mqex ) {
            Logger.getRootLogger().fatal( "MQException on opening queue " + configuration.toString() + ". Reason code: "
                    + mqex.reasonCode + " Completion code: " + mqex.completionCode, mqex );
            throw mqex;
        }
    }

    public void disconnect() throws Exception {
        try {
            destinationQueue.close();
            queueManager.close();
        } catch ( MQException mqex ) {
            Logger.getRootLogger().error( "MQException on closing queue " + configuration.toString() + ". Reason code: "
                    + mqex.reasonCode + " Completion code: " + mqex.completionCode, mqex );
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
                 * and the put Message options have to be set to String ...
                 */
                mqMessage.write( b );
            }

            public void flush() throws IOException {
                // Don't flush ...
            }
            
            private void finishWrite() throws IOException {
                if ( !finished ) {
                    MQPutMessageOptions putMessageOptions = new MQPutMessageOptions(); // Default: MQC.MQPMO_NO_SYNCPOINT, d.h. kein explizites Commit notwendig
                    try {
                        WebsphereMQ.this.destinationQueue.put( mqMessage, putMessageOptions ); // 3. Durch MQPutMessageOptions zuverlässig Commit vermeiden
                    } catch ( MQException mqex ) {
                        final String message = "MQException on flushing Message" + ". Reason code: " + mqex.reasonCode
                                + " Completion code: " + mqex.completionCode;
                        Logger.getRootLogger().error( message, mqex );
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
