package de.mgpit.oracle.reports.plugin.destination;


import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import de.mgpit.oracle.reports.plugin.commons.Magic;
import de.mgpit.oracle.reports.plugin.commons.U;
import de.mgpit.oracle.reports.plugin.destination.content.types.Content;
import de.mgpit.oracle.reports.plugin.destination.content.types.InputModifier;
import de.mgpit.oracle.reports.plugin.destination.content.types.Modifier;
import de.mgpit.oracle.reports.plugin.destination.content.types.OutputModifier;
import de.mgpit.types.ContentAlias;
import de.mgpit.types.ModifierAlias;
import de.mgpit.types.ModifierRawDeclaration;
import oracle.reports.RWException;
import oracle.reports.server.Destination;
import oracle.reports.utility.Utility;

/**
 * A Destination which is able to modify the report's content on distribution.
 * <p>
 * This <em>abstract</em> class serves as super class for {@code oracle.reports.server.Destination}s which want to
 * modify the data of the report being distributed by the Reports Server.
 * <p style="color:FireBrick; font-size:110%">
 * <strong>Please read warnings at end!</strong>
 * <p>
 * Possible applications are
 * <ul>
 * <li>encoding the data of a report with outout format {@code PDF} as {@code BASE64}</li>
 * <li>putting the data into an <em>Envelope</em></li>
 * <li>prepending the data with a <em>Header</em></li>
 * <li>or combinations of such modifiers</li>
 * </ul>
 * The modifiers to apply are specified on report execution, see below. For referencing a modifier on
 * report execution they have to be given an <em>alias</em>, which is done in the {@code <reportservername>.conf} file
 * by specifying properties with <em>name</em> {@link Modifier#PROPERTY_NAME_PREFIX} .
 * <p>
 * Modifiers can be divided into <em>Decorators</em> and <em>Transformers</em>. A <em>Decorator</em> applies additional
 * content (like envelopes or headers), a <em>Transformer</em> changes the report's bytes.
 * <p>
 * For <em>Decorartor</em>s one can also specify <em>content provider</em>s which then can be used by the decorator for
 * generating the additional content. So one can choose between implementing a class for each kind of decoration or
 * implementing a generic decoration (a generic envelope, for example) and then provide the content separately.
 * <p>
 * Here is an example configuration excerpt for a {@code <reportservername>.conf} file on how to specify modifiers and
 * content providers.
 * 
 * <pre>
 * {@code
 *  <destination destype="..." class="...">
 *     ...
 *     <!-- Modifiers -->
 *     <property name="modifier.BASE64"      value="de.mgpit.oracle.reports.plugin.destination.content.transformers.Base64Transformer"/>
 *     <property name="modifier.Envelope"    value="de.mgpit.oracle.reports.plugin.destination.content.decorators.EnvelopeDecorator"/>
 *     <property name="modifier.Header"      value="de.mgpit.oracle.reports.plugin.destination.content.decorators.HeaderDecorator"/>
 *     <!-- Content Providers -->
 *     <property name="content.Soap_1_1"     value="tld.foo.bar.batz.SoapV0101Envelope"/> <!-- Must implement de.mgpit.oracle.reports.plugin.destination.content.types.Content -->
 *     <property name="content.Soap_1_2"     value="tld.foo.bar.batz.SoapV0102Envelope"/> <!-- Must implement de.mgpit.oracle.reports.plugin.destination.content.types.Content -->
 *  </destination>
 *         }
 * </pre>
 * 
 * <p>
 * <strong>Warnings</strong>
 * <p>
 * Due to the architecture of the Oracle&reg; Reports server the registry for {@code Modifier}s and {@code Content} is
 * implemented as <em>static</em> state as the reports server uses the static {@code init(Properties)} for initialization.
 * <p>
 * So if you have defined more than one {@code ModifyingDestination} inheriting from this class in your
 * {@code <reportservername>.conf} configuration file they will all share the same registry for
 * {@code Modifier}s and {@code Content}.
 * Destination properties for specifying modifiers and content with the same name will overwrite each other.
 * 
 * {@see MgpDestination}
 * 
 * @author mgp
 *
 */
