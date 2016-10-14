package de.mgpit.oracle.reports.plugin.destination.mq;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
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
import de.mgpit.oracle.reports.plugin.destination.content.types.InputTransformation;
import de.mgpit.oracle.reports.plugin.destination.content.types.OutputTransformation;
import de.mgpit.oracle.reports.plugin.destination.content.types.Transformation;
import de.mgpit.oracle.reports.plugin.destination.content.types.TransformerName;
import oracle.reports.RWException;
import oracle.reports.utility.Utility;

public final class MQDestination extends MgpDestination {

    private static final Logger LOG = Logger.getLogger( MQDestination.class );

    private MQ mq;

    private static final int SOME_PRIME = 23;
    private static Map CONTENTMODIFIERS = new HashMap( SOME_PRIME ); // instantiated for synchronization ...
    private static Map ALIASES = new HashMap( SOME_PRIME );

    private OutputTransformation[] outputTransformationChain = {};
    private InputTransformation[] inputTransformationChain = {};

    /**
     * Stop the distribution cycle.
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
     * Start a new distribution cycle for a report to this destination. Will
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
                continueToSend = true;
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

    protected void sendMainFile( String cacheFileFilename, short fileFormat ) throws RWException {
        getLogger().info( "Sending MAIN file to " + getClass().getName() );
        InputStream source = getContent( IOUtility.asFile( cacheFileFilename ) );
        try {
            String targetFileName = getDesname()+".mq";
            // TODO: Hack Hack Hack 
            if ( this.outputTransformationChain != null) {
                final int namer = this.outputTransformationChain.length-1;
                targetFileName = IOUtility.withExtension( targetFileName , this.outputTransformationChain[namer].fileExtension() );
            }
            File targetFile = IOUtility.asFile( targetFileName );
            FileOutputStream fileOut = IOUtility.asFileOutputStream( targetFile );
            OutputStream target = wrapWithOutputTransformers( fileOut );
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

    protected void sendAdditionalFile( String cacheFileFilename, short fileFormat ) throws RWException {
        getLogger().info( "Sending Other file to " + getClass().getName() );
    }

    protected InputStream getContent( File file ) throws RWException {
        InputStream sourceInput = super.getContent( file );
        return wrapWithInputTransformers( sourceInput );
    }

    /**
     * Wrap the output wiht {@link OutputTransformation}s.
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
     * Wrap the input with {@link InputTransformation}s.
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
     * Initialize the destination on Report Server startup.
     * <ul>
     *  <li>initialize log4j</li>
     *  <li>register declared content modifiers</li>
     *  <li>register declared aliases</li>
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
        registerConfiguredAliases( destinationsProperties );
        LOG.info( "Destination " + U.w( MQDestination.class.getName() ) + " started." );
    }

    private static void registerConfiguredContentModifiers( Properties destinationsProperties ) throws RWException {
        Enumeration keys = destinationsProperties.keys();
        LOG.info( "About to search for and register virtual destinations ..." );
        boolean registrationErrorOccured = false;

        synchronized (CONTENTMODIFIERS) {
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

    private static void registerConfiguredAliases( Properties destinationsProperties ) throws RWException {
        Enumeration keys = destinationsProperties.keys();
        synchronized (ALIASES) {
            while ( keys.hasMoreElements() ) {
                String key = (String) keys.nextElement();
                if ( isAliasDefintion( key ) ) {
                    String value = destinationsProperties.getProperty( key );
                    ALIASES.put( key, value );
                    LOG.info( "Registered alias " + U.w( key ) + " for " + U.w( value ) );
                }
            }
        }
    }

    private static boolean isAliasDefintion( String key ) {
        return key.startsWith( "alias." );
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
