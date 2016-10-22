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

import de.mgpit.oracle.reports.plugin.commons.U;
import de.mgpit.oracle.reports.plugin.commons.Units;
import de.mgpit.types.Directoryname;
import de.mgpit.types.Filename;
import oracle.reports.utility.Utility;
import sun.security.action.GetPropertyAction;

/**
 * 
 * @author mgp
 * 
 *         Provider of methods for working with files and streams, for example methods for
 *          <ul>
 *              <li>filename handling</li>
 *              <li>testing for file existence</li>
 *              <li>opening files as streams</li>
 *              <li>stream copying</li>
 *              <li>byte to string conversion</li>
 *          </ul>
 */
public class IOUtility {

    private static String tmpdir;

    /**
     * Gets the temporary directory.
     * 
     * @return string with the pathname of the directory denoted by <code>java.io.tmpdir</code>.
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
     * 
     * @param str
     *            name string to convert
     * @return a log file name
     */
    public static Filename asLogfileFilename( final String str ) {
        String replaced = str.replace( '.', '_' );
        return Filename.of( replaced ).withExtension( "log" );
    }

    /**
     * Converts the given filename to its platform specific format.
     * <p>
     * This is merely using the correct path separator character.
     * 
     * @param filename
     *            file name to be converted
     * @return a string with the file name according to the current platforms naming conventions
     */
    public static Filename asPlatformFilename( final Filename filename ) {
        try {
            File f = asFile( filename );
            return Filename.of( f.getPath() );
        } catch ( NullPointerException nex ) {
            return null;
        }
    }

    /**
     * Converts the given filename to its platform specific absolute format.
     * <p>
     * This is using the correct path separator and interpreting relative paths in the context of the working directory.
     * 
     * @param filename
     *            file name to be converted
     * @return a string with the file name according to the current platforms naming conventions
     */
    public static Filename asPlatformAbsoluteFilename( final Filename filename ) {
        try {
            File f = asFile( filename );
            return Filename.of( f );
        } catch ( NullPointerException nex ) {
            return null;
        }
    }

    /**
     * Returns a file name ending with the extension given.
     * <p>
     * Leaves the filename unchanged if it already has the correct extension.
     * 
     * @param fileName
     *            to add the extension to
     * @param newExtension
     *            file name extension to set
     * @return string containing a filename ending with the extension given
     */
    public static Filename withExtension( final Filename fileName, final String newExtension ) {
        return (fileName == null)?null:fileName.withExtension( newExtension );
    }

    /**
     * Returns a file name ending with the extension {@code .zip}.
     * <p>
     * Leaves the filename unchanged if it already has the correct extension.
     * 
     * @param fileName
     *            to add the extension to
     * @return string containing a filename ending with the extension {@code .zip}
     */
    public static Filename withZipExtension( final Filename fileName ) {
        return withExtension( fileName, "zip" );
    }

    /**
     * Returns the file name part of a full file name / path.
     * 
     * @param fullFileName
     *            full file name
     * @return the file name part
     */
    public static Filename fileNameOnly( final Filename fullFileName ) {
        return (fullFileName == null) ? null : Filename.of( Utility.fileNameOnly( fullFileName.toString() ) );
    }

    /**
     * Constructs a full file name (path and file name) from the directory and file name given.
     * <p>
     * The resulting file name will <strong>not</strong> be in absolute nor canonical form.
     * If you want an absolute file name you may also consider {@link #asPlatformAbsoluteFilename(String)}.
     * 
     * @param directoryName
     *            the direcotory name
     * @param fileName
     *            the file name
     * 
     * @return string with the full file name
     */
    public static String fullFileName( final String directoryName, final String fileName ) {
        return Utility.fullFileName( fileName, directoryName );
    }

    /**
     * Copies the content from a source stream to a target stream.
     * <p>
     * <strong>The source and target are left open!</strong>
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

    /**
     * Copies the content from a source stream to a target stream.
     * <p>
     * <strong>Closes both source and destination!</strong>
     * 
     * @param source
     *            InputStream with the source data
     * @param destination
     *            OutputStream to copy to
     * @throws IOException
     */
    public static void copyFromToAndThenClose( final InputStream source, final OutputStream destination ) throws IOException {
        try {
            copyFromTo( source, destination );
        } finally {
            try {
                destination.close();
            } catch ( Exception ignore ) {
            }
            try {
                source.close();
            } catch ( Exception ignore ) {
            }
        }

    }

