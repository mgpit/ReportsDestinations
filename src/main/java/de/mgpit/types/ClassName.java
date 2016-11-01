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


/**
 * 
 * A class name.
 * <p>
 * The name is unparsed and unvalidated.
 *
 * @author mgp
 */
public final class ClassName extends TypedString {
    
    public static ClassName of( String name ) {
        return new ClassName( name );
    }

    private String name = "";

    private ClassName( String name ) {
        this.name = name.trim();
    }

    protected String value() {
        return this.name;
    }

    private static ClassName NULL_VALUE = ClassName.of( (String) null );

    public ClassName copy() {
        return ClassName.of( this.value() );
    }

    public ClassName concat( String str ) {
        return (this.isNotNull()) ? ClassName.of( this.value().concat( str ) ) : NULL_VALUE;
    }

    public ClassName trim() {
        return (this.isNotNull()) ? ClassName.of( this.value().trim() ) : NULL_VALUE;
    }
}