public abstract class ModifyingDestination extends MgpDestination {

    private static final Logger LOG = Logger.getLogger( MgpDestination.class );

    /**
     * Holds the modifier plugins as defined in the {@code <reportservername>.conf} file
     */
    protected static final Map MODIFIER_REGISTRY = Collections.synchronizedMap( new HashMap( Magic.PRIME ) );

    /**
     * Holds the content providers as defined in the {@code <reportservername>.conf} file
     */
    protected static final Map CONTENTPROVIDER_REGISTRY = Collections.synchronizedMap( new HashMap( Magic.PRIME ) );

    public static void registerModifier( ModifierAlias key, Class value ) {
        MODIFIER_REGISTRY.put( key, value );
    }

    public static void registerContent( ContentAlias key, Class value ) {
        CONTENTPROVIDER_REGISTRY.put( key, value );
    }

    /**
     * Holds the name of the property used for passing a modifier chain to the current distribution.
     */
    private static final String CHAIN_DECLARATION_PROPERTY = "apply";

    /**
     * Gets the Logger for this destination.
     * 
     * @return logger
     */
    protected abstract Logger getLogger();

    /**
     * Holds the modifier chain which will be applied to the output
     * on the current distribution cycle.
     */
    private OutputModifier[] outputModifierChain = {};

    /**
     * Holds the modifier chain which will be applied to the input
     * on the current distribution cycle.
     */
    private InputModifier[] inputModifierChain = {};

    /**
     * Initializes the destination on Report Server startup.
     * <p>
     * Invoked by the Report Server.
     * <ul>
     * <li>initialize log4j</li>
     * <li>register declared content modifiers</li>
     * <li>register declared aliases</li>
     * </ul>
     * 
     * @param destinationsProperties
     *            the properties set in the report server's conf file within this
     *            destination's configuration section ({@code //destination/property})
     * @throws RWException
     */
    public static void init( Properties destinationsProperties ) throws RWException {
        MgpDestination.init( destinationsProperties );
        registerConfiguredModifiers( destinationsProperties );
        registerConfiguredContentProviders( destinationsProperties );
    }

    private static void registerConfiguredModifiers( Properties destinationsProperties ) throws RWException {
        Enumeration keys = destinationsProperties.keys();
        LOG.info( "About to search for and register virtual destinations ..." );
        boolean registrationErrorOccured = false;

        while ( keys.hasMoreElements() ) {
            String key = (String) keys.nextElement();
            if ( isModifierAliasDefinition( key ) ) {
                ModifierAlias name = extractModifierAlias( key );
                String fullClassName = destinationsProperties.getProperty( key );
                boolean success = registerModifier( name, fullClassName );
                if ( !success ) {
                    registrationErrorOccured = true;
                }
            }
        }
        if ( registrationErrorOccured ) {
            LOG.warn( "Could not register all Content Plugins!" );
        }
    }

    /**
     * Registers a {@code Modifier} under its alias.
     * <p>
     * Will only register those modifiers for which the declared class can be found.
     * 
     * @param name
     *            alias for registering
     * @param implementingClassName
     *            full class name of the {@code Modifier} class
     * @return {@code true} on success, {@code false} else.
     */
    private static boolean registerModifier( ModifierAlias name, String implementingClassName ) {
        LOG.info( " >>> About to register Modifier named " + U.w( name ) );
        Class clazz = null;
        try {
            clazz = Class.forName( implementingClassName );
            LOG.info( U.w( implementingClassName ) + " registered successfully for " + U.w( name ) );
        } catch ( ClassNotFoundException cnf ) {
            LOG.warn( cnf );
            return false;
        }
        ModifyingDestination.registerModifier( name, clazz );
        return true;
    }