    /**
     * Converts the input stream to a byte array.
     * 
     * @param in
     *            the input stream
     * @return byte array with the content of the input stream.
     * @throws IOException
     */
    public static byte[] asByteArray( final InputStream in ) throws IOException {
        final ByteArrayOutputStream temporary = new ByteArrayOutputStream();
        copyFromToAndThenClose( new BufferedInputStream( in ), temporary );
        return temporary.toByteArray();
    }

    /**
     * Returns a String with the input stream's content interpreted in UTF8 encoding.
     * 
     * @param in
     *            the input stream
     * @return a String with the input stream interpreted in UTF8 encoding
     * @throws IOException
     */
    public static String inputAsUTF8String( final InputStream in ) throws IOException {
        final ByteArrayOutputStream temporary = new ByteArrayOutputStream();
        copyFromToAndThenClose( new BufferedInputStream( in ), temporary );
        return temporary.toString( "UTF-8" );
    }

    /**
     * Returns a String with the input stream's content interpreted in platform encoding.
     * 
     * @param in
     *            the input stream
     * @return a String with the input stream interpreted in platform encoding
     * @throws IOException
     */
    public static String inputAsPlatformString( final InputStream in ) throws IOException {
        final ByteArrayOutputStream temporary = new ByteArrayOutputStream();
        copyFromToAndThenClose( new BufferedInputStream( in ), temporary );
        return temporary.toString();
    }

    /**
     * Constructs a file from the file name given.
     * 
     * @param fileName
     *            file's name
     * @return file
     */
    public static File asFile( Filename fileName ) {
        return new File( fileName.toString() );
    }

    /**
     * Constructs a file from the directory and file name given.
     * 
     * @param directoryName
     *            file's directory
     * @param fileName
     *            file's name
     * @return file
     */
    public static File asFile( Directoryname directoryName, Filename fileName ) {
        return new File( new File( directoryName.toString() ), fileName.toString() );
    }

    /**
     * Constructs a {@link FileInputStream} from the {@link File} given.
     * 
     * @param file
     *            the file
     * @return FileInputStream on the file
     * 
     * @throws FileNotFoundException
     */
    public static FileInputStream asFileInputStream( File file ) throws FileNotFoundException {
        return new FileInputStream( file );
    }

    /**
     * Constructs a {@link FileInputStream} from the file denoted by the file name given.
     * 
     * @param fileName
     *            file's name
     * @return FileInputStream on the file
     * @throws FileNotFoundException
     */
    public static FileInputStream asFileInputStream( Filename fileName ) throws FileNotFoundException {
        return asFileInputStream( asFile( fileName ) );
    }

    /**
     * Constructs a {@link FileOutputStream} from the {@link File} given.
     * 
     * @param file
     *            the file
     * @return FileOutputStream on the file
     * @throws FileNotFoundException
     */
    public static FileOutputStream asFileOutputStream( final File file ) throws FileNotFoundException {
        return new FileOutputStream( file );
    }

    /**
     * Constructs a {@link FileOutputStream} from the file denoted by the file name given.
     * 
     * @param fileName
     *            file's name
     * @return FileOutputStream on the file
     * @throws FileNotFoundException
     */
    public static FileOutputStream asFileOutputStream( final Filename fileName ) throws FileNotFoundException {
        return asFileOutputStream( asFile( fileName ) );
    }

    /**
     * Checks if the file given exists.
     * 
     * @param file
     *            the file
     * @return {@code true} if the file exists {@code false} else
     */
    public static boolean exists( File file ) {
        return file.exists();
    }

    /**
     * Checks if the file denoted by the file name exists.
     * 
     * @param filename
     *            file's name
     * @return {@code true} if the file exists {@code false} else
     */
    public static boolean exists( final Filename filename ) {
        return exists( asFile( filename ) );
    }

}
