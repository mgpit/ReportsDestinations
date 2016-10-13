package de.mgpit.oracle.reports.plugin.commons.io;


import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.AccessController;

import de.mgpit.oracle.reports.plugin.commons.Units;
import oracle.reports.utility.Utility;
import sun.security.action.GetPropertyAction;

/**
 * 
 * @author mgp
 * 
 *         Utility methods for working with files and streams.
 *
 */
public class IOUtility {

    private static String tmpdir;

    /**
     * @return pathname of the directory denoted by <code>java.io.tmpdir</code>.
     */
    public static synchronized String getTempDir() {
        if ( tmpdir == null ) {
            GetPropertyAction a = new GetPropertyAction( "java.io.tmpdir" );
            tmpdir = ((String) AccessController.doPrivileged( a ));
        }
        return tmpdir;
    }

    /**
     * Creates a log file name from the string given.
     * <p>
     * Will replace all occurrences of <code>.</code> (dots) by <code>_</code> (underline) in the string given
     * and eventually add a <code>.log</code> file extension.
     * @param str
     *            name string to convert
     * @return a log file name
     */
    public static String asLogfileFilename( final String str ) {
        String replaced = str.replace( '.', '_' );
        return (replaced.endsWith( ".log" )) ? replaced : replaced + ".log";
    }

    public static String asPlatformFilename( final String filename ) {
        try {
            File f = asFile( filename );
            return f.getPath();
        } catch ( NullPointerException nex ) {
            return null;
        }
    }

    /**
     * Return a file name with extension .zip. If the file name already has extension .zip just return the file name provided.
     * 
     * @param fileName
     *            to add the .zip extension to
     * @return fileName with extension .zip
     */
    public static String withZipExtension( final String fileName ) {
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
     * 
     * @param fullFileName
     *            full file name
     * @return the file name part
     */
    public static String fileNameOnly( final String fullFileName ) {
        return (fullFileName == null) ? null : Utility.fileNameOnly( fullFileName );
    }

    /**
     * Construct a full file name (path and file name) from the parameters
     * 
     * @param directoryName
     * @param fileName
     * 
     * @return the full file name
     */
    public static String fullFileName( final String directoryName, final String fileName ) {
        return Utility.fullFileName( fileName, directoryName );
    }

    /**
     * Copies the content from a source stream to a target stream.
     * 
     * @param source
     *            InputStream with the source data
     * @param destination
     *            OutputStream to copy to
     * @throws IOException
     */
    public static void copyFromTo( final InputStream source, final OutputStream destination ) throws IOException {
        final byte[] buffer = new byte[Units.SIXTYFOUR_KILOBYTE];
        int bytesRead;
        while ( (bytesRead = source.read( buffer )) >= 0 ) {
            destination.write( buffer, 0, bytesRead );
        }
    }

    public static byte[] asByteArray( final InputStream in ) throws IOException {
        final ByteArrayOutputStream temporary = new ByteArrayOutputStream();
        copyFromTo( new BufferedInputStream( in ), temporary );
        return temporary.toByteArray();
    }

    public static String inputAsUTF8String( final InputStream in ) throws IOException {
        final ByteArrayOutputStream temporary = new ByteArrayOutputStream();
        copyFromTo( new BufferedInputStream( in ), temporary );
        return temporary.toString( "UTF-8" );
    }

    public static String inputAsPlatformString( final InputStream in ) throws IOException {
        final ByteArrayOutputStream temporary = new ByteArrayOutputStream();
        copyFromTo( new BufferedInputStream( in ), temporary );
        return temporary.toString();
    }
    
    public static File asFile( String fileName ) {
        return new File( fileName );
    }
    
    public static File asFile( String directoryName, String fileName ) {
        return new File( new File( directoryName ), fileName  );
    }
    
    public static FileInputStream asFileInputStream( File file ) throws FileNotFoundException {
        return new FileInputStream( file );
    }
    
    public static FileInputStream asFileInputStream( String fileName ) throws FileNotFoundException {
        return asFileInputStream( asFile( fileName ) );
    }
    
    public static FileOutputStream asFileOutputStream( File file ) throws FileNotFoundException {
        return new FileOutputStream( file );
    }
    
    public static FileOutputStream asFileOutputStream( String fileName ) throws FileNotFoundException {
        return asFileOutputStream( asFile( fileName ) );
    }
    
    public static boolean exists( File file ) {
        return file.exists();
    }
    public static boolean exists( String filename ) {
        return exists( asFile( filename ) );
    }

}
