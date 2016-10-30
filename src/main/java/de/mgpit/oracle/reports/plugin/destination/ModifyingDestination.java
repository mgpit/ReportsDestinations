package de.mgpit.oracle.reports.plugin.destination;


import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import de.mgpit.oracle.reports.plugin.commons.U;
import de.mgpit.oracle.reports.plugin.destination.content.types.Content;
import de.mgpit.oracle.reports.plugin.destination.content.types.InputTransformation;
import de.mgpit.oracle.reports.plugin.destination.content.types.OutputTransformation;
import de.mgpit.oracle.reports.plugin.destination.content.types.Transformation;
import de.mgpit.types.ContentName;
import de.mgpit.types.ModifyerName;
import de.mgpit.types.ModifyerUnparsedName;
import oracle.reports.RWException;
import oracle.reports.server.Destination;
import oracle.reports.utility.Utility;

/**
 * A Destination which is able to transform the report's content on distribution.
 * <p>
 * This <em>abstract</em> class serves as super class for {@code oracle.reports.server.Destination}s which want to
 * modify the data of the report being distributed by the Reports Server.
 * Possible applications are 
 *  <ul>
 *      <li>encoding the data of a report with outout format {@code PDF} as {@code BASE64}</li>
 *      <li>putting the data into an <em>Envelope</em></li>
 *      <li>prepending the data with a <em>Header</em></li>
 *      <li>or combinations of such transformations</li>
 *  </ul>
 * The modifiers to apply are specified on report execution, see below. For referencing a modifier on 
 * report execution they have to be given an <em>alias</em>, which is done in the {@code <reportservername>.conf} file
 * by specifying properties with <em>name</em> {@code transformer.<alias>}.
 * <p>
 * Modifiers can be divided into <em>Decorators</em> and <em>Transformers</em>. A <em>Decorator</em> applies additional
 * content (like envelopes or headers), a <em>Transformer</em> changes the report's bytes. 
 * <p>
 * For <em>Decorartor</em>s one can also specify <em>content provider</em>s which then can be used by the decorator for
 * generating the additional content. So one can choose between implementing a class for each kind of decoration or
 * implementing a generic decoration (a generic envelope, for example) and then provide the content separately.
 * <p>
 * Here is an example configuration excerpt for a {@code <reportservername>.conf} file on how to specify transformers and
 * content providers. 
 * <pre>
 * {@code
 *  <destination destype="..." class="...">
 *     ...
 *     <!-- Transformers -->
 *     <property name="transformer.BASE64"      value="de.mgpit.oracle.reports.plugin.destination.content.transformers.Base64Transformer"/>
 *     <property name="transformer.Envelope"    value="de.mgpit.oracle.reports.plugin.destination.content.decorators.EnvelopeDecorator"/>
 *     <property name="transformer.Header"      value="de.mgpit.oracle.reports.plugin.destination.content.decorators.HeaderDecorator"/>
 *     <!-- Content Providers -->
 *     <property name="content.Soap_1_1"        value="tld.foo.bar.batz.SoapV0101Envelope"/> <!-- Must implement de.mgpit.oracle.reports.plugin.destination.content.types.Content -->
 *     <property name="content.Soap_1_2"        value="tld.foo.bar.batz.SoapV0102Envelope"/> <!-- Must implement de.mgpit.oracle.reports.plugin.destination.content.types.Content -->
 *  </destination>
 *         }
 * </pre>
 * @author mgp
 *
 */
public abstract class ModifyingDestination extends MgpDestination {

    private static final Logger LOG = Logger.getLogger( MgpDestination.class );

    /**
     * Holds the transformer plugins as defined in the {@code <reportservername>.conf} file
     */
    private static Map CONTENTMODIFIERS = Collections.synchronizedMap( new HashMap( SOME_PRIME ) );

    /**
     * Holds the content providers as defined in the {@code <reportservername>.conf} file
     */
    private static Map CONTENTPROVIDERS = Collections.synchronizedMap( new HashMap( SOME_PRIME ) );

    protected abstract Logger getLogger();

