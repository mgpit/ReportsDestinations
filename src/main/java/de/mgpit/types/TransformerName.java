package de.mgpit.types;


/**
 * 
 * @author mgp
 *
 *         A transformer name.
 * 
 */
public final class TransformerName extends TypedString {
    public static TransformerName of( String name ) {
        return new TransformerName( name );
    }

    String name = "";

    private TransformerName( String name ) {
        this.name = name.trim();
    }

    protected String value() {
        return this.name;
    }

    private static TransformerName NULL_VALUE = TransformerName.of( (String) null );

    public TransformerName copy() {
        return TransformerName.of( this.value() );
    }

    public TransformerName concat( String str ) {
        return (this.isNotNull()) ? TransformerName.of( this.value().concat( str ) ) : NULL_VALUE;
    }

    public TransformerName trim() {
        return (this.isNotNull()) ? TransformerName.of( this.value().trim() ) : NULL_VALUE;
    }
}