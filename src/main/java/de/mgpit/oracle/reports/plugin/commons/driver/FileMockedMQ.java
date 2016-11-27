package de.mgpit.oracle.reports.plugin.commons.driver;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.log4j.Logger;

import de.mgpit.oracle.reports.plugin.commons.U;
import de.mgpit.oracle.reports.plugin.commons.io.IOUtility;
import oracle.reports.utility.Utility;

public class FileMockedMQ extends MQ {

    private FileOutputStream fileOut;

    public FileMockedMQ( Configuration configuration ) {
        super( configuration );
    }

    public OutputStream newMessage() {
        return new OutputStream() {

            public void write( int b ) throws IOException {
                if ( fileOut != null ) {
                    fileOut.write( b );
                }
            }

            public void flush() throws IOException {
                fileOut.flush();
            }

            public void close() throws IOException {
                fileOut.close();
            }

        };
    }

    public void connect() throws Exception {
        File targetDirectory = new File( U.coalesce( Utility.getTempDir(), IOUtility.getSystemTempDir() ) );
        File targetFile = File.createTempFile( "QMSALSAXP.IQ.DEVELOPMENT__", ".mq", targetDirectory );
        Logger.getRootLogger().info( "Connected to: " + targetFile.getAbsolutePath() );
        fileOut = new FileOutputStream( targetFile );
    }

    public void disconnect() throws Exception {
    }

}
