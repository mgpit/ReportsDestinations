import java.awt.Color;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Set;

import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfDictionary;
import com.lowagie.text.pdf.PdfName;
import com.lowagie.text.pdf.PdfObject;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfWriter;

import junit.framework.TestCase;

public class ArbitraryTest extends TestCase {

    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testSHA() {
        assertEquals( "iText SHA does not match!", "c83884df914b34c7c9f1023f83205a54d596d21a",
                "c83884df914b34c7c9f1023f83205a54d596d21a" );
    }

    public void testCreatePdf() throws Exception {
        final String RESULT = "O:\\tmp\\reports\\output\\testCreatePdf.pdf";
        // step 1
        Document document = new Document();
        // step 2
        PdfWriter.getInstance( document, new FileOutputStream( RESULT ) );
        document.setPageSize( PageSize.A5 );
        document.setMargins( 36, 72, 108, 180 );
        document.setMarginMirroring( true );
        // step 3
        document.open();
        // step 4
        Rectangle r = new Rectangle( document.getPageSize() );
        r.setGrayFill( 0.5f );
        document.add( r );
        Paragraph p = new Paragraph( new Chunk(
                "The left margin of this odd page is 36pt (0.5 inch); " + "the right margin 72pt (1 inch); "
                        + "the top margin 108pt (1.5 inch); " + "the bottom margin 180pt (2.5 inch).",
                FontFactory.getFont( FontFactory.HELVETICA, 14, Font.ITALIC, new Color( 204, 0, 204 ) ) ) );

        p.setFirstLineIndent( 12 );
        document.add( p );
        Paragraph paragraph = new Paragraph();
        paragraph.setFirstLineIndent( 36 );
        paragraph.setAlignment( Element.ALIGN_JUSTIFIED );
        for ( int i = 0; i < 20; i++ ) {
            paragraph.add( "Hello World! Hello People! " + "Hello Sky! Hello Sun! Hello Moon! Hello Stars!" );
        }
        document.add( paragraph );
        document.add(
                new Paragraph( new Chunk("The right margin of this even page is 36pt (0.5 inch); " + "the left margin 72pt (1 inch)." 
                , FontFactory.getFont( FontFactory.HELVETICA, 13, Font.BOLD, new Color(0, 204, 204 ) ) ) ) );
        // step 5
        document.close();
    }

    public void testReadExisting() throws Exception {
        final String filename = "O:\\tmp\\reports\\output\\contracts_20160809.pdf";
        PrintWriter writer = new PrintWriter( System.out );
        PdfReader reader = new PdfReader( filename );
        writer.println( filename );
        writer.print( "Number of pages: " );
        writer.println( reader.getNumberOfPages() );
        PdfDictionary page1 = reader.getPageN( 1 );
        Set page1Keys = page1.getKeys();
        Iterator page1Inspect = page1Keys.iterator();
        while ( page1Inspect.hasNext() ) {
            PdfName key = (PdfName) page1Inspect.next();
            PdfObject value = (PdfObject) page1.get( key );
            writer.print( key.toString() );
            writer.print( "->" );
            writer.print( value.getClass() );
            writer.print( "::" );
            writer.println( value.toString() );
        }
        Rectangle mediabox = reader.getPageSize( 1 );
        writer.print( "Size of page 1: [" );
        writer.print( mediabox.left() );
        writer.print( ',' );
        writer.print( mediabox.bottom() );
        writer.print( ',' );
        writer.print( mediabox.right() );
        writer.print( ',' );
        writer.print( mediabox.top() );
        writer.println( "]" );
        writer.print( "Rotation of page 1: " );
        writer.println( reader.getPageRotation( 1 ) );
        writer.print( "Page size with rotation of page 1: " );
        writer.println( reader.getPageSizeWithRotation( 1 ) );
        writer.print( "Is rebuilt? " );
        writer.println( reader.isRebuilt() );
        writer.print( "Is encrypted? " );
        writer.println( reader.isEncrypted() );
        writer.println();
        writer.flush();
        reader.close();
    }

}
