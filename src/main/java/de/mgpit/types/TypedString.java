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


import org.apache.log4j.Logger;

import de.mgpit.oracle.reports.plugin.commons.U;

/**
 * A wrapper for types represented as {@code String}.
 * <p>
 * Used for minimizing those foo( String, String, ... , String ) methods where one has to look up the correct sequence of their
 * parameters over and over and which are error prone.
 * <p>
 * There are several coding best practices and IDE support to ensure that the sequence in which the parameters
 * are passed to a method is right, like
 * <ul>
 * <li>advocating short parameter lists</li>
 * <li>using builders</li>
 * <li>assigning more specific types to each of the parameters</li>
 * </ul>
 * The type wrapping approach is part of the latter.
 * <p>
 * Other languages have features like <em>keyword messages</em> (e.g. Smalltalk, ObjectiveC)
 * or <em>named parameter call syntax</em> (e.g. PL/SQL).
 * <p>
 * This version is for Java 1.4. Using Java 5+'s Generics would allow for a much more flexible and
 * less code redundant implementation ...
 * 
 * @author mgp
 */
public abstract class TypedString {

    protected final String NONE = "";

    public String toString() {
        return this.value();
    }

    protected abstract String value();

    protected String assertAtLeastNone( String str ) {
        return U.isEmpty( str ) ? NONE : str;
    }

    protected String cleaned( String str ) {
        return assertAtLeastNone( str ).trim();
    }

    public final boolean equals( Object other ) {
        if ( this == other ) {
            return true;
        }
        ;
        if ( other.getClass() == this.getClass() ) {
            TypedString otherTyped = (TypedString) other;
            if ( this.value() == null && otherTyped.value() == null ) {
                return true;
            }
            return this.value().equals( otherTyped.value() );
        } else {
            return false;
        }
    }

    public final int hashCode() {
        return (this.value() == null) ? 0 : this.value().hashCode();
    }

    public int compareTo( Object o ) {
        if ( this.getClass() != o.getClass() ) {
            throw new ClassCastException();
        }
        return compareTo( (TypedString) o );
    }

    public int compareTo( TypedString otherTyped ) {
        if ( this.getClass() != otherTyped.getClass() ) {
            throw new ClassCastException();
        }
        if ( this.value() != null ) {
            return this.value().compareTo( otherTyped.value() );
        } else if ( otherTyped != null ) {
            return otherTyped.value().compareTo( this.value() );
        } else {
            // must be null both ...
            return 0;
        }
    }

    public boolean isEmpty() {
        return this.isNull() || this.isNone();
    }
    
    
    public boolean isNone( ){
        return NONE.equals( this.value() );
    }
    
    public boolean isNotNone() {
        return !NONE.equals( this.value() );
    }

    public boolean isNull() {
        return this.value() == null;
    }

    public boolean isNotNull() {
        return this.value() != null;
    }

}
