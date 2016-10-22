package de.mgpit.oracle.reports.plugin.commons;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;

public class MQ {

    /**
     * 
     * @author mgp
     * 
     *         Class representing a configuration for a Websphere MQ client connection.
     *         <p>
     *         This is still a very simple implementation as
     *         <ol>
     *         <li>only a few parameters are supported by now</li>
     *         <li>none of the business rules as specified in <a href="ftp://public.dhe.ibm.com/software/integration/support/supportpacs/individual/ma93_wmqsvcdef1.0.pdf">Websphere MQ Service Defintion</a>
     *         are implemented</li>
     *         </ol>
     *
     */
    public static class Configuration {
        /**
         * Scheme for wmq URIs (IRIs) is "wmq"
         */
        public static final String WMQ_SCHEME = "wmq";
        /**
         * path prefix for queue destinations.
         */
        private static final String WMQ_DEST = "/dest/queue/";
        private static final String NO_USER = null;
        public static final int MQ_DEFAULT_PORT = 1414;

        private final String hostName, queueManagerName, channelName, queueName;
        private final int port;

        /**
         * Creates a new Configuration instance
         * 
         * @param hostName
         *            host name where the queue manager is running
         * @param port
         *            TCP port where the queue manager is listening. Any value <= 0 will be replaced by default @code MQ_DEFAULT_PORT}
         * @param queueManagerName
         *            name of the queue manager
         * @param channelName
         *            channel for the connection
         * @param queueName
         *            name of the queue
         */
        public Configuration( final String hostName, final int port, final String queueManagerName, final String channelName,
                final String queueName ) {
            this.hostName = hostName;
            this.port = (port <= 0) ? MQ_DEFAULT_PORT : port;
            this.queueManagerName = queueManagerName;
            this.channelName = channelName;
            this.queueName = queueName;
        }

        /**
         * Returns a Configuration built from a Websphere MQ compliant URI / IRI.
         * <p>
         * Only a few of parameters from the spec will be used, though. <strong>Note</strong>: Ommitting the port will result in using
         * the default port 1414.
         * <ul>
         * <li>Class support client connections, only, so the {@code [connection-name]} MUST be provided and MUST be
         * as {@code [tcp-connection-name]}</li>
         * <li>The queue manager to contact can be specified as {@code [wmq-qmr]} of {@code [queue-dest]}
         * or as {@code [parm]} named {@code connectQueueManager}. Parameter {@code connectQueueManager} takes precedence
         * before {@code [wmq-qmr]}</li>
         * <li>Parameter {@code channelName} will be recognized for declaring the channel name.</li>
         * </ul>
         * <p>
         * <strong>Sample usage</strong>
         * <code><pre>
         * URI uri = new URI( "wmq://localhost:1414/dest/queue/QUEUE.IN@QMGR?channelName=CHANNEL_1" );
         * Configuration c1 = Configuration.fromURI( uri );
         *
         * URI another = new URI( "wmq", null, "localhost", 1414, "/dest/queue/QUEUE.IN@QMGR", "channelName=CHANNEL_1", null );
         * Configuration c2 = Configuration.fromURI( another );
         *
         * URI third = new URI( "wmq", null, "localhost", 1414, "/dest/queue/QUEUE.IN@FOO",
         *        "connectQueueManager=QMGR&channelName=CHANNEL_1", null );
         * Configuration c3 = Configuration.fromURI( third );
         *
         * System.out.println( "C1 and C2 are " + (c1.equals( c2 ) ? "equal" : "different") ); // equal
         * System.out.println( "C2 and C3 are " + (c2.equals( c3 ) ? "equal" : "different") ); // equal
         * System.out.println( "C1 and C3 are " + (c1.equals( c3 ) ? "equal" : "different") ); // equal
         * </pre></code>
         * <p>
         * <strong>Notes on Websphere MQ URI/IRI specification</strong>
         * <p>
         * Source for this information is http://www.redbooks.ibm.com/redpapers/pdfs/redp4350.pdf
         * <p>
         * The basic syntax of the wmq IRI scheme is:
         * <ul>
         * <li>wmq:/wmq-dest</li>
         * </ul>
         * where wmq-dest is one of the following options:
         * <ul>
         * <li>msg/queue/queue_name</li>
         * <li>msg/topic/topic_name</li>
         * </ul>
         * The full syntax specification for a WMQ URI/IRI is as follows:
         * <p>
         * 
         * <pre>
         * wmq-iri = "wmq:" [ "//" connection-name ] "/" wmq-dest ["?" parm *("&" parm)]
         *   connection-name = tcp-connection-name / other-connection-name
         *     tcp-connection-name = ihost [ ":" port ]
         *     other-connection-name = 1*(iunreserved / pct-encoded)
         *   wmq-dest = queue-dest / topic-dest
         *     queue-dest = "msg/queue/" wmq-queue ["@" wmq-qmgr]
         *       wmq-queue = wmq-name
         *       wmq-qmgr = wmq-name
         *         wmq-name = 1*48( wmq-char )
         *     topic-dest = "msg/topic/" wmq-topic
         *       wmq-topic = segment *( "/" segment )
         *       segment = 1*(iunreserved / pct-encoded)
         *   parm = parm-name "=" parm-value
         *     parm-name = 1*(iunreserved / pct-encoded)
         *     parm-value = *(iunreserved / pct-encoded)
         * 
         *   wmq-char = ALPHA / DIGIT / "." / "_" / %x2F / %x25 ; Encode “/” and “%”
         *   ihost = ; see [RFC3987]
         *   port = ; see [RFC3987]
         *   iunreserved = ; see [RFC3987]
         *   pct-encoded = ; see [RFC3986]
         *   ALPHA = ; see [RFC4234]
         *   DIGIT = ; see [RFC4234
         * </pre>
         * 
         * 
         * @param uri
         *            a Websphere MQ compliant URI / IRI. See notes on syntax and elements used above.
         * @return a new Configuration built from the URI
         * @throws AssertionError
         *             if the URI is null or does not have the wmq scheme
         * 
         */
        public static final Configuration fromURI( final URI uri ) {
            U.assertNotNull( uri, "You must provide a non null URI" );
            final String scheme = uri.getScheme();
            U.assertTrue( WMQ_SCHEME.equalsIgnoreCase( scheme ), uri.toString() + " is NOT a valid Websphere MQ URI" );

            {
                final String host = uri.getHost();
                final int port = uri.getPort();

                final String path = getRelevantPath( uri.getPath() );
                final String[] pathElements = URIUtility.splitPathIntoElements( path );
                final String queueName = pathElements[0];
                String queueManagerName = pathElements[1];

                final String query = uri.getQuery();
                final Properties queryParameters = URIUtility.queryStringAsProperties( query );
                /* This implementation lets the query parameters override queue manager specified via path */
                queueManagerName = queryParameters.getProperty( "connectQueueManager", queueManagerName );
                final String channelName = queryParameters.getProperty( "channelName" );

                return new MQ.Configuration( host, port, queueManagerName, channelName, queueName );
            }
        }

