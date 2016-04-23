package de.mgpit.oracle.reports.plugin.commons;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import oracle.reports.utility.Utility;

/**
 * 
 * @author mgp
 * 
 * Utility methods for working with files and streams.
 *
 */
public class IOUtility {
    
    /**
     * Return a file name with extension .zip. If the file name already has extension .zip just return the file name provided.
     * @param fileName to add the .zip extension to
     * @return fileName with extension .zip
     */
    public static String withZipExtension( String fileName ) {
        String fileNameWithZipExtension;
        int dotPosition = fileName.lastIndexOf( '.' );
        if ( dotPosition > 0 ) {
            String currentExtension = fileName.substring( dotPosition + 1 );
            fileNameWithZipExtension = (currentExtension.equalsIgnoreCase( "zip" ) ? fileName : fileName + ".zip");
        } else {
            fileNameWithZipExtension = fileName + ".zip";
        }
        return fileNameWithZipExtension;
    }
    
    /**
     * Return the file name part of a full file name / path.
     * @param fullFileName full file name
     * @return the file name part
     */
    public static String filenameOnly( String fullFileName ) {
        return Utility.fileNameOnly( fullFileName );
    }
    
    /**
     * Copies the content from a source stream to a target stream.
     * @param source InputStream with the source data
     * @param destination OutputStream to copy to
     * @throws IOException
     */
    public static void copyFromTo( InputStream source, OutputStream destination ) throws IOException {
        byte[] buffer = new byte[Units.FOUR_KILOBYTE];
        int bytesRead;
        while ((bytesRead = source.read(buffer)) >= 0) {
            destination.write(buffer, 0, bytesRead);
        }
    }
    
    public static byte[] asByteArray( InputStream in ) throws IOException {
        ByteArrayOutputStream temporary = new ByteArrayOutputStream();
        copyFromTo( in, temporary );
        return temporary.toByteArray();
    }
    
}
