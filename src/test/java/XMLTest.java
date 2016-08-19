import java.text.SimpleDateFormat;
import java.util.Date;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import junit.framework.TestCase;

public class XMLTest extends TestCase {

    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testDom() throws Exception {
        Document cdm;
        
        DefaultHandler defaulthandler = new DefaultHandler();
                
        oracle.xml.parser.v2.SAXParser saxParser = new oracle.xml.parser.v2.SAXParser();
     // XMLReaderFactory.createXMLReader( "oracle.xml.parser.v2.SAXParser" );
     // !!! saxParser.setDocumentHandler(defaulthandler);
        saxParser.setEntityResolver(defaulthandler);
        saxParser.setDTDHandler(defaulthandler);
        saxParser.setErrorHandler(defaulthandler);
        
        

        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder cdmBuilder;
        try {
            
            cdmBuilder = builderFactory.newDocumentBuilder();

            cdm = cdmBuilder.newDocument();

            Element cdmdoc = cdm.createElement( "cdmdoc" );
            cdm.appendChild( cdmdoc );

            Element data = cdm.createElement( "data" );
            Element info = cdm.createElement( "info" );

            cdmdoc.appendChild( data );
            cdmdoc.appendChild( info );

            Element created = cdm.createElement( "created" );
            SimpleDateFormat dateFormat = new SimpleDateFormat( "YYYY.MM.dd HH:mm:ss" );
            String now = dateFormat.format( new Date() );
            created.appendChild( cdm.createTextNode( now ) );
            info.appendChild( created );

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            
            System.out.println( "\n============================================================" );
            System.out.println( "Transformer Factory: " + transformerFactory.getClass() );
            System.out.println( "Transformer........: " + transformer.getClass() );
            System.out.println( "\n============================================================\n\n" );
            
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            
            DOMSource source = new DOMSource( cdm );
            // StreamResult bytes = new StreamResult( new ByteArrayOutputStream() );
            StreamResult console = new StreamResult( System.out );

            transformer.transform( source, console );
            
            DocumentFragment foo = cdm.createDocumentFragment();
            foo.appendChild( info );
            DOMSource fragment = new DOMSource( foo );
            
            System.out.println( "\n\n\n" );
            transformer.setOutputProperty( OutputKeys.OMIT_XML_DECLARATION, "yes" );
            transformer.transform( fragment, console );
            

        } catch ( ParserConfigurationException configException ) {
            // LOG.error( "Cannot create SimpleCdm!", configException );
        } catch ( TransformerConfigurationException configException ) {
            // LOG.error( "Cannot create SimpleCdm!", configException );
        } catch ( TransformerException transformerException ) {
            // LOG.error( "Cannot create SimpleCdm!", transformerException );
        }
    }

}
