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
import de.mgpit.oracle.reports.plugin.destination.content.types.WithModel;
import de.mgpit.types.ContentAlias;
import de.mgpit.types.ModifierAlias;
import de.mgpit.types.ModifierRawDeclaration;
import oracle.reports.RWException;
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
     * Gets the content to be distributed as an {@code InputStream}.
     * <p>
     * If {@code InputModifier}s are specified the the file's input stream will be
     * wrapped with those {@code InputModifier}s.
     * 
     * @param file
     *            File to be distributed
     * @return InputStream on the content to be distributed.
     * 
     */
    protected InputStream getContent( File file ) throws RWException {
        InputStream sourceInput = super.getContent( file );
        return wrapWithInputModifiers( sourceInput );
    }

    /**
     * Wraps the input with {@code InputModifier}s.
     * <p>
     * 
     * @param initialStream
     *            the {@code InputStream} to be wrapped.
     * @return the {@initialStream} or {@initialStream} wrapped with {@code InputModifier}s
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
     * 
     * @return
     * @throws RWException
     */
    protected OutputStream getTarget() throws RWException {
        try {
            OutputStream targetOut = getTargetOut();
            return wrapWithOutputModifiers( targetOut );
        } catch ( Exception any ) {
            throw asRWException( any );
        }
    }

    /**
     * Gets an @{code OutputStream} on the distribution target.
     * 
     * @return OutputStream on the distribution target.
     * @throws RWException
     * @throws Exception
     */
    protected abstract OutputStream getTargetOut() throws RWException, Exception;

    /**
     * Wraps the output with {@code OutputModifier}s.
     * <p>
     * 
     * @param initialStream
     *            the {@code OutputStream} to be wrapped.
     * @return the {@initialStream} or {@initialStream} wrapped with {@code OutputModifier}s
     * @throws RWException
     */
    protected OutputStream wrapWithOutputModifiers( OutputStream targetStream ) throws RWException {
        OutputStream wrapped = targetStream;
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
                extractDeclaredModifierChainFrom( allProperties );
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
    protected void extractDeclaredModifierChainFrom( final Properties allProperties ) throws RWException {
        getLogger().info( "Looking for Modifier Chain declaration (Property \"" + CHAIN_DECLARATION_PROPERTY + "\" )..." );
        String modificationDeclaration = allProperties.getProperty( CHAIN_DECLARATION_PROPERTY );

        boolean declarationsOK = true;

        if ( modificationDeclaration != null ) {
            getLogger().info( "Declaration found. Extracting declared Modifier Chain" );
            ModifierRawDeclaration[] declaredModifiers = ModifierChainDeclaration.extractNames( modificationDeclaration );

            final int numberOfDeclarationsFound = declaredModifiers.length;
            getLogger().info( "Chain contains " + U.w( numberOfDeclarationsFound ) + " items." );
            List declaredOutputModifications = new ArrayList( numberOfDeclarationsFound );
            List declaredInputModifications = new ArrayList( numberOfDeclarationsFound );

            for ( int runIndex = 0; runIndex < numberOfDeclarationsFound; runIndex++ ) {
                ModifierRawDeclaration unparsed = declaredModifiers[runIndex];
                ModifierDeclaration parsed = new ModifierDeclaration( unparsed );

                getLogger().debug( U.w( U.lpad( runIndex, 2 ) ) + ": Extracting " + U.w( parsed.toString() ) );
                if ( parsed.definesOutputModifier() ) {
                    getLogger().debug( U.w( parsed ) + " identified as Output modifier." );
                    declaredOutputModifications.add( parsed );
                } else if ( parsed.definesInputModifier() ) {
                    getLogger().debug( U.w( parsed ) + " identified as Input modifier." );
                    declaredInputModifications.add( parsed );
                } else {
                    getLogger().warn( U.w( parsed + " built from " + U.w( unparsed ) )
                            + " cannot be identified as Output nor as Input modifier." );
                    declarationsOK = false;
                }
            }

            if ( declarationsOK ) {
                try {
                    buildOutputModifierChainFrom( declaredOutputModifications );
                    buildInputModifierChainFrom( declaredInputModifications );
                } catch ( Exception any ) {
                    LOG.fatal( "Fatal error on instantiating extracted modifiers for current distribution!", any );
                    throw asRWException( any );
                }
            } else {
                String message = "Found invalid modifier chain declaraton for current distribution!";
                LOG.fatal( message );
                throw asRWException( new IllegalArgumentException( message ) );
            }

        } else {
            getLogger().info( "No modifiers declared for current distribution." );
        }
    }

    /**
     * Instantiates the Output modifiers.
     * <p>
     * TODO: Code duplication with {@code buildInputModifierChain}
     * 
     * @param declarationsExtracted
     */
    private void buildOutputModifierChainFrom( final List declarationsExtracted ) throws Exception {
        final boolean hasDeclarations = declarationsExtracted != null && declarationsExtracted.size() > 0;
        ;
        if ( hasDeclarations ) {
            this.outputModifierChain = new OutputModifier[declarationsExtracted.size()];
            int targetIndex = 0;
            Iterator toInstantiate = declarationsExtracted.iterator();
            while ( toInstantiate.hasNext() ) {
                ModifierDeclaration declaration = (ModifierDeclaration) toInstantiate.next();
                // fail fast
                OutputModifier modifier = declaration.instantiateAsOutputModifier();
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
    private void buildInputModifierChainFrom( final List declarationsExtracted ) throws Exception {
        final boolean hasDeclarations = declarationsExtracted != null && declarationsExtracted.size() > 0;
        if ( hasDeclarations ) {
            this.outputModifierChain = new OutputModifier[declarationsExtracted.size()];
            int targetIndex = 0;
            Iterator toInstantiate = declarationsExtracted.iterator();
            while ( toInstantiate.hasNext() ) {
                ModifierDeclaration declaration = (ModifierDeclaration) toInstantiate.next();
                // fail fast
                InputModifier modifier = declaration.instantiateAsInputModifier();
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
        private final Pattern PARSE_PATTERN;

        /**
         * Holds the modifiers alias.
         */
        private ModifierAlias alias = ModifierAlias.EMPTY_VALUE;
        /**
         * Holds the modifier class
         */
        private Class modifier = null;

        /**
         * Holds the alias of an optional content provider.
         */
        private ContentAlias contentAlias = ContentAlias.EMPTY_VALUE;
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
            PARSE_PATTERN = Pattern.compile( ModifierRawDeclaration.PATTERN );
            Matcher test = PARSE_PATTERN.matcher( unparsed.toString() );

            if ( test.matches() ) {
                String modifierDefinition = test.group( 2 );
                String contentDefinition = test.group( 4 );
                getLogger().info( U.w( modifierDefinition ) + U.w( contentDefinition ) );

                U.assertNotEmpty( modifierDefinition, "Cannot parse " + unparsed + "!" );
                this.alias = ModifierAlias.of( modifierDefinition );
                this.modifier = (Class) DestinationRegistrar.MODIFIER_REGISTRY.get( this.alias );
                if ( isModifierValid() ) {
                    if ( !U.isEmpty( contentDefinition ) ) {
                        this.contentAlias = ContentAlias.of( contentDefinition );
                        if ( this.contentAlias.isNotEmpty() ) {
                            this.content = (Class) DestinationRegistrar.CONTENTPROVIDER_REGISTRY.get( this.contentAlias );
                        }

                        if ( !isContentValid() ) {
                            getLogger().error( "Content Declaration is NOT valid!" );
                        }
                    }
                } else {
                    getLogger().error( "Modifier Declaration is NOT valid!" );
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
            return this.alias.isNotEmpty() && this.modifier != null;
        }

        /**
         * Tests if the {@code Content} definition is valid.
         * <p>
         * A content definition is valid if either the content alias is empty or
         * there is a non empty content alias and the class referenced by the content alias
         * is registered in the Content Registry.
         * <p>
         * A {@code null} content definition is considered valid, too.
         * 
         * @return {@code true} if the modifier definition is valid, {@code false} else
         */
        private boolean isContentValid() {
            return this.contentAlias.isEmpty() || (this.contentAlias.isNotEmpty() && this.content != null);
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
            return isValid() && this.contentAlias.isNotEmpty();
        }

        protected boolean definesOutputModifier() {
            return isValid() && OutputModifier.class.isAssignableFrom( this.modifier );
        }

        protected boolean definesInputModifier() {
            return isValid() && InputModifier.class.isAssignableFrom( this.modifier );
        }

        protected final OutputModifier instantiateAsOutputModifier() throws Exception {
            U.assertTrue( isValid(), "Cannot instantiate with an invalid Modifier Definition!" );
            return (OutputModifier) instantiate();
        }

        protected final InputModifier instantiateAsInputModifier() throws Exception {
            U.assertTrue( isValid(), "Cannot instantiate with an invalid Modifier Definition!" );
            return (InputModifier) instantiate();
        }

        private Modifier instantiate() throws Exception {
            try {
                Modifier modifierInstance = (Modifier) modifier.newInstance();
                if ( this.isaWithModel() ) {
                    if ( isParametrized() ) {
                        Content contentInstance = (Content) content.newInstance();
                        ((WithModel) modifierInstance).setContentModel( contentInstance );
                    }
                    // TODO: Should a modifier implementing WithModel require a Content?
                }
                return modifierInstance;
            } catch ( InstantiationException cannotInstatiate ) {
                getLogger().fatal( "Cannot instantiate abstract/interface " + modifier.getName() + "!", cannotInstatiate );
                throw cannotInstatiate;
            } catch ( IllegalAccessException cannotAccess ) {
                getLogger().fatal( "Cannot instantiate due to access restrictions on " + modifier.getName() + "!", cannotAccess );
                throw cannotAccess;
            }
        }

        private boolean isaWithModel() {
            return WithModel.class.isAssignableFrom( modifier );
        }

        /**
         * Returns a string representation of this object.
         * <p>
         * Output will be of form
         * <ul>
         * <li>{@code Alias} for normal modifiers</li>
         * <li>{@code Alias(Content)} for parametrized modifiers</li>
         * <li>{@code <!!!Invalid!!!>} for non parseable modifiers or modifiers, which references point
         * to non existing modifier or content aliases</li>
         * </ul>
         * 
         * @returns a string representation of this object.
         */
        public String toString() {
            String stringRepresentation = "";
            if ( this.isValid() ) {
                stringRepresentation = this.alias + (this.isParametrized() ? "(" + this.contentAlias + ")" : "");
            } else {
                if ( isModifierValid() ) {
                    stringRepresentation = this.alias + "(<!!!Invalid(" + this.contentAlias + ")!!!>)";
                } else {
                    stringRepresentation = "<!!!Invalid!!!>";
                }
            }
            return stringRepresentation;
        }

    }

    /**
     * Initializes the destination on Report Server startup.
     * <p>
     * Invoked by the Report Server.
     * <ul>
     * <li>initialize log4j</li>
     * <li>register declared content modifiers under an alias</li>
     * <li>register declared content providers under an alias</li>
     * </ul>
     * 
     * @param destinationsProperties
     *            the properties set in the report server's conf file within this
     *            destination's configuration section ({@code //destination/property})
     * @throws RWException
     */
    public static void init( Properties destinationsProperties ) throws RWException {
        MgpDestination.init( destinationsProperties );
        DestinationRegistrar.registerConfiguredModifiersFrom( destinationsProperties );
        DestinationRegistrar.registerConfiguredContentProvidersFrom( destinationsProperties );
        DestinationRegistrar.listAll();
    }

    /**
     * Registrar for {@code Modifier}s and {@code Content}s.
     * 
     * @author mgp
     *
     */
    private static final class DestinationRegistrar {
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

        private static void registerConfiguredModifiersFrom( Properties destinationsProperties ) throws RWException {
            Enumeration keys = destinationsProperties.keys();
            LOG.info( "About to search for and register declared Modifiers ..." );
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
                LOG.warn( "Could not register all Modifiers!" );
                // TODO: Consider throwing IllegalArgumentException
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
         * @return {@code true} on if the class has been registered as Modifier under the alias given, {@code false} else.
         */
        private static boolean registerModifier( ModifierAlias name, String implementingClassName ) {
            LOG.info( " >>> About to register Modifier named " + U.w( name ) );
            Class clazz = null;
            try {
                clazz = Class.forName( implementingClassName );
                if ( !Modifier.class.isAssignableFrom( clazz ) ) {
                    LOG.error( implementingClassName + " is not a Modifier!" );
                    return false;
                }
                LOG.info( U.w( implementingClassName ) + " registered successfully for " + U.w( name ) );
            } catch ( ClassNotFoundException cnf ) {
                LOG.error( cnf );
                return false;
            }
            registerModifier( name, clazz );
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
        private static void registerConfiguredContentProvidersFrom( Properties destinationsProperties ) throws RWException {
            Enumeration keys = destinationsProperties.keys();
            LOG.info( "About to search for and register declared Content providers ..." );
            boolean registrationErrorOccured = false;

            while ( keys.hasMoreElements() ) {
                String key = (String) keys.nextElement();
                if ( isContentAliasDefinition( key ) ) {
                    ContentAlias name = extractContentAlias( key );
                    String fullClassName = destinationsProperties.getProperty( key );
                    boolean success = registerContent( name, fullClassName );
                    if ( !success ) {
                        registrationErrorOccured = true;
                    }
                }
            }
            if ( registrationErrorOccured ) {
                LOG.warn( "Could not register all Content providers!" );
                // TODO: Consider throwing IllegalArgumentException
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
         * @return {@code true} on if the class has been registered as Content under the alias given, {@code false} else.
         */
        private static boolean registerContent( ContentAlias name, String implementingClassName ) {
            LOG.info( " >>> About to register Content named " + U.w( name ) );
            Class implementingClass = null;
            try {
                implementingClass = Class.forName( implementingClassName );
                if ( !Content.class.isAssignableFrom( implementingClass ) ) {
                    LOG.error( implementingClassName + " is not a Content!" );
                    return false;
                }
                LOG.info( U.w( implementingClassName ) + " registered successfully under alias " + U.w( name ) );
            } catch ( ClassNotFoundException cnf ) {
                LOG.error( cnf );
                return false;
            }
            registerContent( name, implementingClass );
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

        protected static void listAll() {
            final Logger LOGGA = Logger.getRootLogger();
            LOGGA.info( "Listing Modifier Registry ..." );
            list( MODIFIER_REGISTRY );
            LOGGA.info( "Listing Content Registry ..." );
            list( CONTENTPROVIDER_REGISTRY );
        }

        protected static void list( Map m ) {
            final Logger LOGGA = Logger.getRootLogger();
            Iterator iter = m.keySet().iterator();
            while ( iter.hasNext() ) {
                Object k = iter.next();
                Object v = m.get( k );
                LOGGA.info( U.w( k ) + " -> " + U.w( (U.coalesce( v, "<null>" )) ) );
            }
        }

    }
}