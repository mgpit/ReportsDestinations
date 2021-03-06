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
package de.mgpit.oracle.reports.plugin.destination.mq;


import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.net.URISyntaxException;
import java.util.Properties;

import org.apache.log4j.Logger;

import de.mgpit.oracle.reports.plugin.commons.U;
import de.mgpit.oracle.reports.plugin.commons.driver.MQ;
import de.mgpit.oracle.reports.plugin.commons.io.IOUtility;
import de.mgpit.oracle.reports.plugin.destination.MgpDestination;
import de.mgpit.oracle.reports.plugin.destination.ModifyingDestination;
import de.mgpit.types.Filename;
import oracle.reports.RWException;

/**
 * 
 * Implements an Oracle&reg; Reports {@link oracle.reports.server.Destination} for distributing
 * the files of a distribution process into a IBM<sup>&reg;</sup> Websphere MQ<sup>&reg;</sup> (MQ Series<sup>&reg;</sup>) Queue.
 * The destination is able to modify / transform the data generated report output. See below.
 * 
 * <p>
 * <strong>Registering the destination in your <em>reportserver.conf</em>:</strong>
 * <p>
 * The {@code DESTYPE} for this destination is suggested to be {@code mq}.
 * Basic configuration example:
 * 
 * <pre>
 * {@code
 *  <destination destype="mq" class="de.mgpit.oracle.reports.plugin.destination.zip.MQDestination">
 *     <property name="loglevel" value="DEBUG"/> <!-- log4j message levels. Allow debug messages --> 
 *     <property name="logfile"  value="/tmp/log/mqdestination.log"/> <!-- file to send the messages to -->
 *     <!-- MQ Implementation - the "Driver" -->
 *     <property name="mq.implementation"   value="de.mgpit.oracle.reports.plugin.commons.driver.FileMockedMQ" />;
 *     <!-- (Default) MQ Connection. Can be overridden on distribution -->
 *     <property name="mq"                  value="wmq://localhost:1414/dest/queue/MY.QUEUE@MYQMGR?channelName=CHANNEL_1/"/>
 *  </destination>
 *         }
 * </pre>
 * 
 * The {@code mq.implementation} property is <strong>mandatory</strong> and is either one of
 * <ul>
 * <li>{@code de.mgpit.oracle.reports.plugin.commons.driver.FileMockedMQ}</li>
 * <li>{@code de.mgpit.oracle.reports.plugin.commons.driver.WebsphereMQ}</li>
 * <li>or one of your implementations of {@code de.mgpit.oracle.reports.plugin.commons.driver.MQ}</li>
 * </ul>
 * <p>
 * The default MQ target is configured as property named {@code mq} in the form of an URI denoting
 * Queue Manager, Queue, and Channel name.
 * <ul>
 * <li>{@code wmq://}<em>host:mqport</em>{@code /dest/queue/}<em>queuename@qmgr</em>{@code ?channelName=}<em>channelname</em>
 * </ul>
 * <strong>note: </strong>scheme must be <strong>wmq</strong> (lowercase!)
 * 
 * <p>
 * One can also register modifier plugins.
 * 
 * <pre>
 * {@code
 *  <destination destype="MQ" class="de.mgpit.oracle.reports.plugin.destination.mq.MQDestination">
 *     <property name="loglevel" value="DEBUG"/> <!-- log4j message levels. Allow debug messages --> 
 *     <property name="logfile"  value="/tmp/log/mqdestination.log"/> <!-- file to send the messages to -->
 *     <property name="mq"       value="wmq://localhost:1414/dest/queue/MY.QUEUE@MYQMGR?channelName=CHANNEL_1/"/>
 *     <!-- Modifiers -->
 *     <property name="modifier.BASE64"      value="de.mgpit.oracle.reports.plugin.destination.content.transformers.Base64Transformer"/>
 *     <property name="modifier.Envelope"    value="de.mgpit.oracle.reports.plugin.destination.content.decorators.EnvelopeDecorator"/>
 *     <property name="modifier.Header"      value="de.mgpit.oracle.reports.plugin.destination.content.decorators.HeaderDecorator"/>
 *     <!-- Content Providers -->
 *     <property name="content.Soap_1_1"     value="tld.foo.bar.batz.SoapV0101Envelope"/> <!-- Must implement de.mgpit.oracle.reports.plugin.destination.content.types.Content -->
 *  </destination>
 *         }
 * </pre>
 *
 * <p>
 * <strong>Using the destination:</strong>
 * <p>
 * The default distribution target is configured as property in the <em>reportserver.conf</em> - see above.
 * On distribution one can pass a different target via {@code DESNAME} in the form of the above URI format.
 * <p>
 *
 * When running from <strong>Oracle&reg; Forms</strong>&trade; using a <em>REPORT_OBJECT</em> you can pass the parameters as follows.
 * <ul>
 * <li>{@code SET_REPORT_OBJECT_PROPERTY( }REPORT_OBJECT</em>
 * {@code , REPORT_DESTYPE, CACHE }<sup>1)</sup> {@code );}</li>
 * <li>If wanting to override the MQ set up in the <em>reportserver.conf</em><br/>
 * {@code SET_REPORT_OBJECT_PROPERTY( }<em>REPORT_OBJECT</em>
 * {@code , REPORT_DESNAME, 'wmq://localhost:1414/dest/queue/MY.QUEUE@MYQMGR?channelName=CHANNEL_1' );}
 * </li>
 * <li>code SET_REPORT_OBJECT_PROPERTY( }<em>REPORT_OBJECT</em>
 * {@code , REPORT_OTHER, 'DESTYPE=MQ' );}
 * </li>
 * <li>If wanting to apply modifiers: via Parameter List
 * 
 * <pre>
 * {@code 
 * distribution_parameters := CREATE_PARAMETER_LIST( 'SOME_UNIQUE_NAME' );  
 * ADD_PARAMETER( distribution_parameters, 'APPLY', TEXT_PARAMETER, 'BASE64>>ENVELOPE(SOAP_1_1)' );
 * --
 * -- Pass the Parameter List on RUN_REPORT_OBJECT
 * --
 * RUN_REPORT_OBJECT( REPORT_OBJECT, distribution_parameters ); }
 * </li>
 * </ul>
 * <small>1) Any valid DESTYPE. {@code NULL} or {@code ''} not allowed.</small>
 *
 * @see MgpDestination
 * 
 * @author mgp
 */
