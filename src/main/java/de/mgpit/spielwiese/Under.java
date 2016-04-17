package de.mgpit.spielwiese;

public class Under extends Sub {
    public Under(){
        System.out.println( "Under NOARG" );
    }
    
    public Under( String superDuper ) {
        super( ">" +  superDuper );
        System.out.println( "Under (String) with: " + superDuper + " ..." );
    }
}
