package de.mgpit.oracle.reports.plugin.destination.mq;


import java.util.Properties;

import org.apache.log4j.Logger;

import de.mgpit.oracle.reports.plugin.commons.MQ;
import de.mgpit.oracle.reports.plugin.commons.U;
import de.mgpit.oracle.reports.plugin.destination.MgpDestination;
import de.mgpit.oracle.reports.plugin.destination.zip.ZipDestination;
import oracle.reports.RWException;
import oracle.reports.server.Destination;
import oracle.reports.utility.Utility;

public final class MQDestination extends MgpDestination {

    private static Logger LOG = Logger.getLogger( MQDestination.class );

    private static boolean keepConnection = false;
    private MQ mq;
    
    /**
     * Stop the distribution cycle.
     */
    protected void stop() throws RWException {
        try {
            super.stop();
        } catch ( Exception any ) {
            LOG.error( "Error during finishing distribution. See following message(s)!" );
            LOG.error( any );
            RWException rwException = Utility.newRWException( any );
            throw rwException;
        }
        LOG.info( "Finished distribution to " + U.w( mq ) );
    }

    /**
     * Send another file from the current distribution to the destination.
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
        // NOP
    }

    /**
     * Start a new distribution cycle for a report to this destination. Will
     * distribute one or many files depending on the format.
     * 
     * @param allProperties
     *            all properties (system and user parameters) passed to the
     *            report
     * @param targetName
     *            target name of the distribution. Will be the entry name of the
     *            ZIP file.
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
        MgpDestination.init( destinationsProperties );
        LOG.info( "Destination " + U.w( ZipDestination.class.getName() ) + " started." );
    }

    public static void shutdown() {
        MgpDestination.shutdown();
        LOG.info( "Destination shut down." );
    }    
}