        public static final Configuration fromURILiteral( final String uriLiteral ) throws URISyntaxException {
            return fromURI( new URI( uriLiteral ) );
        }

        /**
         * Returns the relevant path for determining the connection parameters.
         * This is the <code>wmq-queue ["@" wmq-qmgr}</code> part of <code>"msg/queue/" wmq-queue ["@" wmq-qmgr]</code>
         * 
         * @param path
         *            path given in the URI
         * @return <code>wmq-queue ["@" wmq-qmgr}</code> part of <code>"msg/queue/" wmq-queue ["@" wmq-qmgr]</code>
         */
        private static String getRelevantPath( final String path ) {
            U.assertNotEmpty( path, "You must not provide an empty URI path" );
            U.assertTrue( path.startsWith( Configuration.WMQ_DEST ), "URI path must start with " + Configuration.WMQ_DEST );
            final String relevantPath = path.substring( WMQ_DEST.length() );
            return relevantPath;
        }


        public String toString() {
            final StringBuffer sb = new StringBuffer( 127 );
            sb.append( "MQ Configuration Host: " ).append( U.w( this.hostName ) ).append( " Port: " ).append( U.w( this.port ) )
                    .append( " Queue Manager: " ).append( U.w( this.queueManagerName ) ).append( " Channel: " )
                    .append( U.w( this.channelName ) ).append( " Queue: " ).append( U.w( this.queueName ) );
            return sb.toString();
        }

        /**
         * 
         * @return the {@link URI} representation of the Configuration
         */
        public URI toURI() {
            final String path = WMQ_DEST + this.queueName
                    + (U.isEmpty( this.queueManagerName ) ? null : "@" + this.queueManagerName);
            final String query = U.isEmpty( this.channelName ) ? null : "channelName=" + this.channelName;
            try {
                URI uri = new URI( WMQ_SCHEME, NO_USER, this.hostName, this.port, path, query, null );
                return uri;
            } catch ( URISyntaxException syntax ) {
                return null;
            }
        }

        public boolean equals( Object obj ) {
            if ( this == obj ) {
                return true;
            }
            if ( obj == null ) {
                return false;
            }
            if ( !(obj instanceof MQ.Configuration) ) {
                return false;
            }

            final Configuration another = (Configuration) obj;
            return U.eq( this.hostName, another.hostName ) && this.port == another.port
                    && U.eq( this.queueManagerName, another.queueManagerName ) && U.eq( this.channelName, another.channelName )
                    && U.eq( this.queueName, another.queueName );
        }

        public int hashCode() {
            int hashCode = 11; // initial hash code - prime
            final int prime = 29;    // prime, not too big
            hashCode = hashCode * prime + (null == this.hostName ? 0 : this.hostName.hashCode());
            hashCode = hashCode * prime + this.port;
            hashCode = hashCode * prime + (null == this.queueManagerName ? 0 : this.queueManagerName.hashCode());
            hashCode = hashCode * prime + (null == this.channelName ? 0 : this.channelName.hashCode());
            hashCode = hashCode * prime + (null == this.queueName ? 0 : this.queueName.hashCode());
            return hashCode();
        }

    }

    private final Configuration configuration;

    public MQ( Configuration configuration ) {
        this.configuration = configuration;
    }

    public void connect() {

    }

    public void disconnect() {

    }

    public OutputStream newMessage() {
        return new OutputStream() {
            private FileOutputStream file;

            public void write( int b ) throws IOException {
                if ( file != null ) {
                    file.write( b );
                }
            }
            
            public void flush() throws IOException {
                file.flush();
            }
            public void close() throws IOException {
                file.close();
            }

            {
                try {
                    file = new FileOutputStream( File.createTempFile( "QMSALSAXP.IQ.DEVELOPMENT__", ".mq", new File( "O:\\tmp\\reports\\output" ) ) );
                } catch ( IOException ignored ) {
                    file = null;
                }
            }

        };
    }

}