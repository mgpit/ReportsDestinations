package de.mgpit.oracle.reports.plugin.destination.content;


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
public interface ContentModificationPlugin {

    public static String PREFIX = "transformer.";

    /**
     * Wraps the input stream.
     * 
     * @param in
     *            InputStream to be wrapped.
     * @param allProperties
     *            Parameters for the plugin.
     * @return InputStream wrapping the input stream given.
     */
    public InputStream wrap( final InputStream in, final Properties parameters ) throws RWException;

    public String mimetype();

    public String fileExtension();

    public static final class PluginName extends TypedString {
        public static PluginName of( String name ) {
            return new PluginName( name );
        }

        String name = "";

        private PluginName( String name ) {
            this.name = name;
        }

        protected String value() {
            return this.name;
        }

    }
}
