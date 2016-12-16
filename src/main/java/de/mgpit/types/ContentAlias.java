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
 * A conent alias name.
 * <p>
 * The name is unparsed and unvalidated.
 *
 * @author mgp
 */
public final class ContentAlias extends TypedString {
    
    public static final ContentAlias EMPTY_VALUE = ContentAlias.of( NONE );
    
    public static ContentAlias of( String name ) {
        return new ContentAlias( name );
    }

    private String name = NONE;

    private ContentAlias( String name ) {
        this.name = cleaned( name );
    }

    protected String value() {
        return this.name;
    }

    private static ContentAlias NULL_VALUE = ContentAlias.of( (String) null );

    public ContentAlias copy() {
        return ContentAlias.of( this.value() );
    }

    public ContentAlias concat( ContentAlias other ) {
        if (other == null) {
            throw new IllegalArgumentException( "Other musn't be null" );
        }
        if ( this.isNotNull() ) {
            return ContentAlias.of( this.value().concat( other.value() ) );
        } else {
            return other.copy();
        }
    }

    public ContentAlias trim() {
        return (this.isNotNull()) ? ContentAlias.of( this.value().trim() ) : NULL_VALUE;
    }
}