package de.mgpit.spielwiese;


import junit.framework.TestCase;

public class SubSuperTest extends TestCase {

    public void testConstructor() {
        System.out.println( ">>>>>>>>>> First Super" );
        Super suppi = new Super();
        System.out.println( ">>>>>>>>>> Second Super" );
        Super suppi2 = new Super( "Lorem Ipsum" );
        
        System.out.println( ">>>>>>>>>> First Sub" );
        Sub subbi = new Sub();
        System.out.println( ">>>>>>>>>> Second Sub" );
        Sub subbi2 = new Sub( "Dolor Si Amet" );

        System.out.println( ">>>>>>>>>> First Under" );
        Under under = new Under();
        System.out.println( ">>>>>>>>>> Second Under" );
        Under under2 = new Under( "Et Semper Fides" );

        
        assertTrue( true );

    }

}
