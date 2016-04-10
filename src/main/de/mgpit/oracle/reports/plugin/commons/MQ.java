package de.mgpit.oracle.reports.plugin.commons;


import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;

import de.mgpit.oracle.reports.plugin.commons.U;

public class MQ {

    public static class Configuration {
        private static String SCHEME = "wmq";
        private static String WMQ_DEST = "/dest/queue/";
        private static String NO_USER = null;

        private String hostName, queueManagerName, channelName, queueName;
        private int port;

        public Configuration( String hostName, int port, String queueManagerName, String channelName, String queueName ) {
            this.hostName = hostName;
            this.port = port;
            this.queueManagerName = queueManagerName;
            this.channelName = channelName;
            this.queueName = queueName;
        }

        /*
         * <p>
         * Source of this information is http://www.redbooks.ibm.com/redpapers/pdfs/redp4350.pdf
         * <p>
         * The basic syntax of the wmq IRI scheme is:
         * wmq:/wmq-dest
         * where wmq-dest is one of the following options:
         * msg/queue/queue_name
         * msg/topic/topic_name
         * 
         * <p>
         * wmq-iri = "wmq:" [ "//" connection-name ] "/" wmq-dest ["?" parm *("&" parm)]
         * connection-name = tcp-connection-name / other-connection-name
         * tcp-connection-name = ihost [ ":" port ]
         * other-connection-name = 1*(iunreserved / pct-encoded)
         * wmq-dest = queue-dest / topic-dest
         * queue-dest = "msg/queue/" wmq-queue ["@" wmq-qmgr]
         * wmq-queue = wmq-name
         * wmq-qmgr = wmq-name
         * wmq-name = 1*48( wmq-char )
         * topic-dest = "msg/topic/" wmq-topic
         * wmq-topic = segment *( "/" segment )
         * segment = 1*(iunreserved / pct-encoded)
         * parm = parm-name "=" parm-value
         * parm-name = 1*(iunreserved / pct-encoded)
         * parm-value = *(iunreserved / pct-encoded)
         * 
         * wmq-char = ALPHA / DIGIT / "." / "_" / %x2F / %x25 ; Encode “/” and “%”
         * ihost = ; see [RFC3987]
         * port = ; see [RFC3987]
         * iunreserved = ; see [RFC3987]
         * pct-encoded = ; see [RFC3986]
         * ALPHA = ; see [RFC4234]
         * DIGIT = ; see [RFC4234
         * 
         */
        /**
         * 
         * @param uri
         * @return
         * @throws MalformedURIException
         */
        public static final Configuration fromURI( URI uri ) {
            if ( uri == null ) {
                throw new IllegalArgumentException( "You must provide a non null URI" );
            }
            String scheme = uri.getScheme();
            if ( SCHEME.equalsIgnoreCase( scheme ) ) {

                String host = uri.getHost();
                int port = uri.getPort();

                String path = getRelevantPath( uri.getPath() );
                String[] pathElements = splitPath( path );
                String queueName = pathElements[0];
                String queueManagerName = pathElements[1];

                String query = uri.getQuery();
                Properties queryParameters = queryStringToProperties( query );
                /* This implementation lets query parameter override queue manager specified via path */
                queueManagerName = queryParameters.getProperty( "connectQueueManager", queueManagerName );
                String channelName = queryParameters.getProperty( "channelName" );

                return new MQ.Configuration( host, port, queueManagerName, channelName, queueName );
            } else {
                throw new IllegalArgumentException( uri.toString() + " is NOT a valid MQ URI" );
            }
        }

        private static String getRelevantPath( String path ) {
            String relevantPath = null;
            if ( U.isEmpty( path ) ) {
                throw new IllegalArgumentException( "You must not provide an empty URI path" );
            }
            if ( path.startsWith( Configuration.WMQ_DEST ) ) {
                relevantPath = path.substring( Configuration.WMQ_DEST.length() );
            } else {
                throw new IllegalArgumentException( "URI path must start with " + Configuration.WMQ_DEST );
            }
            return relevantPath;
        }

        private static String[] splitPath( String path ) {
            String[] splitted = path.split( "@" );
            /* Ensure that the caller gets (at least) 2 elements */
            if ( splitted.length == 1 ) {
                splitted[1] = null;
            }
            return splitted;
        }

        private static Properties queryStringToProperties( String query ) {
            Properties queryParameters = new Properties();
            if ( query != null ) {
                String[] parameters = query.split( "&" );
                for ( int index = 0; index < parameters.length; index++ ) {
                    String parameter = parameters[index];
                    String[] keyValuePair = parameter.split( "=" );
                    String key = keyValuePair[0];
                    String value = (keyValuePair.length == 1) ? null : keyValuePair[1];
                    queryParameters.put( key, value );
                }
            }
            return queryParameters;
        }

        public String toString() {
            StringBuffer sb = new StringBuffer( 127 );
            sb.append( "MQ Configuration Host: " ).append( U.w( this.hostName ) ).append( " Port: " ).append( U.w( this.port ) )
                    .append( " Queue Manager: " ).append( U.w( this.queueManagerName ) ).append( " Channel: " )
                    .append( U.w( this.channelName ) ).append( " Queue: " ).append( U.w( this.queueName ) );
            return sb.toString();
        }

        public URI toURI() {
            String path = WMQ_DEST + this.queueName + (U.isEmpty( this.queueManagerName ) ? null : "@" + this.queueManagerName);
            String query = U.isEmpty( this.channelName ) ? null : "channelName=" + this.channelName;
            try {
                URI uri = new URI( SCHEME, NO_USER, this.hostName, this.port, path, query, null );
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

            Configuration another = (Configuration) obj;
            return U.eq( this.hostName, another.hostName ) && this.port == another.port
                    && U.eq( this.queueManagerName, another.queueManagerName ) && U.eq( this.channelName, another.channelName )
                    && U.eq( this.queueName, another.queueName );
        }

        public int hashCode() {
            int hashCode = 11; // initial hash code - prime
            int prime = 29;    // prime, not too big
            hashCode = hashCode * prime + (null == this.hostName ? 0 : this.hostName.hashCode());
            hashCode = hashCode * prime + this.port;
            hashCode = hashCode * prime + (null == this.hostName ? 0 : this.queueManagerName.hashCode());
            hashCode = hashCode * prime + (null == this.hostName ? 0 : this.channelName.hashCode());
            hashCode = hashCode * prime + (null == this.hostName ? 0 : this.queueName.hashCode());
            return hashCode();
        }

    }

}
