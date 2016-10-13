package de.mgpit.oracle.reports.plugin.destination.mq;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;

import de.mgpit.oracle.reports.plugin.commons.MQ;
import de.mgpit.oracle.reports.plugin.commons.U;
import de.mgpit.oracle.reports.plugin.commons.io.IOUtility;
import de.mgpit.oracle.reports.plugin.destination.MgpDestination;
import de.mgpit.oracle.reports.plugin.destination.content.decorators.InputTransformation;
import de.mgpit.oracle.reports.plugin.destination.content.decorators.Transformation;
import de.mgpit.oracle.reports.plugin.destination.content.decorators.Transformation.PluginName;
import oracle.reports.RWException;
import oracle.reports.utility.Utility;

public final class MQDestination extends MgpDestination {

    private static final Logger LOG = Logger.getLogger( MQDestination.class );

    private MQ mq;
    private String[] transformationChain = null;

    private static final int SOME_PRIME = 23; 
    private static Map CONTENTMODIFIERS = new HashMap( SOME_PRIME ); // instantiated for synchronization ...
    private static Map ALIASES = new HashMap( SOME_PRIME );

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

        boolean continueToSend = super.start( allProperties, targetName, totalNumberOfFiles, totalFileSize, mainFormat );
        extractDeclaredTransformationChain( allProperties );

        if ( continueToSend ) {
            continueToSend = true;
        } else {
            getLogger().warn( "Cannot continue to send ..." );
        }
        return continueToSend;
    }

    private void extractDeclaredTransformationChain( Properties allProperties ) {
        String transformationDeclaration = allProperties.getProperty( "transform" );
        if ( transformationDeclaration != null ) {
            this.transformationChain = transformationDeclaration.split( "::" );
        }
    }

    protected void sendMainFile( String cacheFileFilename, short fileFormat ) throws RWException {
        getLogger().info( "Sending MAIN file to " + getClass().getName() );
        InputStream source = getContent( IOUtility.asLogfileFilename( cacheFileFilename ) );
        File targetFile = IOUtility.asFile( Utility.getLogsDir(), IOUtility.fileNameOnly( getDesname() ) );
        try {
            FileOutputStream fileOut = IOUtility.asFileOutputStream( targetFile );
            IOUtility.copyFromTo( source, fileOut );
        } catch ( FileNotFoundException fileNotFound ) {
            getLogger().error( "Error during distribution! Could not find file to add!" );
            throw Utility.newRWException( fileNotFound );
        } catch ( IOException ioex ) {
            getLogger().error( "Error during distribution! Could not copy file content!", ioex );
            throw Utility.newRWException( ioex );
        }

    }

    protected void sendAdditionalFile( String cacheFileFilename, short fileFormat ) throws RWException {
        getLogger().info( "Sending Other file to " + getClass().getName() );
    }

    protected InputStream getContent( File file ) throws RWException {
        InputStream sourceInput = super.getContent( file );
        if ( this.transformationChain != null && transformationChain.length > 0 ) {
            return applyTransformers( sourceInput );
        } else {
            return sourceInput;
        }
    }

    private InputStream applyTransformers( InputStream initialStream ) throws RWException {
        InputStream wrapped = initialStream;
        final int numberOfTransformers = this.transformationChain.length;
        final Properties allProperties = getProperties();
        for ( int runIndex = 0; runIndex < numberOfTransformers; runIndex++ ) {
            String givenName = this.transformationChain[runIndex];
            InputTransformation transformer = getNewPluginInstance( PluginName.of( givenName ) );
            wrapped = transformer.forInput( wrapped, allProperties );
            getLogger().info( "Transformer for " + U.w( givenName ) + " has been applied successfully." );
        }
        return wrapped;
    }

    protected static InputTransformation getNewPluginInstance( PluginName name ) throws RWException {
        Class clazz = (Class) CONTENTMODIFIERS.get( name );
        if ( clazz != null ) {
            try {
                Object newInstance = clazz.newInstance();
                InputTransformation transformer = (InputTransformation) newInstance;
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

    /**
     * Initialize the destination on Report Server startup. Will mainly
     * initialize log4j Logging.
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
                    PluginName name = extractContentModificationPluginName( key );
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

    private static boolean registerContentModifier( PluginName name, String implementingClassName ) {
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

    private static PluginName extractContentModificationPluginName( String key ) {
        String[] pathElements = key.split( "\\." );
        int numberOfElements = pathElements.length;
        if ( numberOfElements < 2 ) {
            LOG.warn( "Got invalid Content Transformername: " + U.w( key ) );
            return null;
        }
        int indexOfContentModificationPluginName = --numberOfElements;
        return PluginName.of( pathElements[indexOfContentModificationPluginName] );
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

}
