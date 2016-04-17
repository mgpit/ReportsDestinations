package de.mgpit.spielwiese;

public class Super {
    protected String superDuper;
    public Super(){
        System.out.println( "Super NOARG" );
    }
    
    public Super( String superDuper ){
        this.superDuper = superDuper;
        System.out.println( "Super (String) with: " + superDuper + " ..." );
    }

}
