package de.mgpit.types;


import java.io.File;
import java.io.IOException;

import de.mgpit.oracle.reports.plugin.commons.U;

/**
 * 
 * @author mgp
 *
 *         A transformer name.
 * 
 */
public final class Filename extends TypedString {
    public static Filename of( String name ) {
        return new Filename( name );
    }

    public static Filename of( File file ) {
        return new Filename( file.getPath() );
    }
    
    public static Filename filenameOnlyOf( File file ) {
        return new Filename( file.getName() );
    }

    public static Filename absoluteOf( File file ) {
        try {
            return new Filename( file.getCanonicalPath() );
        } catch ( IOException io ) {
            return new Filename( file.getAbsolutePath() );
        }
    }

    String name = "";

    private Filename( String name ) {
        this.name = name.trim();
    }

    protected String value() {
        return this.name;
    }

    public Filename withExtension( String newExtension ) {
        String value = this.value();
        if ( U.isEmpty( value ) ) {
            return null;
        }
        if ( !U.isEmpty( newExtension ) ) {
            String fileNameWithExtension;

            int dotPosition = value.lastIndexOf( '.' );
            if ( dotPosition > 0 ) {
                String currentExtension = value.substring( dotPosition + 1 );
                fileNameWithExtension = (currentExtension.equalsIgnoreCase( newExtension ) ? value : value + "." + newExtension);
            } else {
                fileNameWithExtension = value + "." + newExtension;
            }
            return Filename.of( fileNameWithExtension );
        }
        return Filename.of( this.value() );
    }
}