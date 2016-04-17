package de.mgpit.spielwiese;

public class Sub extends Super {
    
    public Sub(){
        System.out.println( "Sub NOARG" );
    }
    
    public Sub( String superDuper ) {
        super( ">" +  superDuper );
        System.out.println( "Sub (String) with: " + superDuper + " ..." );
    }

}
