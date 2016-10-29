
/*
 * Copyright 2016 Marco Pauls www.mgp-it.de
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

/**
 * @license APACHE-2.0
 */
package de.mgpit.oracle.reports.plugin.destination;


import java.util.regex.Pattern;

import de.mgpit.oracle.reports.plugin.commons.U;
import de.mgpit.types.TransformerUnparsedName;

/**
 * 
 * 
 * Transformation chain declaration.
 * <p>
 * A transformation chain declaration can be passed to a Oracle Reports&trade; destination on reports execution.
 * A transformation chain is used to modify the output generated by the Oracle Reports&trade; server.
 * Applications are
 * <ul>
 * <li>Converting / Encoding the output to/as BASE64</li>
 * <li>Prepending an XML header before the output</li>
 * <li>Wrapping the output in an envelope like a SOAP message</li>
 * <li>...</li>
 * </ul>
 * <p>
 * Transformations can be chained (like Unix Pipes), for example
 * <ol>
 * <li>the report output could be BASE64 encoded</li>
 * <li>and then being wrapped into a SOAP message</li>
 * </ol>
 * <p>
 * The transformation chain is represented as literal
 * <ul>
 * <li>first to last: {@code TRANSFORMER1>>TRANSFORMER2>>...>>TRANSFORMER}<sub>n</sub>
 * <li>last to first: {@code TRANSFORMER1<<TRANSFORMER2<<...<<TRANSFORMER}<sub>m</sub>
 * </ul>
 * <small>(One has to admit this is a little bit over engineered, first to last with {@code ::} as separator would have been sufficient)</small>
 * <p>
 * A transformation chain should only contain tranformers for one use case, i.e.
 * <ul>
 * <li>transformers which transform on reading form the input, or</li>
 * <li>transformers which transofrm on writing to the output</li>
 * </ul>
 *
 * @author mgp
 * 
 */
public class TransformationChainDeclaration {

    private final String FIRST_2_LAST_SEPARATOR = ">>";
    private final String FIRST_2_LAST_EXPRESSION = "^([^><]+>>)*([^><])+$";
    private final Pattern FIRST_2_LAST = Pattern.compile( FIRST_2_LAST_EXPRESSION );

    private final String LAST_2_FIRST_SEPARATOR = "<<";
    private final String LAST_2_FIRST_EXPRESSION = "^([^><]+<<)*([^><])+$";
    private final Pattern LAST_2_FIRST = Pattern.compile( LAST_2_FIRST_EXPRESSION );

    public static TransformerUnparsedName[] extractNames( String literal ) {
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

    /**
     * Gets a list of {@code TransformerName}s contained in this declaration.
     * 
     * @return list
     */
    public TransformerUnparsedName[] get() {
        TransformerUnparsedName[] definitions = {};
        if ( this.isFirstToLastDefinition() ) {
            final String[] tokens = this.literal.split( FIRST_2_LAST_SEPARATOR );
            definitions = new TransformerUnparsedName[tokens.length];
            int targetIndex = 0;
            for ( int i = 0; i < tokens.length; i++ ) {
                definitions[targetIndex] = TransformerUnparsedName.of( tokens[i] );
                targetIndex++;
            }
        } else {
            final String[] tokens = this.literal.split( LAST_2_FIRST_SEPARATOR );
            definitions = new TransformerUnparsedName[tokens.length];
            int targetIndex = 0;
            for ( int i = tokens.length - 1; i >= 0; --i ) {
                definitions[targetIndex] = TransformerUnparsedName.of( tokens[i] );
                targetIndex++;
            }
        }
        return definitions;
    }

}