public final class MQDestination extends ModifyingDestination {

    /*
     * Me logger ... ;-)
     */
    private static final Logger LOG = Logger.getLogger( MQDestination.class );

    /**
     * Holds the MQ connection used for the current distribution cycle - which is
     * <br/>
     * {@code start} &rarr; {@code sendFile}<sup>{1..n}</sup> &rarr; {@code stop()}
     *
     */
    private MQ mq;

    /**
     * Stops the distribution cycle.
     */
    protected void stop() throws RWException {
        try {
            super.stop();
            mq.disconnect();
        } catch ( Exception any ) {
            getLogger().error( "Error during finishing distribution!", any );
            throw asRWException( any );
        }
        getLogger().info( "Finished distribution to " + U.w( mq ) );
    }

    /**
     * Starts a new distribution cycle for a report to a Websphere MQ&req; queue.
     * 
     * @param allProperties
     *            parameters for this distribution passed as {@code Properties}
     *            <br>
     *            <em>Remark:</em> For Oracle&reg; Forms this will include the parameters set via {@code SET_REPORT_OBJECT_PROPERTY}
     *            plus the parameters passed via a {@code ParamList}
     * @param targetName
     *            target name of the distribution
     * @param totalNumberOfFiles
     *            total number of files being distributed
     * @param totalFileSize
     *            total file size of all files being distributed
     * @param mainFormat
     *            the output format of the main file being distributed
     * 
     * @throws RWException
     *             if there is a failure during distribution setup. The RWException normally will wrap the original Exception.
     */
    protected boolean start( Properties allProperties, String targetName, int totalNumberOfFiles, long totalFileSize,
            short mainFormat ) throws RWException {

        try {
            boolean continueToSend = super.start( allProperties, targetName, totalNumberOfFiles, totalFileSize, mainFormat );

            if ( continueToSend ) {
                getLogger().info( "Starting distribution to MQ" );
                final MQ individualMq = MQRegistrar.getDeclaredMQfrom( allProperties );
                this.mq = (MQ) U.coalesce( individualMq, MQRegistrar.DEFAULT_MQ );
                continueToSend = this.mq != null;
                if ( !continueToSend ) {
                    getLogger().warn( "Cannot continue to send! No MQ destination provided nor default MQ destination speficied!" );
                } else {
                    mq.connect();
                }
            } else {
                getLogger().warn( "Cannot continue to send ..." );
            }
            return continueToSend;
        } catch ( RWException forLogging ) {
            getLogger().error( "Error during preparation of distribution!", forLogging );
            throw forLogging;
        } catch ( Throwable fatalOther ) {
            if ( mq != null ) {
                try {
                    mq.disconnect();
                } catch ( Exception ignoredButLogged ) {
                    getLogger().warn( "Error on emergency close", ignoredButLogged );
                }
            }
            getLogger().fatal( "Fatal error on starting Distribution!", fatalOther );
            throw asRWException( new Exception( "Fatal error on starting Distribution!", fatalOther ) );
        }
    }