    /**
     * Initializes the destination on Report Server startup.
     * <p>
     * Invoked by the Report Server.
     * <ul>
     * <li>initialize log4j</li>
     * <li>register declared content modifiers</li>
     * <li>register declared aliases</li>
     * </ul>
     * 
     * @param destinationsProperties
     *            the properties set in the report server's conf file within this
     *            destination's configuration section ({@code //destination/property})
     * @throws RWException
     */
    public static void init( Properties destinationsProperties ) throws RWException {
        MgpDestination.init( destinationsProperties );
        registerConfiguredContentModifiers( destinationsProperties );
        registerConfiguredContentProvideres( destinationsProperties );
    }

    private static void registerConfiguredContentModifiers( Properties destinationsProperties ) throws RWException {
        Enumeration keys = destinationsProperties.keys();
        LOG.info( "About to search for and register virtual destinations ..." );
        boolean registrationErrorOccured = false;

        while ( keys.hasMoreElements() ) {
            String key = (String) keys.nextElement();
            if ( isContentModificationPluginDefinition( key ) ) {
                ModifyerUnparsedName name = extractContentModificationPluginName( key );
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

    private static boolean registerContentModifier( ModifyerUnparsedName name, String implementingClassName ) {
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

    private static ModifyerUnparsedName extractContentModificationPluginName( String key ) {
        String[] pathElements = key.split( "\\." );
        int numberOfElements = pathElements.length;
        if ( numberOfElements < 2 ) {
            LOG.warn( "Got invalid Content Transformername: " + U.w( key ) );
            return null;
        }
        int indexOfContentModificationPluginName = --numberOfElements;
        return ModifyerUnparsedName.of( pathElements[indexOfContentModificationPluginName] );
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

    protected void extractDeclaredTransformationChain( final Properties allProperties ) throws RWException {
        String transformationDeclaration = allProperties.getProperty( "transform" );

        if ( transformationDeclaration != null ) {
            getLogger().info( "Extracting declared Transformation Chain" );
            ModifyerUnparsedName[] declaredTransformations = ModifierChainDeclaration
                    .extractNames( transformationDeclaration );
            getLogger().info( "Declaration contains " + U.w( declaredTransformations.length ) + " items." );

            ArrayList declaredOutputTransformations = new ArrayList();
            ArrayList declaredInputTransformations = new ArrayList();
            for ( int runIndex = 0; runIndex < declaredTransformations.length; runIndex++ ) {
                ModifyerUnparsedName givenName = declaredTransformations[runIndex];
                getLogger().debug( U.w( U.lpad( runIndex, 2 ) ) + ": Extracting " + U.w( givenName.toString() ) );
                Transformer transformer = new Transformer();
                if ( transformer.transformsOnOutput( givenName ) ) {
                    getLogger().info( U.w( givenName ) + " identified as Output transformation." );
                    declaredOutputTransformations.add( transformer.new Out().createTransformation( givenName, this ) );
                } else if ( transformer.transformsOnInput( givenName ) ) {
                    getLogger().info( U.w( givenName ) + " identified as Input transformation." );
                    declaredInputTransformations.add( transformer.new In().createTransformation( givenName, this ) );
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
    protected OutputStream wrapWithOutputTransformers( OutputStream destinationStream ) throws RWException {
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
    protected InputStream wrapWithInputTransformers( InputStream initialStream ) throws RWException {
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

    protected boolean start( Properties allProperties, String targetName, int totalNumberOfFiles, long totalFileSize,
            short mainFormat ) throws RWException {

        try {
            boolean continueToSend = super.start( allProperties, targetName, totalNumberOfFiles, totalFileSize, mainFormat );

            if ( continueToSend ) {
                getLogger().info( "Looking for Transformation Chain for extraction ... " );
                extractDeclaredTransformationChain( allProperties );
            } else {
                getLogger().warn( "Cannot continue to send ..." );
            }
            return continueToSend;
        } catch ( RWException forLogging ) {
            getLogger().error( "Error during preparation of distribution!", forLogging );
            throw forLogging;
        }
    }

    /**
     * 
     * @author mgp
     *
     */
    protected class Transformer {
        private final Logger outerLog = getLogger();

        protected TransformerDeclaration getContentDeclaration( ModifyerUnparsedName name ) {
            return new TransformerDeclaration( name );
        }

        protected Transformation createTransformationInstance( ModifyerUnparsedName name ) throws RWException {
            Class clazz = (Class) CONTENTMODIFIERS.get( name );
            if ( clazz != null ) {
                try {
                    Object newInstance = clazz.newInstance();
                    Transformation transformer = (Transformation) newInstance;
                    return transformer;
                } catch ( InstantiationException cannotInstantiate ) {
                    outerLog.error( "Cannot instantiate " + U.w( clazz.getName() ), cannotInstantiate );
                    throw Utility.newRWException( cannotInstantiate );
                } catch ( Exception anyOther ) {
                    outerLog.error( "Error during instantiation of ContentModificationPlugin!", anyOther );
                    throw Utility.newRWException( anyOther );
                }
            } else {
                IllegalArgumentException illegalArgument = new IllegalArgumentException(
                        "No transformer named " + U.w( name ) + " has been registered!" );
                getLogger().fatal( illegalArgument );
                throw Utility.newRWException( illegalArgument );
            }
        }

        protected Transformation buildTransformation( ModifyerUnparsedName name, Destination requesting ) throws RWException {
            try {
                TransformerDeclaration declaredContent = getContentDeclaration( name );
                return this.createTransformationInstance( name );
            } catch ( Throwable any ) {
                outerLog.fatal( "Cannot instantiate " + U.w( name ) + " (as OutputTransformation)", any );
                throw Utility.newRWException( new Exception( any ) );
            }

        }

        /**
         * A Factory for building OutputTransformations.
         * 
         * @author mgp
         *
         */
        private final class Out extends Transformer {
            protected final OutputTransformation createTransformation( ModifyerUnparsedName name, Destination requesting )
                    throws RWException {
                return (OutputTransformation) new Out().createTransformation( name, requesting );
            }
        }

        /**
         * A Factory for building InputTransformations.
         * 
         * @author mgp
         *
         */
        private final class In extends Transformer {
            protected final InputTransformation createTransformation( ModifyerUnparsedName name, Destination requesting )
                    throws RWException {
                return (InputTransformation) new In().createTransformation( name, requesting );
            }
        }

        /**
         * A Content Declaration.
         * 
         * @author mgp
         *
         */
        private final class TransformerDeclaration {
            /**
             * Holds the TransformerName
             */
            private final Pattern PARSE_PATTERN = Pattern.compile( ModifyerUnparsedName.PATTERN );

            private ModifyerName transformerName = null;
            private ContentName contentName = null;

            protected TransformerDeclaration( ModifyerUnparsedName givenName ) {
                Matcher test = PARSE_PATTERN.matcher( givenName.toString() );
                if ( test.matches() ) {
                    String transformerDefinition = test.group( 2 );
                    String contentDefinition = test.group( 4 );
                    this.transformerName = ModifyerName.of( transformerDefinition );
                    this.contentName = ContentName.of( contentDefinition );
                }
            }

            protected boolean isParametrized() {
                return !this.contentName.isEmpty();
            }

            public ModifyerName getTransformerName() {
                return this.transformerName;
            }

            public ContentName getContentName() {
                return this.contentName;
            }
        }

        protected boolean transformsOnInput( ModifyerUnparsedName givenName ) {
            Class clazz = (Class) CONTENTMODIFIERS.get( givenName );
            if ( clazz == null ) {
                outerLog.error( "No transformer named " + U.w( givenName ) + "has been registered!" );
                return false;
            }
            return InputTransformation.class.isAssignableFrom( clazz );
        }

        protected boolean transformsOnOutput( ModifyerUnparsedName givenName ) {
            Class clazz = (Class) CONTENTMODIFIERS.get( givenName );
            if ( clazz == null ) {
                outerLog.error( "No transformer named " + U.w( givenName ) + "has been registered!" );
                return false;
            }
            return OutputTransformation.class.isAssignableFrom( clazz );
        }
    }

}