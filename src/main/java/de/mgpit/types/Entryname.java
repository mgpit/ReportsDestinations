package de.mgpit.types;


public class Entryname extends TypedString {

    private String name = NONE;

    public static Entryname of( String name ) {
        return new Entryname( name );
    }

    public static Entryname of( Filename filename ) {
        Filename tmp = Filename.filenameNameOnlyOf( filename );
        return new Entryname( tmp.value() );
    }

    protected String value() {
        return this.name;
    }

    private Entryname( String name ) {
        this.name = cleaned( name );
    }

    private static Entryname NULL_VALUE = Entryname.of( (String) null );

    public Entryname copy() {
        return Entryname.of( this.value() );
    }

    public Entryname concat( String str ) {
        return (this.isNotNull()) ? Entryname.of( this.value().concat( str ) ) : NULL_VALUE;
    }

    public Entryname trim() {
        return (this.isNotNull()) ? Entryname.of( this.value().trim() ) : NULL_VALUE;
    }

}
