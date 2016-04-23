package de.mgpit.oracle.reports.plugin.destination.content;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.evermind.io.IOUtils;

import de.mgpit.oracle.reports.plugin.commons.IOUtility;
import oracle.reports.RWException;
import oracle.reports.utility.Utility;

public abstract class ContentModifier {
    private ContentModifier next;
    
    public boolean hasNext() {
        return next != null;
    }
    
    public ContentModifier followedBy( ContentModifier modifier ) {
        this.next = modifier;
        return modifier;
    }
    
    public void modify( InputStream in, OutputStream out ) throws RWException{
        InputStream modified = this.applyModification( in );
        if ( this.hasNext() ) {
            next.modify( modified, out );
        } else {
            try {
                IOUtility.copyFromTo( modified, out );
            } catch ( IOException ioex ) {
                throw Utility.newRWException( ioex );
            }
        }
    }
    
    /**
     * Apply this decorators decoration to the content
     * 
     * @param content
     * @return
     */
    protected abstract InputStream applyModification( InputStream in ) throws RWException;
    
}
