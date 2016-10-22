package de.mgpit.types;

/**
 * 
 * @author mgp
 *
 * A transformer name.
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
}