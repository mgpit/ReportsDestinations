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


import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;

import de.mgpit.oracle.reports.plugin.commons.MQ;
import de.mgpit.oracle.reports.plugin.commons.U;
import de.mgpit.oracle.reports.plugin.commons.io.IOUtility;
import de.mgpit.oracle.reports.plugin.destination.MgpDestination;
import de.mgpit.oracle.reports.plugin.destination.TransformationChainDeclaration;
import de.mgpit.oracle.reports.plugin.destination.content.types.Content;
import de.mgpit.oracle.reports.plugin.destination.content.types.InputTransformation;
import de.mgpit.oracle.reports.plugin.destination.content.types.OutputTransformation;
import de.mgpit.oracle.reports.plugin.destination.content.types.Transformation;
import de.mgpit.types.Filename;
import de.mgpit.types.TransformerName;
import oracle.reports.RWException;
import oracle.reports.utility.Utility;

/**
 * 
 * Implements an Oracle Reports&trade; {@link oracle.reports.server.Destination} for distributing
 * the files of a distribution process into a Websphere MQ&trade; (MQ Series) Queue.
 * The destination is able to modify / transform the data genearated report output. See below.
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
 *     <property name="mq"       value="wmq://localhost:1414/dest/queue/MY.QUEUE@MYQMGR?channelName=CHANNEL_1/"/>
 *  </destination>
 *         }
 * </pre>
 * 
 * The default MQ target is configured as property named {@code mq} in the form of an URI denoting
 * Queue Manager, Queue, and Channel name.
 * <ul>
 * <li>{@code wmq://}<em>host:mqport</em>{@code /dest/queue/}<em>queuename@qmgr</em>{@code ?channelName=}<em>channelname</em>
 * </ul>
 * <strong>note: </strong>scheme must be <strong>wmq</strong> (lowercase!)
 * <p>
 * One can also register transformation plugins.
 * 
 * <pre>
 * {@code
 *  <destination destype="MQ" class="de.mgpit.oracle.reports.plugin.destination.zip.MQDestination">
 *     <property name="loglevel" value="DEBUG"/> <!-- log4j message levels. Allow debug messages --> 
 *     <property name="logfile"  value="/tmp/log/mqdestination.log"/> <!-- file to send the messages to -->
 *     <property name="mq"       value="wmq://localhost:1414/dest/queue/MY.QUEUE@MYQMGR?channelName=CHANNEL_1/"/>
 *     <!-- Transformers -->
 *     <property name="transformer.BASE64"      value="de.mgpit.oracle.reports.plugin.destination.content.transformers.Base64Transformer"/>
 *     <property name="transformer.Envelope"    value="de.mgpit.oracle.reports.plugin.destination.content.decorators.EnvelopeDecorator"/>
 *     <property name="transformer.Header"      value="de.mgpit.oracle.reports.plugin.destination.content.decorators.HeaderDecorator"/>
 *     <!-- Content Providers -->
 *     <property name="content.Soap"            value="tld.foo.bar.batz.SoapEnvelope"/> <!-- Must implement de.mgpit.oracle.reports.plugin.destination.content.types.Content -->
 *  </destination>
 *         }
 * </pre>
 *
 *
 * <p>
 * <strong>Using the destination:</strong>
 * <p>
 * The default distribution target is configured as property in the <em>reportserver.conf</em> - see above.
 * On distribution one can pass a different target via {@code DESNAME} in the form of the above URI format.
 * <p>
 *
 * When running from <strong>Oracle Forms</strong>&trade; using a <em>REPORT_OBJECT</em> you can pass the parameters as follows.
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
 * <li>If wanting to apply transformations: via Parameter List
 * 
 * <pre>
 * {@code 
 * distribution_parameters := CREATE_PARAMETER_LIST( 'SOME_UNIQUE_NAME' );  
 * ADD_PARAMETER( distribution_parameters, 'TRANSFORM', TEXT_PARAMETER, 'BASE64>>ENVELOPE(SOAP)' );
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
public final class MQDestination extends MgpDestination {

    /*
     * Me logger ... ;-)
     */
    private static final Logger LOG = Logger.getLogger( MQDestination.class );

    /**
     * Holds the default MQ instance as defined in the {@code <reportservername>.conf} file
     */
    private static MQ DEFAULT_MQ;

    private static final int SOME_PRIME = 23;

    /**
     * Holds the transformer plugins as defined in the {@code <reportservername>.conf} file
     */
    private static Map CONTENTMODIFIERS = Collections.synchronizedMap( new HashMap( SOME_PRIME ) );

    /**
     * Holds the content providers as defined in the {@code <reportservername>.conf} file
     */
    private static Map CONTENTPROVIDERS = Collections.synchronizedMap( new HashMap( SOME_PRIME ) );

    /**
     * Holds the transformation chain which will be applied to the output
     * on the current distribution cycle - which is
     * <br/>
     * {@code start} &rarr; {@code sendFile}<sup>{1..n}</sup> &rarr; {@code stop()}
     * 
     */
    private OutputTransformation[] outputTransformationChain = {};
    /**
     * Holds the transformation chain which will be applied to the input
     * on the current distribution cycle - which is
     * <br/>
     * {@code start} &rarr; {@code sendFile}<sup>{1..n}</sup> &rarr; {@code stop()}
     */
    private InputTransformation[] inputTransformationChain = {};

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
        } catch ( Exception any ) {
            getLogger().error( "Error during finishing distribution!", any );
            throw Utility.newRWException( any );
        }
        getLogger().info( "Finished distribution to " + U.w( mq ) );
    }

    /**
     * Starts a new distribution cycle for a report to this destination. Will
     * distribute one or many files depending on the format.
     * 
     * @param allProperties
     *            all properties (parameters) passed to the report.
     *            For Oracle Forms&trade; this will include the parameters set via <code>SET_REPORT_OBJECT_PROPERTY</code>
     *            plus the parameters passed via a <code>ParamList</code>
     * @param targetName
     *            target name of the distribution.
     * @param totalNumberOfFiles
     *            total number of files to be distributed.
     * @param totalFileSize
     *            total file size of all files distributed.
     * @param mainFormat
     *            the output format of the main file.
     */
    protected boolean start( Properties allProperties, String targetName, int totalNumberOfFiles, long totalFileSize,
            short mainFormat ) throws RWException {

        try {
            boolean continueToSend = super.start( allProperties, targetName, totalNumberOfFiles, totalFileSize, mainFormat );

            if ( continueToSend ) {
                getLogger().info( "Starting distribution to MQ" );
                extractDeclaredTransformationChain( allProperties );
                this.mq = (MQ) U.coalesce( getDeclaredMQ( allProperties ), DEFAULT_MQ );

                continueToSend = this.mq != null;
                if ( !continueToSend ) {
                    getLogger().warn( "Cannot continue to send! No MQ destination provided nor default MQ destination speficied!" );
                }
            } else {
                getLogger().warn( "Cannot continue to send ..." );
            }
            return continueToSend;
        } catch ( RWException forLogging ) {
            getLogger().error( "Error during preparation of distribution!", forLogging );
            throw forLogging;
        }
    }

    protected void extractDeclaredTransformationChain( final Properties allProperties ) throws RWException {
        String transformationDeclaration = allProperties.getProperty( "transform" );

        if ( transformationDeclaration != null ) {
            getLogger().info( "Extracting declared Transformation Chain" );
            TransformerName[] declaredTransformations = TransformationChainDeclaration.extractNames( transformationDeclaration );
            getLogger().info( "Declaration contains " + U.w( declaredTransformations.length ) + " items." );

            ArrayList declaredOutputTransformations = new ArrayList();
            ArrayList declaredInputTransformations = new ArrayList();
            for ( int runIndex = 0; runIndex < declaredTransformations.length; runIndex++ ) {
                TransformerName givenName = declaredTransformations[runIndex];
                getLogger().debug( U.w( U.lpad( runIndex, 2 ) ) + ": Extracting " + U.w( givenName.toString() ) );
                if ( Transformer.transformsOnOutput( givenName ) ) {
                    getLogger().info( U.w( givenName ) + " identified as Output transformation." );
                    declaredOutputTransformations.add( Transformer.Out.newInstance( givenName ) );
                } else if ( Transformer.transformsOnInput( givenName ) ) {
                    getLogger().info( U.w( givenName ) + " identified as Input transformation." );
                    declaredInputTransformations.add( Transformer.In.newInstance( givenName ) );
                } else {
                    getLogger().warn( U.w( givenName ) + " cannot be identified as Output nor Input transformation." );
                }
            }

            try {
                assignOutputTransformationChain( declaredOutputTransformations );
                assignInputTransformationChain( declaredInputTransformations );
            } catch ( Exception any ) {
                LOG.fatal( "Fatal error on extracting transformations!", any );
                throw Utility.newRWException( any );
            }

        } else {
            getLogger().info( "No transformations declared." );
        }
    }

    private void assignOutputTransformationChain( List extracted ) {
        if ( extracted != null && extracted.size() > 0 ) {
            this.outputTransformationChain = new OutputTransformation[extracted.size()];
            int targetIndex = 0;
            Iterator elements = extracted.iterator();
            while ( elements.hasNext() ) {
                OutputTransformation element = (OutputTransformation) elements.next();
                this.outputTransformationChain[targetIndex++] = element;
            }
        }
    }

    private void assignInputTransformationChain( List extracted ) {
        if ( extracted != null && extracted.size() > 0 ) {
            this.outputTransformationChain = new OutputTransformation[extracted.size()];
            int targetIndex = 0;
            Iterator elements = extracted.iterator();
            while ( elements.hasNext() ) {
                InputTransformation element = (InputTransformation) elements.next();
                this.inputTransformationChain[targetIndex++] = element;
            }
        }
    }

    protected void sendMainFile( Filename cacheFileFilename, short fileFormat ) throws RWException {
        getLogger().info( "Sending MAIN file to " + getClass().getName() );
        InputStream source = getContent( IOUtility.asFile( cacheFileFilename ) );
        try {
            U.assertNotNull( this.mq, "Cannot continue to send! No MQ destination provided nor default MQ destination speficied!" );
            OutputStream mqOut = this.mq.newMessage();
            OutputStream target = wrapWithOutputTransformers( mqOut );
            IOUtility.copyFromToAndThenClose( source, target );
        } catch ( FileNotFoundException fileNotFound ) {
            getLogger().error( "Error during distribution! Could not find file to add!" );
            throw Utility.newRWException( fileNotFound );
        } catch ( IOException ioex ) {
            getLogger().error( "Error during distribution! Could not copy file content!", ioex );
            throw Utility.newRWException( ioex );
        } catch ( Throwable anyOther ) {
            getLogger().fatal( "Fatal Error during sending main file " + U.w( cacheFileFilename ) + "!", anyOther );
            throw Utility.newRWException( new Exception( anyOther ) );
        }

    }

    protected void sendAdditionalFile( final Filename cacheFileFilename, short fileFormat ) throws RWException {
        getLogger().info( "Sending Other file to " + getClass().getName() );
    }

    protected InputStream getContent( File file ) throws RWException {
        InputStream sourceInput = super.getContent( file );
        return wrapWithInputTransformers( sourceInput );
    }

    /**
     * Wraps the output wiht {@link OutputTransformation}s.
     * 
     * @param destinationStream
     * @return
     * @throws RWException
     */
    private OutputStream wrapWithOutputTransformers( OutputStream destinationStream ) throws RWException {
        OutputStream wrapped = destinationStream;
        if ( this.outputTransformationChain != null ) {
            final int startIndex = this.outputTransformationChain.length - 1;
            final Properties allProperties = getProperties();
            for ( int runIndex = startIndex; runIndex >= 0; --runIndex ) {
                OutputTransformation transformation = this.outputTransformationChain[runIndex];
                wrapped = transformation.forOutput( wrapped, allProperties );
                getLogger().info( "Transformer for " + U.w( transformation.toString() ) + " has been applied successfully." );
            }
        }
        return wrapped;
    }

    /**
     * Wraps the input with {@link InputTransformation}s.
     * 
     * @param initialStream
     * @return
     * @throws RWException
     */
    private InputStream wrapWithInputTransformers( InputStream initialStream ) throws RWException {
        InputStream wrapped = initialStream;
        if ( this.inputTransformationChain != null ) {
            final int endIndex = this.inputTransformationChain.length;
            final Properties allProperties = getProperties();
            for ( int runIndex = 0; runIndex < endIndex; runIndex++ ) {
                InputTransformation transformation = this.inputTransformationChain[runIndex];
                wrapped = transformation.forInput( wrapped, allProperties );
                getLogger().info( "Transformer " + U.w( transformation.toString() ) + " has been applied successfully." );
            }
        }
        return wrapped;
    }

    /**
     * Initializes the destination on Report Server startup.
     * <ul>
     * <li>initialize log4j</li>
     * <li>register declared content modifiers</li>
     * <li>register declared aliases</li>
     * </ul>
     * 
     * @param destinationsProperties
     * @throws RWException
     */
    public static void init( Properties destinationsProperties ) throws RWException {
        initLogging( destinationsProperties, MQDestination.class );
        if ( LOG.isDebugEnabled() ) {
            dumpProperties( destinationsProperties, LOG );
        }
        registerConfiguredContentModifiers( destinationsProperties );
        registerConfiguredContentProvideres( destinationsProperties );
        registerDefaultMQ( destinationsProperties );
        LOG.info( "Destination " + U.w( MQDestination.class.getName() ) + " started." );
    }

    private static void registerConfiguredContentModifiers( Properties destinationsProperties ) throws RWException {
        Enumeration keys = destinationsProperties.keys();
        LOG.info( "About to search for and register virtual destinations ..." );
        boolean registrationErrorOccured = false;

        while ( keys.hasMoreElements() ) {
            String key = (String) keys.nextElement();
            if ( isContentModificationPluginDefinition( key ) ) {
                TransformerName name = extractContentModificationPluginName( key );
                String virtualDestinationClassName = destinationsProperties.getProperty( key );
                boolean success = registerContentModifier( name, virtualDestinationClassName );
                if ( !success ) {
                    registrationErrorOccured = true;
                }
            }
        }
        if ( registrationErrorOccured ) {
            LOG.warn( "Could not register all Content Plugins!" );
        }
    }

    private static boolean registerContentModifier( TransformerName name, String implementingClassName ) {
        LOG.info( " >>> About to register Content Transformer named " + U.w( name ) );
        Class clazz = null;
        try {
            clazz = Class.forName( implementingClassName );
            LOG.info( U.w( implementingClassName ) + " registered successfully for " + U.w( name ) );
        } catch ( ClassNotFoundException cnf ) {
            LOG.warn( cnf );
            return false;
        }
        CONTENTMODIFIERS.put( name, clazz );
        return true;
    }

    private static boolean isContentModificationPluginDefinition( String key ) {
        return key.startsWith( Transformation.PROPERTY_NAME_PREFIX );
    }

    private static TransformerName extractContentModificationPluginName( String key ) {
        String[] pathElements = key.split( "\\." );
        int numberOfElements = pathElements.length;
        if ( numberOfElements < 2 ) {
            LOG.warn( "Got invalid Content Transformername: " + U.w( key ) );
            return null;
        }
        int indexOfContentModificationPluginName = --numberOfElements;
        return TransformerName.of( pathElements[indexOfContentModificationPluginName] );
    }

    private static void registerConfiguredContentProvideres( Properties destinationsProperties ) throws RWException {
        Enumeration keys = destinationsProperties.keys();
        while ( keys.hasMoreElements() ) {
            String key = (String) keys.nextElement();
            if ( isContentProviderDefintion( key ) ) {
                String value = destinationsProperties.getProperty( key );
                CONTENTPROVIDERS.put( key, value );
                LOG.info( "Registered alias " + U.w( key ) + " for " + U.w( value ) );
            }
        }
    }

    private static boolean isContentProviderDefintion( String key ) {
        return key.startsWith( Content.PROPERTY_NAME_PREFIX );
    }

    private static void registerDefaultMQ( Properties destinationsProperties ) {
        DEFAULT_MQ = getDeclaredMQ( destinationsProperties );
        if ( DEFAULT_MQ == null ) {
            LOG.info(
                    "No default MQ Connection specified. Destination may not work. Please specify a default Connection as <property name=\"mq\" value=\"wmq://<host>:<port>/dest/queue/<queuename>@<queuemanager>?channelName=<channelname>/\"/>" );
        }
    }

    public static MQ getDeclaredMQ( Properties properties ) {
        /*
         * Cascading search for one of the three valid property names to specify a default MQ connection ...
         */
        String uriLiteral = properties.getProperty( "mq",
                properties.getProperty( "uri", properties.getProperty( "connection", properties.getProperty( "desname" ) ) ) );
        final String prefixWanted = MQ.Configuration.WMQ_SCHEME + ":";
        if ( !U.isEmpty( uriLiteral ) && uriLiteral.startsWith( prefixWanted ) ) {

            try {
                return new MQ( MQ.Configuration.fromURILiteral( uriLiteral ) );
            } catch ( URISyntaxException syntax ) {
                LOG.error( "Invalid Connection URI! Cannot create MQ Connection!", syntax );
            } catch ( Exception other ) {
                LOG.error( "Cannot create MQ Connection!", other );
            }
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

    private static class Transformer {

        private static final class Out extends Transformer {
            protected static final OutputTransformation newInstance( TransformerName name ) throws RWException {
                try {
                    return (OutputTransformation) Transformer._newInstance( name );
                } catch ( Throwable any ) {
                    LOG.fatal( "Cannot instantiate " + U.w( name ) + " (as OutputTransformation)", any );
                    throw Utility.newRWException( new Exception( any ) );
                }
            }
        }

        private static final class In extends Transformer {
            protected static final InputTransformation newInstance( TransformerName name ) throws RWException {
                try {
                    return (InputTransformation) Transformer._newInstance( name );
                } catch ( Throwable any ) {
                    LOG.fatal( "Cannot instantiate " + U.w( name ) + " (as InputTransformation)", any );
                    throw Utility.newRWException( new Exception( any ) );
                }
            }
        }

        protected static Transformation _newInstance( TransformerName name ) throws RWException {
            Class clazz = (Class) CONTENTMODIFIERS.get( name );
            if ( clazz != null ) {
                try {
                    Object newInstance = clazz.newInstance();
                    Transformation transformer = (Transformation) newInstance;
                    return transformer;
                } catch ( InstantiationException cannotInstantiate ) {
                    LOG.error( "Cannot instantiate " + U.w( clazz.getName() ), cannotInstantiate );
                    throw Utility.newRWException( cannotInstantiate );
                } catch ( Exception anyOther ) {
                    LOG.error( "Error during instantiation of ContentModificationPlugin!", anyOther );
                    throw Utility.newRWException( anyOther );
                }
            } else {
                IllegalArgumentException illegalArgument = new IllegalArgumentException(
                        "No transformer named " + U.w( name ) + " has been registered!" );
                LOG.fatal( illegalArgument );
                throw Utility.newRWException( illegalArgument );
            }
        }

        protected static boolean transformsOnInput( TransformerName givenName ) {
            Class clazz = (Class) CONTENTMODIFIERS.get( givenName );
            if ( clazz == null ) {
                LOG.error( "No transformer named " + U.w( givenName ) + "has been registered!" );
                return false;
            }
            return InputTransformation.class.isAssignableFrom( clazz );
        }

        protected static boolean transformsOnOutput( TransformerName givenName ) {
            Class clazz = (Class) CONTENTMODIFIERS.get( givenName );
            if ( clazz == null ) {
                LOG.error( "No transformer named " + U.w( givenName ) + "has been registered!" );
                return false;
            }
            return OutputTransformation.class.isAssignableFrom( clazz );
        }
    }

}