package de.mgpit.oracle.reports.plugin.destination.content;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.log4j.Logger;

import de.mgpit.oracle.reports.plugin.commons.U;
import de.mgpit.oracle.reports.plugin.commons.io.IOUtility;
import oracle.reports.RWException;
import oracle.reports.utility.Utility;

/**
 * 
 * @author mgp
 * 
 *         Abstract superclass for content modifiers.
 *         Will modify the content of an InputStream given ...
 *         <p>
 *         Modification may be
 *         <ul>
 *         <li>Transformation, or</li>
 *         <li>Decoration</li>
 *         </ul>
 *
 */
public abstract class ContentModifier {

    private static final Logger LOG = Logger.getLogger( ContentModifier.class );

    private ContentModifier next;

    private boolean isLastInChain() {
        return next == null;
    }

    public ContentModifier followedBy( final ContentModifier modifier ) {
        this.next = modifier;
        return modifier;
    }

    /**
     * Modifies the InputStream. If the receiving ContentModifier is the last one in the chain than the result will be written to
     * the output stream else the input stream will be handed over to the next ContentModifier in the chain.
     * 
     * @param in
     *            InputStream to be modified. MUST NOT be null.
     * @param out
     *            OutputStream to write the results. MUST NOT be null.
     * @throws RWException
     *             if one of the arguments is null or any nested error occurs.
     */
    public final void modify( final InputStream in, final OutputStream out ) throws RWException {
        U.Rw.assertNotNull( in, "InputStream provided MUST NOT be null!" );
        U.Rw.assertNotNull( out, "OutputStream provided MUST NOT be null!" );

        LOG.debug( "Receiving " + in.getClass().getName() + " for modification " );
        InputStream modified = this.applyModification( in );
        if ( isLastInChain() ) {
            try {
                LOG.debug( "End of chain. Pushing to " + out.getClass().getName() );
                IOUtility.copyFromTo( modified, out );
                out.close();
            } catch ( IOException ioex ) {
                throw Utility.newRWException( ioex );
            }
        } else {
            LOG.debug( "Passing to next ContentModifier ..." );
            next.modify( modified, out );
        }
    }

    /**
     * Applies the modification to the input stream.
     * 
     * @param in
     *            InputStream to be modified
     * @return
     */
    protected abstract InputStream applyModification( final InputStream in ) throws RWException;

}
