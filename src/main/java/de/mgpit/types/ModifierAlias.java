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
 * A modifier alias name.
 * <p>
 * The name is unparsed and unvalidated.
 *
 * @author mgp
 */
public final class ModifierAlias extends TypedString {

    public static ModifierAlias of( String name ) {
        return new ModifierAlias( name );
    }

    private String name = NONE;

    private ModifierAlias( String name ) {
        this.name = cleaned( name );
    }

    protected String value() {
        return this.name;
    }

    private static ModifierAlias NULL_VALUE = ModifierAlias.of( (String) null );

    public ModifierAlias copy() {
        return ModifierAlias.of( this.value() );
    }

    public ModifierAlias concat( String str ) {
        return (this.isNotNull()) ? ModifierAlias.of( this.value().concat( str ) ) : NULL_VALUE;
    }

    public ModifierAlias trim() {
        return (this.isNotNull()) ? ModifierAlias.of( this.value().trim() ) : NULL_VALUE;
    }
    
}