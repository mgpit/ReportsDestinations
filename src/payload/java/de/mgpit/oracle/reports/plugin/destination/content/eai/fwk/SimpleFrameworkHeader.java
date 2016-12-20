package de.mgpit.oracle.reports.plugin.destination.content.eai.fwk;


import java.util.Properties;

import de.mgpit.oracle.reports.plugin.commons.U;
import de.mgpit.oracle.reports.plugin.destination.content.AbstractXmlHeader;
import de.mgpit.oracle.reports.plugin.destination.content.eai.Unifier;
import de.mgpit.oracle.reports.plugin.destination.content.types.BufferingHeader;
import de.mgpit.xml.XML;

public class SimpleFrameworkHeader extends AbstractXmlHeader implements BufferingHeader {
    
    protected String getHeaderAsStringPropulatedWith( Properties parameters ) throws Exception {

        //@formatter:off
        XML fwk = XML.newDocument( encoding().name() );
        fwk.add( "framework" ).nest()
            .add( "meta" ).nest()
                .add( "objectId" ).withData( "" )
                .add( "unifier" ).withData( Unifier.next() )
                .unnest()
            .add( "attach" ).attribute( "size", parameters.getProperty( BufferingHeader.SIZE_PROPERTY ) )
            .unnest()
        ;
        //@formatter:on
        return fwk.toString().concat( XML.NL );
    }

}