    /**
     * Tests if the key defines a {@code Modifier} declaration.
     * 
     * @param key
     * @return {@code true} if the key defines a {@code Modifier} declaration, {@code false} else
     */
    private static boolean isModifierAliasDefinition( String key ) {
        return key.startsWith( Modifier.PROPERTY_NAME_PREFIX );
    }

    /**
     * Gets the modifier name part from the property name.
     * 
     * @param key
     *            property name
     * @return modifier name
     */
    private static ModifierAlias extractModifierAlias( String key ) {
        return ModifierAlias.of( extractAlias( key ) );
    }

    /**
     * Registers a {@code Content} under its alias.
     * <p>
     * Will only register those contents for which the declared class can be found.
     * 
     * @param name
     *            alias for registering
     * @param implementingClassName
     *            full class name of the {@code Content} class
     * @return {@code true} on success, {@code false} else.
     */
    private static void registerConfiguredContentProviders( Properties destinationsProperties ) throws RWException {
        Enumeration keys = destinationsProperties.keys();
        while ( keys.hasMoreElements() ) {
            String key = (String) keys.nextElement();
            if ( isContentAliasDefinition( key ) ) {
                ContentAlias name = extractContentAlias( key );
                String fullClassName = destinationsProperties.getProperty( key );
                boolean success = registerContent( name, fullClassName );
            }
        }
    }

    /**
     * Registers a {@code Content} under its alias.
     * <p>
     * Will only register those contents for which the declared class can be found.
     * 
     * @param name
     *            alias for registering
     * @param implementingClassName
     *            full class name of the {@code Content} class
     * @return {@code true} on success, {@code false} else.
     */
    private static boolean registerContent( ContentAlias name, String implementingClassName ) {
        LOG.info( " >>> About to register Modifier named " + U.w( name ) );
        Class clazz = null;
        try {
            clazz = Class.forName( implementingClassName );
            LOG.info( U.w( implementingClassName ) + " registered successfully for " + U.w( name ) );
        } catch ( ClassNotFoundException cnf ) {
            LOG.warn( cnf );
            return false;
        }
        ModifyingDestination.registerContent( name, clazz );
        return true;
    }

    /**
     * Tests if the key defines a {@code Content} declaration.
     * 
     * @param key
     * @return {@code true} if the key defines a {@code Modifier} declaration, {@code false} else
     */
    private static boolean isContentAliasDefinition( String key ) {
        return key.startsWith( Content.PROPERTY_NAME_PREFIX );
    }

    /**
     * Gets the content name part from the property name.
     * 
     * @param key
     *            property name
     * @return modifier name
     */
    private static ContentAlias extractContentAlias( String key ) {
        return ContentAlias.of( extractAlias( key ) );
    }

    /**
     * Extracts the alias from the key given.
     * <p>
     * The key is supposed to be of format {@code prefix.alias}. This means
     * the alias will be the last element in this dot seprated path.
     * 
     * @param key
     *            to extract the alias from
     * @return the alias part from the key
     */
    private static String extractAlias( String key ) {
        String[] pathElements = key.split( "\\." );
        int numberOfElements = pathElements.length;
        if ( numberOfElements < 2 ) {
            LOG.warn( "Got invalid name: " + U.w( key ) );
            return null;
        }
        int indexOfAlias = --numberOfElements;
        return pathElements[indexOfAlias];
    }

    protected InputStream getContent( File file ) throws RWException {
        InputStream sourceInput = super.getContent( file );
        return wrapWithInputModifiers( sourceInput );
    }

    /**
     * Wraps the input with {@code InputModifier}s.
     * 
     * @param initialStream
     * @return
     * @throws RWException
     */
    protected InputStream wrapWithInputModifiers( InputStream initialStream ) throws RWException {
        InputStream wrapped = initialStream;
        if ( this.inputModifierChain != null ) {
            final int endIndex = this.inputModifierChain.length;
            final Properties allProperties = getProperties();
            for ( int runIndex = 0; runIndex < endIndex; runIndex++ ) {
                InputModifier modifier = this.inputModifierChain[runIndex];
                wrapped = modifier.forInput( wrapped, allProperties );
                getLogger().info( "Modifier " + U.w( modifier.toString() ) + " has been applied successfully." );
            }
        }
        return wrapped;
    }

