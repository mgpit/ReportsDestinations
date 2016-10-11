package de.mgpit.oracle.reports.plugin.destination.content.decorators;


import java.io.ByteArrayInputStream;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;

import de.mgpit.oracle.reports.plugin.commons.Magic;
import de.mgpit.oracle.reports.plugin.destination.content.EnvelopeInput;

public class SimpleCdmDecorator extends EnvelopeDecorator {
    private static Logger LOG = Logger.getLogger( SimpleCdmDecorator.class );

    Document cdm;

    public SimpleCdmDecorator() {
        // DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        // DocumentBuilder cdmBuilder;
        // try {
        // cdmBuilder = builderFactory.newDocumentBuilder();
        //
        // cdm = cdmBuilder.newDocument();
        //
        // Element cdmdoc = cdm.createElement( "cdmdoc" );
        // cdm.appendChild( cdmdoc );
        //
        // Element data = cdm.createElement( "data" );
        // Element info = cdm.createElement( "info" );
        //
        // cdmdoc.appendChild( data );
        // cdmdoc.appendChild( info );
        //
        // Element created = cdm.createElement( "created" );
        // SimpleDateFormat dateFormat = new SimpleDateFormat( "YYYY.MM.ddTHH:mm:ss" );
        // String now = dateFormat.format( new Date() );
        // created.setNodeValue( now );
        // info.appendChild( created );
        //
        // TransformerFactory transformerFactory = TransformerFactory.newInstance();
        // Transformer transformer = transformerFactory.newTransformer();
        // DOMSource source = new DOMSource( cdm );
        // StreamResult console = new StreamResult( new ByteArrayOutputStream() );
        //
        // transformer.transform( source, console );
        //
        // } catch ( ParserConfigurationException configException ) {
        // LOG.error( "Cannot create SimpleCdm!", configException );
        // } catch ( TransformerConfigurationException configException ) {
        // LOG.error( "Cannot create SimpleCdm!", configException );
        // } catch ( TransformerException transformerException ) {
        // LOG.error( "Cannot create SimpleCdm!", transformerException );
        // }
    }

    protected EnvelopeInput getCdm( Properties parameters ) {
        return new EnvelopeInput() {
            private static final int IN_HEADER = 1;
            private static final int IN_DATA = 2;
            private static final int IN_FOOTER = 3;

            private ByteArrayInputStream header = new ByteArrayInputStream(
                    "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<cdmdoc>\n    <data>\n".getBytes() );
            private ByteArrayInputStream footer = new ByteArrayInputStream( "\n    </data>\n</cdmdoc>".getBytes() );
            private int currentBlock = IN_HEADER;

            public boolean dataWanted() {
                boolean wanted = false;
                switch ( currentBlock ) {
                case IN_DATA:
                    wanted = true;
                    break;
                default:
                    wanted = false;
                }
                return wanted;
            }

            public int read() {
                int aByte = Magic.END_OF_STREAM;
                switch ( currentBlock ) {
                case IN_HEADER:
                    aByte = readHeader();
                    if ( aByte == Magic.END_OF_STREAM ) {
                        currentBlock = IN_DATA;
                        aByte = this.read();
                    }
                    break;
                case IN_DATA:
                    break;
                case IN_FOOTER:
                    aByte = readFooter();
                    break;
                default:
                    aByte = Magic.END_OF_STREAM;
                }
                return aByte;
            }

            private int readFooter() {
                return footer.read();
            };

            private int readHeader() {
                return header.read();
            }

            public void dataFinished() {
                if ( currentBlock != IN_DATA ) {
                    throw new IllegalStateException( "dataFinished() not allowed when Cdm doesn't want data!" );
                }
                currentBlock = IN_FOOTER;
            }
        };
    }

}
