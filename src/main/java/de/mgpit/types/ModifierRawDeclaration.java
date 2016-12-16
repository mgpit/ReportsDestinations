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
 * A raw modifier declaration.
 * <p>
 * The declaration is unparsed and unvalidated.
 *
 * @author mgp
 */
public final class ModifierRawDeclaration extends TypedString {
    /**
     * Holds the string representation of the RegExp describing a valid TransformerName declaration.
     * <p>
     * Valid declarations which match the RegExp are, for example
     * <ul>
     * <li>{@code ASIMPLENAME }</li>
     * <li>{@code ANAM3W1THD1G1TS }</li>
     * <li>{@code ANAMEWITH(PARAMETER}</li>
     * <li>{@code ANAMEWITH( PARAMETER )}</li>
     * <li>{@code ANAM3W1TH( PARA:METER ) }</li>
     * </ul>
     * i.e. a String which starts with an Alpha character, followed by zero or more Alphanumeric characters
     * and may be followed by a parameter name enclosed in parantheses and arbitrary whitespace around
     * name parameter and parantheses.
     * 
     * <pre>
     * modifierrawdeclaration ::= whitespace{*} name whitespace{*} parameter whitespace{*}
     *                   name ::= alpha{1} other{*} 
     *                  other ::= alphanum | ":" | "-" | "_" | "$"
     *              parameter ::= "(" whitespace{*}name whitespace{*} ")"
     * </pre>
     * <p>
     * The pattern has four capture groups. Group 2 will capture the modifier name 
     * and group 4 the content provider name - each without whitespace.
     */
    public static final String PATTERN = "^\\s*((\\p{Alpha}[\\p{Alnum}:_\\-.$]*\\s*)([(]\\s*(\\p{Alpha}[\\p{Alnum}:_\\-.$]*)\\s*[)])?)\\s*$";

    public static ModifierRawDeclaration of( String name ) {
        return new ModifierRawDeclaration( name );
    }

    private String name = NONE;

    private ModifierRawDeclaration( String name ) {
        this.name = cleaned( name );
    }

    protected String value() {
        return this.name;
    }

    private static ModifierRawDeclaration NULL_VALUE = ModifierRawDeclaration.of( (String) null );

    public ModifierRawDeclaration copy() {
        return ModifierRawDeclaration.of( this.value() );
    }

    public ModifierRawDeclaration concat( ModifierRawDeclaration other ) {
        if (other == null) {
            throw new IllegalArgumentException( "Other musn't be null" );
        }
        if ( this.isNotNull() ) {
            return ModifierRawDeclaration.of( this.value().concat( other.value() ) );
        } else {
            return other.copy();
        }
    }

    public ModifierRawDeclaration trim() {
        return (this.isNotNull()) ? ModifierRawDeclaration.of( this.value().trim() ) : NULL_VALUE;
    }
}