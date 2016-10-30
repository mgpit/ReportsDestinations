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
 * A transformer name.
 * <p>
 * The name is unparsed and unvalidated.
 *
 * @author mgp
 */
public final class ModifyerName extends TypedString {

    public static ModifyerName of( String name ) {
        return new ModifyerName( name );
    }

    String name = NONE;

    private ModifyerName( String name ) {
        this.name = cleaned( name );
    }

    protected String value() {
        return this.name;
    }

    private static ModifyerName NULL_VALUE = ModifyerName.of( (String) null );

    public ModifyerName copy() {
        return ModifyerName.of( this.value() );
    }

    public ModifyerName concat( String str ) {
        return (this.isNotNull()) ? ModifyerName.of( this.value().concat( str ) ) : NULL_VALUE;
    }

    public ModifyerName trim() {
        return (this.isNotNull()) ? ModifyerName.of( this.value().trim() ) : NULL_VALUE;
    }
}