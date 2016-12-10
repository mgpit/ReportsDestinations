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
package de.mgpit.oracle.reports.plugin.destination.content.cdm;


import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import de.mgpit.xml.XML;
import de.mgpit.xml.XML.XMLFragment;

/**
 * A simple Cdm.
 * 
 * @author mgp
 *
 */
public class SimpleCdm extends AbstractCdm {

    private static final String DATA_ELEMENT = "<data>";

    public String getEnvelopeAsStringPopulatedWith( Properties parameters ) throws Exception {
        final SimpleDateFormat dateFormat = new SimpleDateFormat( "yyyy.MM.dd HH:mm:ss" );
        final String now = dateFormat.format( new Date() );

        XML cdm = XML.newDocument();
        cdm.add( "cdmdoc" )
           .attribute( "created", now )
           .nest();

        XMLFragment address = XML.newFragment( cdm );
        address.add( "address" );
        boolean hasAddress = false;
        address.nest();
        for ( int lineNumber = 1; lineNumber < 7; lineNumber++ ) {
            final String key = "address_line_" + lineNumber;
            if ( parameters.containsKey( key ) ) {
                address.add( key )
                       .withData( parameters.getProperty( key, "" ) );
                hasAddress = true;
            }
        }
        // address.up();
        if ( hasAddress ) {
            cdm.add( address );
        }
        cdm.add( "fizz" )
           .nest()
           .add( "buzz" )
           .nest()
           .add( "foo" )
           .add( "bar" )
           .withData( "Lorem Ipsum" )
           .unnest()
           .add( "doe" )
           .withData( "Dolor sit amet" )
           .unnest()
           .add( "data" )
           .withData( "" );

        // System.out.println( cdm.toString() );
        // System.out.println( "\n" );
        if ( hasAddress ) System.out.println( address.toString() );
        return cdm.toString();

    }

    protected String getSplitAtToken() {
        return DATA_ELEMENT;
    }

}
