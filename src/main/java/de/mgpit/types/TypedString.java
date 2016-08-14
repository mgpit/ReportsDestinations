package de.mgpit.types;

public abstract class TypedString {
    
    public String toString() {
        return this.value();
    }
    
    protected abstract String value();
    
    public final boolean equals(Object other) {
        if (this==other) {
            return true;
        };
        if ( other.getClass() == this.getClass() ) {
            TypedString otherTyped = (TypedString)other;
            if ( this.value() == null && otherTyped.value() == null ) {
                return true;
            }
            return this.value().equals( otherTyped.value() );
        } else {
            return false;
        }
    }

    public final int hashCode() {
        return (this.value()==null)?0:this.value().hashCode();
    }
    
    public int compareTo( Object o ){
        if ( this.getClass() != o.getClass() ) {
            throw new ClassCastException();
        }
        return compareTo((TypedString)o);
    }
    
    public int compareTo( TypedString otherTyped ){
        if ( this.getClass() != otherTyped.getClass() ) {
            throw new ClassCastException();
        }
        if ( this.value() != null ){
            return this.value().compareTo(  otherTyped.value() );
        } else if ( otherTyped != null ) {
            return otherTyped.value().compareTo( this.value() );
        } else {
            // must be null both ...
            return 0;
        }
    }

}
