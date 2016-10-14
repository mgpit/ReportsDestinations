package de.mgpit.oracle.reports.plugin.destination.content.types;


import de.mgpit.types.TypedString;

public final class TransformerName extends TypedString {
    public static TransformerName of( String name ) {
        return new TransformerName( name );
    }

    String name = "";

    private TransformerName( String name ) {
        this.name = name;
    }

    protected String value() {
        return this.name;
    }
}