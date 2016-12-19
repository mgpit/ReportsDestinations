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
package de.mgpit.oracle.reports.plugin.destination.content.eai.cdm;


import java.util.Properties;

import de.mgpit.oracle.reports.plugin.destination.content.eai.cdm.SimpleCdm;
import junit.framework.TestCase;

public class SimpleCdmTest extends TestCase {

    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testBuild() {
        final Properties parameters = new Properties();
        for ( int lineNumber = 1; lineNumber < 6; lineNumber++ ) {
            final String key = "address_line_" + lineNumber;
            parameters.setProperty( key, key );
        }
        boolean exceptionOccured = false;
        
        try {
            SimpleCdm first = null, last = null;
            for ( int i = 0; i < 1010; i++ ) {
                final SimpleCdm simple = new SimpleCdm();
                simple.build( parameters );
                if ( i == 0 ) { first = simple; }
                if ( i == 1009 ) { last = simple; }
            }
            System.out.println( first.getText() );
            System.out.println( last.getText() );
        } catch ( Throwable anly ) {
            exceptionOccured = true;
            anly.printStackTrace( System.err );
        }

        assertFalse( exceptionOccured );
    }
}