    /**
     * Wraps the output with {@code OutputModifier}s.
     * 
     * @param destinationStream
     * @return
     * @throws RWException
     */
    protected OutputStream wrapWithOutputModifiers( OutputStream destinationStream ) throws RWException {
        OutputStream wrapped = destinationStream;
        if ( this.outputModifierChain != null ) {
            final int startIndex = this.outputModifierChain.length - 1;
            final Properties allProperties = getProperties();
            for ( int runIndex = startIndex; runIndex >= 0; --runIndex ) {
                OutputModifier modifier = this.outputModifierChain[runIndex];
                wrapped = modifier.forOutput( wrapped, allProperties );
                getLogger().info( "Modifier for " + U.w( modifier.toString() ) + " has been applied successfully." );
            }
        }
        return wrapped;
    }

    /**
     * Starts a new distribution cycle for a report to this destination.
     * 
     * @param allProperties
     *            parameters for this distribution passed as {@code Properties}
     *            <br>
     *            <em>Remark:</em> For Oracle&reg; Forms this will include the parameters set via {@code SET_REPORT_OBJECT_PROPERTY}
     *            plus the parameters passed via a {@code ParamList}
     * @param targetName
     *            target name of the distribution
     * @param totalNumberOfFiles
     *            total number of files being distributed
     * @param totalFileSize
     *            total file size of all files being distributed
     * @param mainFormat
     *            the output format of the main file being distributed
     * 
     * @throws RWException
     *             if there is a failure during distribution setup. The RWException normally will wrap the original Exception.
     */
    protected boolean start( Properties allProperties, String targetName, int totalNumberOfFiles, long totalFileSize,
            short mainFormat ) throws RWException {

        try {
            boolean continueToSend = super.start( allProperties, targetName, totalNumberOfFiles, totalFileSize, mainFormat );

            if ( continueToSend ) {
                extractDeclaredModifierChain( allProperties );
            } else {
                getLogger().warn( "Cannot continue to send ..." );
            }
            return continueToSend;
        } catch ( RWException forLogging ) {
            getLogger().error( "Error during preparation of distribution!", forLogging );
            throw forLogging;
        }
    }

    /**
     * Looks for and extracts the declared modification chain from a distribution.
     * <p>
     * 
     * @param allProperties
     * @throws RWException
     */
    protected void extractDeclaredModifierChain( final Properties allProperties ) throws RWException {
        getLogger().info( "Looking for Modifier Chain declaration (Property \"" + CHAIN_DECLARATION_PROPERTY + "\" )..." );
        String modificationDeclaration = allProperties.getProperty( CHAIN_DECLARATION_PROPERTY );

        if ( modificationDeclaration != null ) {
            getLogger().info( "Declaration found. Extracting declared Modifier Chain" );
            ModifierRawDeclaration[] declaredModifiers = ModifierChainDeclaration.extractNames( modificationDeclaration );

            final int numberOfDeclarationsFound = declaredModifiers.length;
            getLogger().info( "Chain contains " + U.w( numberOfDeclarationsFound ) + " items." );
            List declaredOutputModifications = new ArrayList( numberOfDeclarationsFound );
            List declaredInputModifications = new ArrayList( numberOfDeclarationsFound );

            for ( int runIndex = 0; runIndex < numberOfDeclarationsFound; runIndex++ ) {
                getLogger().warn( "Iterating ..." );
                ModifierRawDeclaration unparsed = declaredModifiers[runIndex];
                getLogger().warn( "Unparsed is: " + unparsed.toString() );

                ModifierDeclaration parsed = null;
                try {
                    parsed = new ModifierDeclaration( unparsed );
                } catch ( Throwable thrown ) {
                    getLogger().fatal( "Deep shit happened!", thrown );
                }

                getLogger().debug( U.w( U.lpad( runIndex, 2 ) ) + ": Extracting " + U.w( parsed.toString() ) );
                if ( parsed.definesOutputModifier() ) {
                    getLogger().info( U.w( parsed ) + " identified as Output modifier." );
                    declaredOutputModifications.add( parsed );
                } else if ( parsed.definesInputModifier() ) {
                    getLogger().info( U.w( parsed ) + " identified as Input modifier." );
                    declaredInputModifications.add( parsed );
                } else {
                    getLogger().warn( U.w( parsed ) + " cannot be identified as Output nor Input modifier." );
                }
            }

            try {
                buildOutputModifierChain( declaredOutputModifications );
                buildInputModifierChain( declaredInputModifications );
            } catch ( Exception any ) {
                LOG.fatal( "Fatal error on instantiating extracted modifiers!", any );
                throw Utility.newRWException( any );
            }

        } else {
            getLogger().info( "No modifiers declared for this distribution." );
        }
    }

