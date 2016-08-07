package de.mgpit.oracle.reports.plugin.destination.content;


import java.io.InputStream;

import org.apache.log4j.Logger;

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

    private static final Logger LOG = Logger.getLogger( ContentPlugin.class );

    /**
     * Applies the modification to the input stream.
     * 
     * @param in
     *            InputStream to be modified
     * @return
     */
    protected abstract InputStream wrap( final InputStream in ) throws RWException;

}
