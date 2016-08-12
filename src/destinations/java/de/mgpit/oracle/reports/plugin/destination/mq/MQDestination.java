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

    private static Map virtualDestinations = new HashMap( 7 ); // instantiated for synchronization ...

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

    protected void sendMainFile( String cacheFileFilename, short fileFormat ) throws RWException {
        // TODO Auto-generated method stub

    }

    protected void sendOtherFile( String cacheFileFilename, short fileFormat ) throws RWException {
        // TODO Auto-generated method stub

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
        registerVirtualDestinations( destinationsProperties );
        LOG.info( "Destination " + U.w( MQDestination.class.getName() ) + " started." );
    }

    private static void registerVirtualDestinations( Properties destinationsProperties ) throws RWException {
        Enumeration keys = destinationsProperties.keys();
        LOG.info( "About to search for and register virtual destinations ..." );
        boolean registrationErrorOccured = false;

        synchronized (virtualDestinations) {
            while ( keys.hasMoreElements() ) {
                String key = (String) keys.nextElement();
                if ( keyIsVirtualDestinationDefinition( key ) ) {
                    String virtualDestinationName = extractVirtualDestinationName( key );
                    String virtualDestinationClassName = destinationsProperties.getProperty( key );
                    boolean success = registerVirtualDestination( virtualDestinationName, virtualDestinationClassName );
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

    private static boolean registerVirtualDestination( String pluginName, String pluginClassName ) {
        LOG.info( " >>> About to register plugin named " + U.w( pluginName ) );
        Class clazz = null;
        try {
            clazz = Class.forName( pluginClassName );
        } catch ( ClassNotFoundException cnf ) {
            LOG.warn( cnf );
            return false;
        }
        virtualDestinations.put( pluginName, clazz );
        return true;
    }

    private static boolean keyIsVirtualDestinationDefinition( String key ) {
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
