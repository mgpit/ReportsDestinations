
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

import org.apache.log4j.Logger;

import de.mgpit.oracle.reports.plugin.commons.io.IOUtility;

/**
 * 
 * A directory name.
 *
 * @author mgp
 */
public final class Directoryname extends TypedString {
    public static Directoryname of( String name ) {
        return new Directoryname( name );
    }

    private String name = "";

    private Directoryname( String name ) {
        this.name = cleaned( name );
    }

    protected String value() {
        return this.name;
    }

    private static Directoryname NULL_VALUE = Directoryname.of( (String) null );

    public Directoryname copy() {
        return Directoryname.of( this.value() );
    }

    public Directoryname concat( Directoryname other ) {
        if (other == null) {
            throw new IllegalArgumentException( "Other musn't be null" );
        }
        if ( this.isNotNull() ) {
            return Directoryname.of( this.value().concat( other.value() ) );
        } else {
            return other.copy();
        }
    }

    public Directoryname trim() {
        return (this.isNotNull()) ? Directoryname.of( this.value().trim() ) : NULL_VALUE;
    }
    
    /**
     * Checks if this is a valid directory, i.e. must be
     * <ul>
     * <li>an existing directory</li>
     * <li>readable</li>
     * <li>writable</li>
     * </ul>
     * 
     * @return <code>true</code> if the pathname is a valid directory, <code>false</code> else.
     */
    public boolean isValidDirectory( ) {
        if ( this.isEmpty() ) {
            Logger.getRootLogger().warn( "I think I am Empty!" );
            return false;
        }
        File possibleDirectory = IOUtility.fileFromName( this );
        boolean valid = possibleDirectory.isDirectory() && possibleDirectory.canRead() && possibleDirectory.canWrite();
        return valid;
    }
}