    private boolean isMultipart() {
        return false;
    }

    protected void sendMainFile( Filename cacheFileFilename, short fileFormat ) throws RWException {
        getLogger().info( "Sending MAIN file of format " + humanReadable( fileFormat ) + " to " + getClass().getName() );
        InputStream source = getContent( IOUtility.fileFromName( cacheFileFilename ) );
        OutputStream target = null;
        try {
            U.assertNotNull( this.mq, "Cannot continue to send! No MQ destination provided nor default MQ destination speficied!" );
            target = getTarget();
            IOUtility.copyFromTo( source, target );
            source.close();
            if ( !this.isMultipart() ) {
                target.close();
            }
        } catch ( Throwable anyOther ) {
            getLogger().fatal( "Fatal Error during sending main file " + U.w( cacheFileFilename ) + "!", anyOther );
            throw asRWException( new Exception( anyOther ) );
        } finally {
            try {
                if ( source != null ) source.close();
            } catch ( Exception ignoredButLogged ) {
                getLogger().warn( "Error on emergency close of Input Source", ignoredButLogged );
            }
            try {
                if ( target != null ) target.close();
            } catch ( Exception ignoredButLogged ) {
                getLogger().warn( "Error on emergency close of Output Target", ignoredButLogged );
            }
        }
    }

    /**
     * Sends an additional file to the destination.
     * <p>
     * This operation is currently not supported.
     * TODO: No clear idea for design. Should additional files go as separate messages or should
     * additional files be appended to the main file.
     * 
     * @param cacheFileFilename
     * @param fileFormat
     */
    protected void sendAdditionalFile( final Filename cacheFileFilename, short fileFormat ) throws RWException {
        getLogger().info( "Sending Other file to " + getClass().getName() );
        getLogger().warn( U.classname( this ) + " currently does not support multiple files!" );
    }

    /**
     * Gets a Websphere MQ&reg; message for output.
     * 
     * @return {@OutputStream} for writing into an Websphere MQ&reg; message.
     * 
     */
    protected OutputStream getTargetOut() throws Exception {
        return this.mq.newMessage();
    }

    /**
     * Initializes the destination on Report Server startup.
     * <p>
     * Invoked by the Report Server.
     * <ul>
     * <li>initialize log4j</li>
     * <li>register default Websphere MQ settings</li>
     * </ul>
     * 
     * @param destinationsProperties
     *            the properties set in the report server's conf file within this
     *            destination's configuration section ({@code //destination/property})
     * @throws RWException
     */
    public static void init( Properties destinationsProperties ) throws RWException {
        ModifyingDestination.init( destinationsProperties );
        initLogging( destinationsProperties, MQDestination.class );
        if ( LOG.isDebugEnabled() ) {
            dumpProperties( destinationsProperties, LOG );
        }
        try {
            MQRegistrar.registerMqImplementationFrom( destinationsProperties );
            MQRegistrar.registerDefaultMQfrom( destinationsProperties );
        } catch ( Exception ex ) {
            throw asRWException( ex );
        } catch ( Throwable t ) {
            throw asRWException( t );
        }
        LOG.info( "Destination " + U.w( MQDestination.class.getName() ) + " started." );
    }