    /**
     * Instantiates the Output modifiers.
     * <p>
     * TODO: Code duplication with {@code buildInputModifierChain}
     * 
     * @param declarationsExtracted
     */
    private void buildOutputModifierChain( final List declarationsExtracted ) throws Exception {
        if ( declarationsExtracted != null && declarationsExtracted.size() > 0 ) {
            this.outputModifierChain = new OutputModifier[declarationsExtracted.size()];
            int targetIndex = 0;
            Iterator toInstantiate = declarationsExtracted.iterator();
            while ( toInstantiate.hasNext() ) {
                ModifierDeclaration declaration = (ModifierDeclaration) toInstantiate.next();
                // fail fast
                OutputModifier modifier = declaration.instantiateOutputModifier();
                this.outputModifierChain[targetIndex++] = modifier;
            }
        }
    }

    /**
     * Instantiates the Output modifiers.
     * <p>
     * TODO: Code duplication with {@code buildOutputModifierChain}
     * 
     * @param declarationsExtracted
     */
    private void buildInputModifierChain( final List declarationsExtracted ) throws Exception {
        if ( declarationsExtracted != null && declarationsExtracted.size() > 0 ) {
            this.outputModifierChain = new OutputModifier[declarationsExtracted.size()];
            int targetIndex = 0;
            Iterator toInstantiate = declarationsExtracted.iterator();
            while ( toInstantiate.hasNext() ) {
                ModifierDeclaration declaration = (ModifierDeclaration) toInstantiate.next();
                // fail fast
                InputModifier modifier = declaration.instantiateInputModifier();
                this.inputModifierChain[targetIndex++] = modifier;
            }
        }
    }

    /**
     * A Modifier Declaration.
     * 
     * @author mgp
     *
     */
    protected final class ModifierDeclaration {

        /**
         * Holds the pattern for parsing modifier declarations.
         */
        private final Pattern PARSE_PATTERN = Pattern.compile( ModifierRawDeclaration.PATTERN );

        /**
         * Holds the modifiers alias.
         */
        private ModifierAlias alias = null;
        /**
         * Holds the modifier class
         */
        private Class modifier = null;

        /**
         * Holds the alias of an optional content provider.
         */
        private ContentAlias contentAlias = null;
        /**
         * Holds the content class
         */
        private Class content = null;

