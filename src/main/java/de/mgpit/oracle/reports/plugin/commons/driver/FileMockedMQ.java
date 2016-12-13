package de.mgpit.oracle.reports.plugin.commons.driver;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.log4j.Logger;

import de.mgpit.oracle.reports.plugin.commons.U;
import de.mgpit.oracle.reports.plugin.commons.io.IOUtility;
import de.mgpit.types.Filename;
import oracle.reports.utility.Utility;

public class FileMockedMQ extends MQ {

    public FileMockedMQ( Configuration configuration ) {
        super( configuration );
    }

    public OutputStream newMessage() {
        return new OutputStream() {
            
            private File file;
            private FileOutputStream fileOut;

            {
                try {
                    File targetDirectory = new File( U.coalesce( Utility.getTempDir(), IOUtility.getSystemTempDir() ) );
                    String prefix = filenameFromConfiguration();
                    String suffix = ".part";
                    File file = File.createTempFile( prefix, suffix, targetDirectory );
                    Logger.getRootLogger()
                        .info( "Connected to: " + file.getAbsolutePath() );
                    fileOut = new FileOutputStream( file );
                } catch ( Exception cause ) {
                    Logger.getRootLogger().error( "Cannot get Output Stream", cause );
                    throw new RuntimeException( cause );
                }
            }

            public void write( int b ) throws IOException {
                if ( fileOut != null ) {
                    fileOut.write( b );
                }
            }

            public void flush() throws IOException {
                fileOut.flush();
            }

            public void close() throws IOException {
                flush();
                fileOut.close();
                final Filename finalFilename = Filename.of( file.getPath() ).withExtension( "mgf" );
                file.renameTo( IOUtility.fileFromName( finalFilename ) );
            }

        };
    }

    public void connect() throws Exception {}

    private String filenameFromConfiguration() {
        return configuration.getQueueManagerName() + "_" + configuration.getQueueName() + "_" + configuration.getChannelName()
                + "__";
    }

    public void disconnect() throws Exception {}

}
