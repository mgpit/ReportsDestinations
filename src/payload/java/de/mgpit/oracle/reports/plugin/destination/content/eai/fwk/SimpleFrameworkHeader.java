package de.mgpit.oracle.reports.plugin.destination.content.eai.fwk;

import java.util.Properties;

import de.mgpit.xml.XML;

public class SimpleFrameworkHeader extends AbstractHeader {

    protected String getHeaderAsStringPropulatedWith( Properties parameters ) throws Exception {
        //@formatter:off
        XML fwk = XML.newDocument();
        fwk.add( "framework" ).nest()
            .add( "meta" ).nest()
                .add( "objectId" ).withData( "" ).unnest()
            .unnest()
        ;
        //@formatter:on
        return fwk.toString();
    }

}
