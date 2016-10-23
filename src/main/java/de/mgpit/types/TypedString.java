package de.mgpit.types;


/**
 * 
 * @author mgp
 *
 *         <p>
 *         Used for typed Strings to avoid those foo( String, String, ... , String ) methods where one has to look up the correct sequence of their
 *         parameters over and over.
 * 
 */
public abstract class TypedString {
    
    public String toString() {
        return this.value();
    }

    protected abstract String value();

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
        return this.value() != null && !"".equals( this.value() );
    }
    
    public boolean isNull() {
        return this.value() == null;
    }
    
    public boolean isNotNull() {
        return this.value() != null;
    }

}
