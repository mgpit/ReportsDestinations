package de.mgpit.types;

/**
 * 
 * @author mgp
 *
 * A transformer name.
 * 
 */
public final class Directoryname extends TypedString {
    public static Directoryname of( String name ) {
        return new Directoryname( name );
    }

    String name = "";

    private Directoryname( String name ) {
        this.name = name.trim();
    }

    protected String value() {
        return this.name;
    }
}