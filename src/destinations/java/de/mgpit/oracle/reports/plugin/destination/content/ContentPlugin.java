package de.mgpit.oracle.reports.plugin.destination.content;


import java.io.InputStream;

import oracle.reports.RWException;

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
public abstract class ContentPlugin {

    /**
     * Applies the modification to the input stream.
     * 
     * @param in
     *            InputStream to be modified
     * @return
     */
    protected abstract InputStream wrap( final InputStream in ) throws RWException;

}
