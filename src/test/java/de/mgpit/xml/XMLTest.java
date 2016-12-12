package de.mgpit.xml;


import java.util.Calendar;

import junit.framework.TestCase;

public class XMLTest extends TestCase {

    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testXML() {
        boolean exceptionOccured = false;
        try {
            XML xml = XML.newDocument( "LATIN1");
            xml.add( "customer" )
               .attribute( "id", "1234567" )
               .attribute( "exported", Calendar.getInstance().getTime() )
               .nest()
               .add( "name" )
               .withData( "Müller" )
               .add( "firstname" )
               .withData( "Max" )
               .add( "birtday" )
               .withData( "1970-02-03" )
               .add( "address" )
               .attribute( "primary", true )
               .nest()
               .add( "street" )
               .withData( "Dorfstraße" )
               .add( "number" )
               .withData( "7" )
               .add( "zip" )
               .withData( "12345" )
               .add( "city" )
               .withData( "Musterstadt" )
               .unnest()
               .unnest();

            String serialized = xml.toString();
            System.out.println( serialized );

        } catch ( Exception any ) {
            any.printStackTrace( System.err );
            exceptionOccured = true;
        }
        assertFalse( exceptionOccured );
    }
}
