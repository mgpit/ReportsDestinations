package de.mgpit.oracle.reports.plugin.destination.content.types;


import java.io.InputStream;
import java.util.Properties;

import de.mgpit.types.TypedString;
import oracle.reports.RWException;

/**
 * 
 * @author mgp
 * 
 *         A modifier for the content in context of an Oracle Reports&trade; distribution.
 *         <p>
 *         Typical applications may be
 *         <ul>
 *         <li>Filtering of the input stream</li>
 *         <li>Encoding the input stream</li>
 *         <li>or changing the encoding of the input stream</li>
 *         <li>Applying headers</li>
 *         <li>Applying envelopes</li>
 *         <li>...</li>
 *         </ul>
 *         i.e. tranformations or decorations of the report's resulting file.
 */
public interface Transformation {

    public static String PROPERTY_NAME_PREFIX = "transformer.";


    public String mimetype();

    public String fileExtension();

}