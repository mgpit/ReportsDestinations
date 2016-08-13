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
import de.mgpit.oracle.reports.plugin.destination.content.ContentModificationPlugin;
import de.mgpit.oracle.reports.plugin.destination.content.ContentModificationPlugin.PluginName;
import oracle.reports.RWException;
import oracle.reports.utility.Utility;

public final class MQDestination extends MgpDestination {

    private static final Logger LOG = Logger.getLogger( MQDestination.class );

    private MQ mq;
    private String[] transformationChain = null;

    private static Map CONTENTMODIFIERS = new HashMap( 23 ); // instantiated for synchronization ...

    protected static ContentModificationPlugin getNewPluginInstance( PluginName name ) throws RWException {
        Class clazz = (Class) CONTENTMODIFIERS.get( name );
        if ( clazz != null ) {
            try {
                Object newInstance = clazz.newInstance();
                ContentModificationPlugin transformer = (ContentModificationPlugin) newInstance;
                return transformer;
            } catch ( InstantiationException cannotInstantiate ) {
                LOG.fatal( cannotInstantiate );
                LOG.fatal( "Cannot instantiate " + U.w( clazz.getName() ) );
                throw Utility.newRWException( cannotInstantiate );
            } catch ( Exception anyOther ) {
                LOG.error( anyOther );
                throw Utility.newRWException( anyOther );
            }
        } else {
            IllegalArgumentException illegalArgument = new IllegalArgumentException(
                    "No transformer named " + U.w( name ) + " has been registered!" );
            LOG.error( illegalArgument );
            throw Utility.newRWException( illegalArgument );
        }
    }

    /**
     * Stop the distribution cycle.
     */
    protected void stop() throws RWException {
        try {
            super.stop();
        } catch ( Exception any ) {
            getLogger().error( "Error during finishing distribution. See following message(s)!" );
            getLogger().error( any );
            throw Utility.newRWException( any );
        }
        getLogger().info( "Finished distribution to " + U.w( mq ) );
    }

    /**
     * Start a new distribution cycle for a report to this destination. Will
     * distribute one or many files depending on the format.
     * 
     * @param allProperties
     *            all properties (system and user parameters) passed to the
     *            report
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
        extractTransformationChain( allProperties );

        if ( continueToSend ) {
            continueToSend = true;
        } else {
            getLogger().warn( "Cannot continue to send ..." );
        }
        return continueToSend;
    }

    private void extractTransformationChain( Properties allProperties ) {
        String transformationDeclaration = allProperties.getProperty( "transform" );
        if ( transformationDeclaration != null ) {
            this.transformationChain = transformationDeclaration.split( "::" );
        }
    }

    protected void sendMainFile( String cacheFileFilename, short fileFormat ) throws RWException {
        getLogger().info( "Sending MAIN file to " + getClass().getName() );
        InputStream source = getContent( new File( cacheFileFilename ) );
        File targetFile = new File( new File( Utility.getLogsDir() ), IOUtility.fileNameOnly( getDesname() ) );
        try {
            FileOutputStream fileOut = new FileOutputStream( targetFile );
            IOUtility.copyFromTo( source, fileOut );
        } catch ( FileNotFoundException fileNotFound ) {
            throw Utility.newRWException( fileNotFound );
        } catch ( IOException ioex ) {
            throw Utility.newRWException( ioex );
        }

    }

    protected void sendAdditionalFile( String cacheFileFilename, short fileFormat ) throws RWException {
        // TODO Auto-generated method stub
        getLogger().info( "Sending Other file to " + getClass().getName() );
    }

    protected InputStream getContent( File file ) throws RWException {
        InputStream sourceInput = super.getContent( file );
        if ( this.transformationChain != null ) {
            return applyTransformers( sourceInput );
        } else {
            return sourceInput;
        }
    }

    private InputStream applyTransformers( InputStream initialStream ) throws RWException {
        InputStream wrapped = initialStream;
        for ( int runIndex = 0; runIndex < this.transformationChain.length; runIndex++ ) {
            String givenName = this.transformationChain[runIndex];
            ContentModificationPlugin transformer = getNewPluginInstance( PluginName.of( givenName ) );
            wrapped = transformer.wrap( wrapped );
            getLogger().info( "Transformer for " + U.w( givenName ) + " has been applied successfully." );
        }
        return wrapped;
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
        dumpProperties( destinationsProperties, LOG );
        registerContentModifiers( destinationsProperties );
        LOG.info( "Destination " + U.w( MQDestination.class.getName() ) + " started." );
    }

    private static void registerContentModifiers( Properties destinationsProperties ) throws RWException {
        Enumeration keys = destinationsProperties.keys();
        LOG.info( "About to search for and register virtual destinations ..." );
        boolean registrationErrorOccured = false;

        synchronized (CONTENTMODIFIERS) {
            while ( keys.hasMoreElements() ) {
                String key = (String) keys.nextElement();
                if ( keyIsConentModificationPluginDefinition( key ) ) {
                    PluginName name = extractContentModificationPluginName( key );
                    String virtualDestinationClassName = destinationsProperties.getProperty( key );
                    boolean success = registerTransformer( name, virtualDestinationClassName );
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

    private static boolean registerTransformer( PluginName name, String implementingClassName ) {
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

    private static boolean keyIsConentModificationPluginDefinition( String key ) {
        return key.startsWith( ContentModificationPlugin.PREFIX );
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

    public static void shutdown() {
        MgpDestination.shutdown();
        LOG.info( "Destination " + U.w( MQDestination.class.getName() ) + " shut down." );
    }

    protected Logger getLogger() {
        return LOG;
    }

}
