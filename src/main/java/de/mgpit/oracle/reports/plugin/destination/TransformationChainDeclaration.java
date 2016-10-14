package de.mgpit.oracle.reports.plugin.destination;


import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import de.mgpit.oracle.reports.plugin.commons.U;
import de.mgpit.oracle.reports.plugin.destination.content.types.TransformerName;

public class TransformationChainDeclaration {
    
    private static final Logger LOG = Logger.getLogger( "de.mgpit.oracle.reports.plugin.destination.mq.MQDestination" );

    private final String FIRST_2_LAST_SEPARATOR = ">>";
    private final String FIRST_2_LAST_EXPRESSION = "^([^><]+>>)*([^><])+$";
    private final Pattern FIRST_2_LAST = Pattern.compile( FIRST_2_LAST_EXPRESSION );

    private final String LAST_2_FIRST_SEPARATOR = "<<";
    private final String LAST_2_FIRST_EXPRESSION = "^([^><]+<<)*([^><])+$";
    private final Pattern LAST_2_FIRST = Pattern.compile( LAST_2_FIRST_EXPRESSION );

    public static TransformerName[] extractNames( String literal ) {
        return new TransformationChainDeclaration( literal ).get();
    }

    private final String literal;

    private boolean isFirstToLastDefinition() {
        return FIRST_2_LAST.matcher( this.literal ).matches();
    }

    private boolean isLastToFirstDefinition() {
        return LAST_2_FIRST.matcher( this.literal ).matches();
    }

    private TransformationChainDeclaration( String literal ) {
        this.literal = literal;
        if ( !U.isEmpty( literal ) ) {
            if ( !isFirstToLastDefinition() && !isLastToFirstDefinition() ) {
                throw new IllegalArgumentException( "Invalid literal " + literal );
            }
        }
    };
    
    public TransformerName[] get() {
        TransformerName[] definitions = {};
        if ( this.isFirstToLastDefinition() ) {
            final String[] tokens = this.literal.split( FIRST_2_LAST_SEPARATOR );
            definitions = new TransformerName[tokens.length];
            int targetIndex = 0;
            for ( int i = 0; i < tokens.length; i++ ) {
                definitions[targetIndex] = TransformerName.of( tokens[i] );
                targetIndex++;
            }
        } else {
            final String[] tokens = this.literal.split( LAST_2_FIRST_SEPARATOR );
            definitions = new TransformerName[tokens.length];
            int targetIndex = 0;
            for ( int i = tokens.length-1; i >= 0 ; --i ) {
                definitions[targetIndex] = TransformerName.of( tokens[i] );
                targetIndex++;
            }
        }
        return definitions;
    }


}
