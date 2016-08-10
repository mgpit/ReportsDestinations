package de.mgpit.oracle.reports.plugin.destination.mq;


import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;

import de.mgpit.oracle.reports.plugin.commons.MQ;
import de.mgpit.oracle.reports.plugin.commons.U;
import de.mgpit.oracle.reports.plugin.destination.MgpDestination;
import oracle.reports.RWException;
import oracle.reports.utility.Utility;

public final class MQDestination extends MgpDestination {

    private static final Logger LOG = Logger.getLogger( MQDestination.class );

    private static Map virtualDestinations;

    private MQ mq;

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
     * Send a file from the current distribution to the destination.
     * 
     * @param isMainFile
     *            flag if the file to be distributed is the main file
     * @param cacheFileName
     *            full file name of the cache file to be distributed
     * @param fileFormat
     *            file format code of the file to be distributed
     * @param fileSize
     *            file size of the file to be distributed
     * 
     */
    protected void sendFile( boolean isMainFile, String cacheFileName, short fileFormat, long fileSize ) throws RWException {
        if ( isMainFile ) {
            getLogger().info( "Sending main file from cache " + cacheFileName );
        } else {
            getLogger().info( "Sending additional file from cache " + cacheFileName );
        }
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

        if ( continueToSend ) {
            continueToSend = true;
        } else {
            getLogger().warn( "Cannot continue to send ..." );
        }
        return continueToSend;
    }

    /**
     * Initialize the destination on Report Server startup. Will mainly
     * initialize log4j Logging.
     * 
     * @param destinationsProperties
     * @throws RWException
     */
    public static void init( Properties destinationsProperties ) throws RWException {
        // MgpDestination.init( destinationsProperties );
        initLogging( destinationsProperties, MQDestination.class );
        registerVirtualDestinations( destinationsProperties );
        LOG.info( "Destination " + U.w( MQDestination.class.getName() ) + " started." );
    }

    private static void registerVirtualDestinations( Properties destinationsProperties ) throws RWException {
        Enumeration keys = destinationsProperties.keys();
        LOG.info( "About to register virtual destinations ..." );
        boolean registrationErrorOccured = false;
        while ( keys.hasMoreElements() ) {
            String key = (String) keys.nextElement();
            if ( declaresVirtualDestinationPlugin( key ) ) {
                String virtualDestinationName = extractVirtualDestinationName( key );
                String virtualDestinationClassName = destinationsProperties.getProperty( key );
                boolean success = registerVirtualDestination( virtualDestinationName, virtualDestinationClassName );
                if ( !success ) {
                    registrationErrorOccured = true;
                }
            }
        }
        if ( registrationErrorOccured ) {
            LOG.error( "Could not register all Contetn Plugins!" );
            // throw Utility.newRWException( new Exception(
            // "At least one Plugin could not be found! See Destination's logfile for more information!" ) );
        }
    }

    private static boolean registerVirtualDestination( String pluginName, String pluginClassName ) {
        LOG.info( " >>> About to register plugin named " + U.w( pluginName ) );
        if ( virtualDestinations == null ) {
            virtualDestinations = new HashMap( 7 );
        }
        Class clazz = null;
        try {
            clazz = Class.forName( pluginClassName );
        } catch ( ClassNotFoundException cnf ) {
            LOG.fatal( cnf );
            return false;
        }
        virtualDestinations.put( pluginName, clazz );
        return true;
    }

    private static boolean declaresVirtualDestinationPlugin( String key ) {
        return key.startsWith( "virtual." );
    }

    private static String extractVirtualDestinationName( String key ) {
        String[] pathElements = key.split( "\\." );
        int numberOfElements = pathElements.length;
        if ( numberOfElements < 2 ) {
            LOG.warn( "Got invalid virtual destination name: " + U.w( key ) );
            return null;
        }
        return pathElements[numberOfElements - 1];
    }

    public static void shutdown() {
        MgpDestination.shutdown();
        LOG.info( "Destination " + U.w( MQDestination.class.getName() ) + " shut down." );
    }
    
    protected Logger getLogger() {
        return LOG;
    }
}
