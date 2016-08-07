package de.mgpit.oracle.reports.plugin.destination.content.decorators;


import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import de.mgpit.modernizr.java.lang.Enum;
import de.mgpit.oracle.reports.plugin.commons.U;

public final class Steps extends de.mgpit.modernizr.java.lang.Enum {
    private static final long serialVersionUID = -758473813852381487L;

    public static final Steps InHeader = new Steps( "InHeader", 0 );
    public static final Steps InPayload = new Steps( "InPayload", 1 );
    public static final Steps InTrailer = new Steps( "InTrailer", 2 );

    Class clazz;

    private String name;
    private int ordinal;

    private Steps( String name, int ordinal ) {
        super( name, ordinal );
    }

    public String toString() {
        return name;
    }

    public int compareTo( Object o ) {
        if ( this.getClass() != o.getClass() ) {
            throw new ClassCastException();
        }
        Steps other = (Steps) o;
        return this.ordinal - other.ordinal;
    }

    /* The following methods have been copied from java.lang.Enum ... */

    public static Enum valueOf( Class enumType, String name ) {
        return Steps.valueOf( name );
    }

    public static Steps valueOf( String name ) {
        U.assertNotNull( name, "Name is Null!" );
        Steps result = (Steps) ENUM_CONSTANT_DIRECTORY.get( name );

        if ( result != null ) {
            return result;
        }

        throw new IllegalArgumentException( "No constant " + Steps.class.getName() + "." + name );
    }

    /* This is some of the stuff the compiler generates when compiling an Enum ... */

    private static final Steps[] ENUM$VALUES = { InHeader, InPayload, InTrailer };

    public static Steps[] values() {
        Steps allSteps[], allStepsCopied[];
        int i;
        System.arraycopy( allSteps = ENUM$VALUES, 0, allStepsCopied = new Steps[i = allSteps.length], 0, i );
        return allStepsCopied;
    }

    private static Map ENUM_CONSTANT_DIRECTORY = null;

    {
        ENUM_CONSTANT_DIRECTORY = new HashMap( ENUM$VALUES.length * 2 );
        Iterator values = Arrays.asList( ENUM$VALUES ).iterator();
        while ( values.hasNext() ) {
            Steps step = (Steps) values.next();
            ENUM_CONSTANT_DIRECTORY.put( step.name, step );
        }

    }

}
