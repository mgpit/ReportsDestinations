package de.mgpit.oracle.reports.plugin.destination.content;


import java.io.InputStream;

import oracle.reports.RWException;

/**
 * 
 * @author mgp
 * 
 * A modifier for the content in context of an Oracle Reports&trade; distribution.
 * <p>
 * Typical applications may be
 * <ul>
 *      <li>Filtering of the input stream</li>
 *      <li>Encoding the input stream</li>
 *      <li>or changing the encoding of the input stream</li>
 *      <li>Applying headers</li>
 *      <li>Applying envelopes</li>
 *      <li>...</li>
 * </ul>
 * i.e. tranformations or decorations of the report's resulting file.
 */
public interface ContentModificationPlugin {

    public static String PREFIX = "transformer.";
    /**
     * Wraps the input stream.
     * 
     * @param in
     *            InputStream to be wrapped.
     * @return InputStream wrapping the input stream given.
     */
    public InputStream wrap( final InputStream in ) throws RWException;
    
    public String mimetype();
    
    public String fileExtension();
    
    public static final class PluginName {
        public static PluginName of(String name){
            return new PluginName( name );
        }
        String name = "";
        private PluginName( String name) {
            this.name = name;
        }
        public String toString() {
            return name;
        }
        
        public String name() {
            return name;
        }

        public final boolean equals(Object other) {
            if (this==other) {
                return true;
            };
            if ( other.getClass() == this.getClass() ) {
                PluginName otherPluginName = (PluginName)other;
                if ( this.name == null && otherPluginName.name == null ) {
                    return true;
                }
                return this.name.equals( otherPluginName.name );
            } else {
                return false;
            }
        }

        public final int hashCode() {
            return (name==null)?0:name.hashCode();
        }
        
        public int compareTo( Object o ){
            return compareTo((PluginName)o);
        }
        
        public int compareTo( PluginName otherPluginName ){
            if ( this.name != null ){
                return this.name.compareTo(  otherPluginName.name );
            } else if ( otherPluginName != null ) {
                return otherPluginName.name.compareTo( this.name );
            } else {
                // must be null both ...
                return 0;
            }
        }
         
    }
}