        /**
         * Creates a new {@code ModifierDeclaration}.
         * 
         * @param unparsed
         *            a raw modifier declaration
         * 
         * @throws IllegalArgumentException
         *             on null or non parseable input
         */
        protected ModifierDeclaration( final ModifierRawDeclaration unparsed ) {
            U.assertNotNull( unparsed, "Cannot parse null name!" );
            getLogger().info( "Parsing...");
            Matcher test = PARSE_PATTERN.matcher( unparsed.toString() );
            if ( test.matches() ) {
                getLogger().info( "Matched");
                String modifierDefinition = test.group( 2 );
                String contentDefinition = test.group( 4 );
                getLogger().info(  U.w( modifierDefinition ) + U.w( contentDefinition ) );

                U.assertNotEmpty( modifierDefinition, "Cannot parse " + unparsed + "!" );
                this.alias = ModifierAlias.of( modifierDefinition );
                this.modifier = (Class) MODIFIER_REGISTRY.get( this.alias );
                if ( isModifierValid() ) {
                    if ( !U.isEmpty(  contentDefinition ) ) {
                        this.contentAlias = ContentAlias.of( contentDefinition );
                        if ( !this.contentAlias.isEmpty() ) {
                            this.content = (Class) CONTENTPROVIDER_REGISTRY.get( this.content );
                        }
                    }
                }
            } else {
                throw new IllegalArgumentException( "Cannot parse " + U.w( unparsed ) + " to a Modifier Declaration" );
            }
        }

        /**
         * Tests if the {@code Modifier} definition is valid.
         * <p>
         * A modifier definition is valid if both there is a non empty alias and the class
         * referenced by the alias is registered in the Modifier Registry.
         * 
         * @return {@code true} if the modifier definition is valid, {@code false} else
         */
        private boolean isModifierValid() {
            return !this.alias.isEmpty() && this.modifier != null;
        }

        /**
         * Tests if the {@code Content} definition is valid.
         * <p>
         * A content definition is valid if either the content alias is empty or
         * there is a non empty content alias and the class referenced by the content alias
         * is registered in the Content Registry.
         * 
         * @return {@code true} if the modifier definition is valid, {@code false} else
         */
        private boolean isContentValid() {
            return this.contentAlias.isEmpty() || !this.contentAlias.isEmpty() && this.content != null;
        }

        /**
         * Tests if the {@code ModifierDeclaration} is valid.
         * <p>
         * A {@code ModifierDeclaration} is valid if both the modifier definition and the content definition are valid.
         * 
         * @return {@code true} if the {@code ModifierDeclaration} is valid, {@code false} else
         */
        private boolean isValid() {
            return isModifierValid() && isContentValid();
        }

        protected boolean isParametrized() {
            return isValid() && !this.contentAlias.isEmpty();
        }

        protected boolean definesOutputModifier() {
            return isValid() && OutputModifier.class.isAssignableFrom( this.modifier );
        }

        protected boolean definesInputModifier() {
            return isValid() && InputModifier.class.isAssignableFrom( this.modifier );
        }

        protected final OutputModifier instantiateOutputModifier() throws Exception {
            U.assertTrue( isValid(), "Cannot instantiate with an invalid Modifier Definition!" );
            return (OutputModifier) instantiate(); // createModifierInstance( declaration, requesting );
        }

        protected final InputModifier instantiateInputModifier() throws Exception {
            U.assertTrue( isValid(), "Cannot instantiate with an invalid Modifier Definition!" );
            return (InputModifier) instantiate(); // createModifierInstance( declaration, requesting );
        }

        private Modifier instantiate() throws Exception {
            try {
                Modifier instance = (Modifier) modifier.newInstance();
                return instance;
            } catch ( InstantiationException cannotInstatiate ) {
                getLogger().fatal( "Cannot instantiate abstract/interface " + modifier.getName() + "!", cannotInstatiate );
                throw cannotInstatiate;
            } catch ( IllegalAccessException cannotAccess ) {
                getLogger().fatal( "Cannot instantiate due to access restrictions on " + modifier.getName() + "!", cannotAccess );
                throw cannotAccess;
            }
        }

        public String toString() {
            return (this.isValid()) ? this.alias + (this.isParametrized() ? "(" + this.contentAlias + ")" : "") : "<!!!Invalid!!!>";
        }

    }

}