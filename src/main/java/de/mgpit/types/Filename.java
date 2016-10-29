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

/**
 * 
 * A file name.
 *
 * @author mgp
 */
public final class Filename extends TypedString {
    public static Filename of( String name ) {
        return new Filename( name );
    }

    public static Filename of( File file ) {
        return of( file.getPath() );
    }
    
    public static Filename filenameNameOnlyOf( String name ) {
        return filenameNameOnlyOf( new File( name ) );
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

    String name = NONE;

    private Filename( String name ) {
        this.name = cleaned( name );
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
            String currentExtension = (dotPosition>0)?value.substring( dotPosition + 1 ):"";
            
            boolean canKeep = currentExtension.equals( newExtension );
            Filename answer = (canKeep)?this.copy():this.concat("."+newExtension);
            return answer;
        }
        return copy();
    }
    
    private static Filename NULL_VALUE = Filename.of( (String)null );

    public Filename copy() {
        return Filename.of( this.value() );
    }
    public Filename concat( String str ) {
        return (this.isNotNull())?Filename.of( this.value().concat(str) ):NULL_VALUE;
    }
    
    public Filename trim() {
        return (this.isNotNull())?Filename.of( this.value().trim()):NULL_VALUE;
    }

}