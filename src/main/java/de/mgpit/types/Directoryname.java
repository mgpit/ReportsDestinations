package de.mgpit.types;


/**
 * 
 * @author mgp
 *
 *         A directory name.
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

    private static Directoryname NULL_VALUE = Directoryname.of( (String) null );

    public Directoryname copy() {
        return Directoryname.of( this.value() );
    }

    public Directoryname concat( String str ) {
        return (this.isNotNull()) ? Directoryname.of( this.value().concat( str ) ) : NULL_VALUE;
    }

    public Directoryname trim() {
        return (this.isNotNull()) ? Directoryname.of( this.value().trim() ) : NULL_VALUE;
    }
}