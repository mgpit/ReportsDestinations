/*
 * Copyright 2016 Marco Pauls www.mgp-it.de
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

/**
 * @license APACHE-2.0
 */
package de.mgpit.types;


import java.io.File;
import java.io.IOException;

import de.mgpit.oracle.reports.plugin.commons.U;
import de.mgpit.oracle.reports.plugin.commons.io.IOUtility;

/**
 * 
 * A file name.
 *
 * @author mgp
 */
public final class Filename extends TypedString {

    public static Filename of( String fullname ) {
        return new Filename( fullname );
    }

    public static Filename of( String name, String extension ) {
        return new Filename( name, extension );
    }

    public static Filename of( File file ) {
        return of( file.getPath() );
    }

    public static Filename filenameNameOnlyOf( String fullname ) {
        return filenameNameOnlyOf( new File( fullname ) );
    }

    public static Filename filenameNameOnlyOf( Filename filename ) {
        return filenameNameOnlyOf( IOUtility.fileFromName( filename ) );
    }

    public static Filename filenameNameOnlyOf( File file ) {
        return new Filename( file.getName() );
    }

    public static Filename absoluteOf( File file ) {
        try {
            return new Filename( file.getCanonicalPath() );
        } catch ( IOException io ) {
            return new Filename( file.getAbsolutePath() );
        }
    }

    private String name = NONE;

    public String getName() {
        return this.name;
    }

    private String extension = NONE;

    public String getExtension() {
        return this.extension;
    }

    private Filename() {
        // NOOP
    }

    private Filename( String fullname ) {
        final String cleaned = cleaned( fullname );
        final int lastDotPosition = cleaned.lastIndexOf( '.' );
        if ( lastDotPosition > 0 ) {
            this.name = cleaned.substring( 0, lastDotPosition );
            this.extension = cleaned.substring( lastDotPosition + 1 );
        } else {
            this.name = cleaned;
            this.extension = NONE;
        }
    }

    private Filename( String name, String extension ) {
        this.name = name;
        this.extension = extension;
    }

    public String value() {
        StringBuffer sb = new StringBuffer( getName() );
        if ( !U.isEmpty( getExtension() ) ) {
            sb.append( "." )
              .append( getExtension() );
        }
        return sb.toString();
    }

    private static final String DOT = ".";

    public Filename withNewExtension( String newExtension ) {
        String cleanedExtension = cleaned( newExtension );
        if ( U.isEmpty( cleanedExtension ) ) {
            return new Filename( getName(), NONE );
        }
        int offset = 0;
        for ( ; cleanedExtension.startsWith( DOT, offset ); offset++ );
        String extenstionStrippedFromLeadingDots = (offset > 0) ? cleanedExtension.substring( offset ) : cleanedExtension;

        return new Filename( getName(), extenstionStrippedFromLeadingDots );
    }

    private static Filename NULL_VALUE = new Filename(); // Filename.of( (String) null );

    public Filename copy() {
        return Filename.of( this.value() );
    }

    public Filename concat( Filename other ) {
        if (other == null) {
            throw new IllegalArgumentException( "Other musn't be null" );
        }
        if ( this.isNotNull() ) {
            return Filename.of( this.value().concat( other.value() ), this.extension.concat( other.extension ) );
        } else {
            return other.copy();
        }
    }

    public Filename trim() {
        return (this.isNotNull()) ? new Filename( this.name.trim(), this.extension.trim() ) : NULL_VALUE;
    }

}