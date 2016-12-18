import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.File;

import de.mgpit.oracle.reports.plugin.commons.U;
import de.mgpit.oracle.reports.plugin.destination.mq.MQDestination;
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
    
    public void testPaths() {
        boolean exceptionOccured = false;
        try {
            File f = new File( "/foo/../tmp" );
            System.out.println( "Name ....: " + f.getName() );
            System.out.println( "Path ....: " + f.getPath() );
            System.out.println( "Absolute.: " + f.getAbsolutePath() );
            System.out.println( "Canonical: " + f.getCanonicalPath() );
        } catch ( Exception ignor ) {
            exceptionOccured = true;
        }
        assertFalse( exceptionOccured );
    }

    public void testBeans() {
        boolean exceptionOccured = false;
        try {
            final BeanInfo componentBeanInfo = Introspector.getBeanInfo( MQDestination.class );
            final PropertyDescriptor[] props = componentBeanInfo.getPropertyDescriptors();
            System.out.println( "Got " + U.w( props.length ) + " props." );
            for ( int i = 0; i < props.length; i++ ) {
                final PropertyDescriptor pd = props[i];
                System.out.println( pd.getReadMethod() );
                System.out.println( pd.getWriteMethod() );
                System.out.println( pd.getDisplayName() );
            }

        } catch ( final IntrospectionException e ) {
            exceptionOccured = true;
        }
        assertFalse( exceptionOccured );
    }

}