    private static final class MQRegistrar {
        /**
         * Holds the MQ Implementation.
         */
        private static Class MQ_IMPLEMENTATION;

        private static void registerMqImplementationFrom( Properties destinationsProperties ) throws IllegalArgumentException {
            String className = destinationsProperties.getProperty( "mq.implementation" );
            if ( className == null ) {
                IllegalArgumentException illegal = new IllegalArgumentException(
                        "Missing property \"mq.implementation\" in Report Server configuration!" );
                LOG.fatal( illegal );
                throw illegal;
            }
            try {
                MQ_IMPLEMENTATION = Class.forName( className );
                final Class[] withConfigurationArgument = { MQ.Configuration.class };
                Constructor c = MQ_IMPLEMENTATION.getDeclaredConstructor( withConfigurationArgument );
                LOG.info( "Found " + className + " for MQ driver implementation." );
            } catch ( NoSuchMethodException noSuchMethod ) {
                final String simpleClassName = U.classname( className );
                final String constructorName = simpleClassName + "( MQ.Configuration )";
                IllegalArgumentException illegal = new IllegalArgumentException(
                        className + " does not declare the constructor " + constructorName );
                LOG.fatal( illegal );
                throw illegal;
            } catch ( ClassNotFoundException notFound ) {
                IllegalArgumentException illegal = new IllegalArgumentException( className + " cannot be found!" );
                LOG.fatal( illegal );
                throw illegal;
            }
        }

        /**
         * Holds the default MQ instance as defined in the {@code <reportservername>.conf} file
         */
        private static MQ DEFAULT_MQ;

        private static void registerDefaultMQfrom( Properties destinationsProperties ) {
            DEFAULT_MQ = getDeclaredMQfrom( destinationsProperties );
            if ( DEFAULT_MQ == null ) {
                LOG.info(
                        "No default MQ Connection specified. Destination may not work. Please specify a default Connection as <property name=\"mq\" value=\"wmq://<host>:<port>/dest/queue/<queuename>@<queuemanager>?channelName=<channelname>/\"/>" );
            }
        }

        public static MQ getDeclaredMQfrom( Properties properties ) {
            /*
             * Cascading search for one of the three valid property names to specify a default MQ connection ...
             */
            String uriLiteral = properties.getProperty( "mq",
                    properties.getProperty( "uri", properties.getProperty( "connection", properties.getProperty( "desname" ) ) ) );
            final String prefixWanted = MQ.Configuration.WMQ_SCHEME + ":";
            if ( !U.isEmpty( uriLiteral ) && uriLiteral.startsWith( prefixWanted ) ) {
                return newMQ( uriLiteral );
            }
            return null;
        }
    }

    /**
     * Creates a new MQ instance.
     * <p>
     * 
     * @param uriLiteral
     *            URI literal with the configuration.
     * @return a new MQ instance.
     */
    public static MQ newMQ( String uriLiteral ) {
        try {

            final Class[] withConfigurationArgument = { MQ.Configuration.class };
            Constructor c = MQRegistrar.MQ_IMPLEMENTATION.getDeclaredConstructor( withConfigurationArgument );

            MQ.Configuration configuration = MQ.Configuration.fromURILiteral( uriLiteral );
            final Object[] configurationArgument = { configuration };
            MQ mq = (MQ) c.newInstance( configurationArgument );
            return mq;
        } catch ( URISyntaxException syntax ) {
            LOG.error( "Invalid Connection URI! Cannot create MQ Connection!", syntax );
        } catch ( NoSuchMethodException noSuchMethod ) {
            LOG.error( "MQ Implementation does not declare MQ(Connection) one arg constructor! Cannot create MQ Connection!", noSuchMethod );
        } catch ( Exception other ) {
            LOG.error( "Cannot create MQ Connection!", other );
        }
        return null;
    }

    public static void shutdown() {
        MgpDestination.shutdown();
        LOG.info( "Destination " + U.w( MQDestination.class.getName() ) + " shut down." );
    }

    protected Logger getLogger() {
        return LOG;
    }
}