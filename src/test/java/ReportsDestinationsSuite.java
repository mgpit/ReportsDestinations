

import de.mgpit.oracle.reports.plugin.commons.ConfigurationTest;
import de.mgpit.oracle.reports.plugin.destination.cdm.schema.MarshallTest;
import junit.framework.Test;
import junit.framework.TestSuite;

public class ReportsDestinationsSuite {

    public static Test suite() {
        TestSuite suite = new TestSuite( ReportsDestinationsSuite.class.getName() );
        //$JUnit-BEGIN$
        suite.addTestSuite( ConfigurationTest.class );
        suite.addTestSuite( MarshallTest.class );
        //$JUnit-END$
        return suite;
    }

}
