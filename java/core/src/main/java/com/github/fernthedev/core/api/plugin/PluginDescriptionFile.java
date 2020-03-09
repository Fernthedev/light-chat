package com.github.fernthedev.core.api.plugin;


import com.github.fernthedev.core.api.plugin.exception.InvalidDescriptionException;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import java.io.InputStream;
import java.util.*;

/**
 * This type is the runtime-container for the information in the com.github.fernthedev.client.plugin.yml.
 * All plugins must have a respective com.github.fernthedev.client.plugin.yml. For plugins written in java
 * using the standard com.github.fernthedev.client.plugin loader, this file must be in the root of the jar
 * file.
 * <p>
 * When Bukkit loads a com.github.fernthedev.client.plugin, it needs to know some basic information about
 * it. It reads this information from a YAML file, 'com.github.fernthedev.client.plugin.yml'. This file
 * consists of a set of attributes, each defined on a new line and with no
 * indentation.
 * <p>
 * Every (almost* every) method corresponds with a specific entry in the
 * com.github.fernthedev.client.plugin.yml. These are the <b>required</b> entries for every com.github.fernthedev.client.plugin.yml:
 * <ul>
 * <li>{@link #getName()} - <code>name</code>
 * <li>{@link #getVersion()} - <code>version</code>
 * <li>{@link #getMain()} - <code>main</code>
 * </ul>
 * <p>
 * Failing to include any of these items will throw an exception and cause the
 * server to ignore your com.github.fernthedev.client.plugin.
 * <p>
 * This is a list of the possible yaml keys, with specific details included in
 * the respective method documentations:
 * <!-- <table border=1>
 * <caption>The description of the com.github.fernthedev.client.plugin.yml layout</caption>
  * <tr>
 *     <th>Node</th>
 *     <th>Method</th>
 *     <th>Summary</th>
 * </tr><tr>
// *     `<code>name</code>
 *     {@link #getName()}
 *     The unique name of com.github.fernthedev.client.plugin
 * </tr><tr>
 *     <code>version</code>
 *     {@link #getVersion()}
 *     A com.github.fernthedev.client.plugin revision identifier
 * </tr><tr>
 *     <code>main</code>
 *     {@link #getMain()}
 *     The com.github.fernthedev.client.plugin's initial class file
 * </tr><tr>
 *     <code>author</code><br><code>authors</code>
 *     {@link #getAuthors()}
 *     The com.github.fernthedev.client.plugin contributors
 * </tr><tr>
 *     <code>description</code>
 *     {@link #getDescription()}
 *     Human readable com.github.fernthedev.client.plugin summary
 * </tr><tr>
 *     <code>website</code>
 *     {@link #getWebsite()}
 *     The URL to the com.github.fernthedev.client.plugin's site
 * </tr><tr>
 *     <code>prefix</code>
 *     {@link #getPrefix()}
 *     The token to prefix com.github.fernthedev.client.plugin log entries
 * </tr><tr>
 *     <code>load</code>
 *     {@link #getLoad()}
 *     The phase of server-startup this com.github.fernthedev.client.plugin will load during
 * </tr><tr>
 *     <code>depend</code>
 *     {@link #getDepend()}
 *     Other required plugins
 * </tr><tr>
 *     <code>softdepend</code>
 *     {@link #getSoftDepend()}
 *     Other plugins that add functionality
 * </tr><tr>
 *     <code>loadbefore</code>
 *     {@link #getLoadBefore()}
 *     The inverse softdepend
 * </tr><tr>
 *     <code>commands</code>
 *     {@link #getCommands()}
 *     The commands the com.github.fernthedev.client.plugin will register
 * </tr>
 *     <code>awareness</code>
 *     {@link #getAwareness()}
 *     The concepts that the com.github.fernthedev.client.plugin acknowledges
 * </tr>
 * </table>-->
 * <p>
 * A com.github.fernthedev.client.plugin.yml example:<blockquote><pre>
 *name: Inferno
 *version: 1.4.1
 *description: This com.github.fernthedev.client.plugin is so 31337. You can set yourself on fire.
 *# We could place every author in the authors list, but chose not to for illustrative purposes
 *# Also, having an author distinguishes that person as the project lead, and ensures their
 *# name is displayed first
 *author: CaptainInflamo
 *authors: [Cogito, verrier, EvilSeph]
 *website: http://www.curse.com/server-mods/minecraft/myplugin
 *
 *main: com.captaininflamo.bukkit.inferno.Inferno
 *depend: [NewFire, FlameWire]
 *
 *commands:
 *  flagrate:
 *    description: Set yourself on fire.
 *    aliases: [combust_me, combustMe]
 *    permission: inferno.flagrate
 *    usage: Syntax error! Simply type /&lt;command&gt; to ignite yourself.
 *  burningdeaths:
 *    description: List how many times you have died by fire.
 *    aliases: [burning_deaths, burningDeaths]
 *    permission: inferno.burningdeaths
 *    usage: |
 *      /&lt;command&gt; [player]
 *      Example: /&lt;command&gt; - see how many times you have burned to death
 *      Example: /&lt;command&gt; CaptainIce - see how many times CaptainIce has burned to death
 *
 *permissions:
 *  inferno.*:
 *    description: Gives access to all Inferno commands
 *    children:
 *      inferno.flagrate: true
 *      inferno.burningdeaths: true
 *      inferno.burningdeaths.others: true
 *  inferno.flagrate:
 *    description: Allows you to ignite yourself
 *    default: true
 *  inferno.burningdeaths:
 *    description: Allows you to see how many times you have burned to death
 *    default: true
 *  inferno.burningdeaths.others:
 *    description: Allows you to see how many times others have burned to death
 *    default: op
 *    children:
 *      inferno.burningdeaths: true
 *</pre></blockquote>
 */
public final class PluginDescriptionFile {
//    private static final ThreadLocal<Yaml> YAML = new ThreadLocal<Yaml>() {
//        @Override
//        protected Yaml initialValue() {
//            return new Yaml(new SafeConstructor() {
//                {
//                    yamlConstructors.put(null, new AbstractConstruct() {
//                        @Override
//                        public Object construct(final Node node) {
//                            if (!node.getTag().startsWith("!@")) {
//                                // Unknown tag - will fail
//                                return SafeConstructor.undefinedConstructor.construct(node);
//                            }
//                            // Unknown awareness - provide a graceful substitution
//                            return new PluginAwareness() {
//                                @Override
//                                public String toString() {
//                                    return node.toString();
//                                }
//                            };
//                        }
//                    });
//                    for (final PluginAwareness.Flags flag : PluginAwareness.Flags.values()) {
//                        yamlConstructors.put(new Tag("!@" + flag.name()), new AbstractConstruct() {
//                            @Override
//                            public PluginAwareness.Flags construct(final Node node) {
//                                return flag;
//                            }
//                        });
//                    }
//                }
//            });
//        }
//    };
    String rawName = null;
    private String name = null;
    private String main = null;
    private String classLoaderOf = null;
    private List<String> depend = ImmutableList.of();
    private List<String> softDepend = ImmutableList.of();
    private List<String> loadBefore = ImmutableList.of();
    private String version = null;
    private Map<String, Map<String, Object>> commands = null;
    private String description = null;
    private List<String> authors = null;
    private String website = null;
    private String prefix = null;
    private PluginLoadOrder order = PluginLoadOrder.POSTWORLD;
    private Map<?, ?> lazyPermissions = null;
    private Set<PluginAwareness> awareness = ImmutableSet.of();

//    public PluginDescriptionFile(final InputStream stream) throws InvalidDescriptionException {
//        loadMap(asMap(YAML.get().load(stream)));
//    }

    /**
     * Loads a PluginDescriptionFile from the specified reader
     *
     * @param reader The reader
     * @throws InvalidDescriptionException If the PluginDescriptionFile is
     *     invalid
     */
//    public PluginDescriptionFile(final Reader reader) throws InvalidDescriptionException {
//        loadMap(asMap(YAML.get().load(reader)));
//    }

    /**
     * Creates a new PluginDescriptionFile with the given detailed
     *
     * @param pluginName Name of this com.github.fernthedev.client.plugin
     * @param pluginVersion Version of this com.github.fernthedev.client.plugin
     * @param mainClass Full location of the main class of this com.github.fernthedev.client.plugin
     */
    public PluginDescriptionFile(final String pluginName, final String pluginVersion, final String mainClass) {
        name = pluginName.replace(' ', '_');
        version = pluginVersion;
        main = mainClass;
    }

    public PluginDescriptionFile(InputStream stream) {

    }

    /**
     * Gives the name of the com.github.fernthedev.client.plugin. This name is a unique identifier for
     * plugins.
     * <ul>
     * <li>Must consist of all alphanumeric characters, underscores, hyphon,
     *     and period (a-z,A-Z,0-9, _.-). Any other character will cause the
     *     com.github.fernthedev.client.plugin.yml to fail loading.
     * <li>Used to determine the name of the com.github.fernthedev.client.plugin's data folder. Data
     *     folders are placed in the ./plugins/ directory by default, but this
     *     behavior should not be relied on.
     *     should be used to reference the data folder.
     * <li>It is good practice to name your jar the same as this, for example
     *     'MyPlugin.jar'.
     * <li>Case sensitive.
     * <li>The is the token referenced in {@link #getDepend()}, {@link
     *     #getSoftDepend()}, and {@link #getLoadBefore()}.
     * <li>Using spaces in the com.github.fernthedev.client.plugin's name is deprecated.
     * </ul>
     * <p>
     * In the com.github.fernthedev.client.plugin.yml, this entry is named <code>name</code>.
     * <p>
     * Example:<blockquote><pre>name: MyPlugin</pre></blockquote>
     *
     * @return the name of the com.github.fernthedev.client.plugin
     */
    public String getName() {
        return name;
    }

    /**
     * Gives the version of the com.github.fernthedev.client.plugin.
     * <ul>
     * <li>Version is an arbitrary string, however the most common format is
     *     MajorRelease.MinorRelease.Build (eg: 1.4.1).
     * <li>Typically you will increment this every time you release a new
     *     feature or bug fix.
     * <li>Displayed when a user types <code>/version PluginName</code>
     * </ul>
     * <p>
     * In the com.github.fernthedev.client.plugin.yml, this entry is named <code>version</code>.
     * <p>
     * Example:<blockquote><pre>version: 1.4.1</pre></blockquote>
     *
     * @return the version of the com.github.fernthedev.client.plugin
     */
    public String getVersion() {
        return version;
    }

    /**
     * Gives the fully qualified name of the main class for a com.github.fernthedev.client.plugin. The
     * format should follow the {@link ClassLoader#loadClass(String)} syntax
     * to successfully be resolved at runtime. For most plugins, this is the
     * <ul>
     * <li>This must contain the full namespace including the class file
     *     itself.
     * <li>If your namespace is <code>org.bukkit.com.github.fernthedev.client.plugin</code>, and your class
     *     file is called <code>MyPlugin</code> then this must be
     *     <code>org.bukkit.com.github.fernthedev.client.plugin.MyPlugin</code>
     * <li>No com.github.fernthedev.client.plugin can use <code>org.bukkit.</code> as a base package for
     *     <b>any class</b>, including the main class.
     * </ul>
     * <p>
     * In the com.github.fernthedev.client.plugin.yml, this entry is named <code>main</code>.
     * <p>
     * Example:
     * <blockquote><pre>main: org.bukkit.com.github.fernthedev.client.plugin.MyPlugin</pre></blockquote>
     *
     * @return the fully qualified main class for the com.github.fernthedev.client.plugin
     */
    public String getMain() {
        return main;
    }

    /**
     * Gives a human-friendly description of the functionality the com.github.fernthedev.client.plugin
     * provides.
     * <ul>
     * <li>The description can have multiple lines.
     * <li>Displayed when a user types <code>/version PluginName</code>
     * </ul>
     * <p>
     * In the com.github.fernthedev.client.plugin.yml, this entry is named <code>description</code>.
     * <p>
     * Example:
     * <blockquote><pre>description: This com.github.fernthedev.client.plugin is so 31337. You can set yourself on fire.</pre></blockquote>
     *
     * @return description of this com.github.fernthedev.client.plugin, or null if not specified
     */
    public String getDescription() {
        return description;
    }

    /**
     * Gives the phase of server startup that the com.github.fernthedev.client.plugin should be loaded.
     * <ul>
     * <li>Possible values are in {@link PluginLoadOrder}.
     * <li>Defaults to {@link PluginLoadOrder#POSTWORLD}.
     * <li>Certain caveats apply to each phase.
     * <li>When different, {@link #getDepend()}, {@link #getSoftDepend()}, and
     *     {@link #getLoadBefore()} become relative in order loaded per-phase.
     *     If a com.github.fernthedev.client.plugin loads at <code>STARTUP</code>, but a dependency loads
     *     at <code>POSTWORLD</code>, the dependency will not be loaded before
     *     the com.github.fernthedev.client.plugin is loaded.
     * </ul>
     * <p>
     * In the com.github.fernthedev.client.plugin.yml, this entry is named <code>load</code>.
     * <p>
     * Example:<blockquote><pre>load: STARTUP</pre></blockquote>
     *
     * @return the phase when the com.github.fernthedev.client.plugin should be loaded
     */
    public PluginLoadOrder getLoad() {
        return order;
    }

    /**
     * Gives the list of authors for the com.github.fernthedev.client.plugin.
     * <ul>
     * <li>Gives credit to the developer.
     * <li>Used in some server error messages to provide helpful feedback on
     *     who to contact when an error occurs.
     * <li>A bukkit.org forum handle or email address is recommended.
     * <li>Is displayed when a user types <code>/version PluginName</code>
     * <li><code>authors</code> must be in <a
     *     href="http://en.wikipedia.org/wiki/YAML#Lists">YAML list
     *     format</a>.
     * </ul>
     * <p>
     * In the com.github.fernthedev.client.plugin.yml, this has two entries, <code>author</code> and
     * <code>authors</code>.
     * <p>
     * Single author example:
     * <blockquote><pre>author: CaptainInflamo</pre></blockquote>
     * Multiple author example:
     * <blockquote><pre>authors: [Cogito, verrier, EvilSeph]</pre></blockquote>
     * When both are specified, author will be the first entry in the list, so
     * this example:
     * <blockquote><pre>author: Grum
     *authors:
     *- feildmaster
     *- amaranth</pre></blockquote>
     * Is equivilant to this example:
     * <pre>authors: [Grum, feildmaster, aramanth]</pre>
     *
     * @return an immutable list of the com.github.fernthedev.client.plugin's authors
     */
    public List<String> getAuthors() {
        return authors;
    }

    /**
     * Gives the com.github.fernthedev.client.plugin's or com.github.fernthedev.client.plugin's author's website.
     * <ul>
     * <li>A link to the Curse page that includes documentation and downloads
     *     is highly recommended.
     * <li>Displayed when a user types <code>/version PluginName</code>
     * </ul>
     * <p>
     * In the com.github.fernthedev.client.plugin.yml, this entry is named <code>website</code>.
     * <p>
     * Example:
     * <blockquote><pre>website: http://www.curse.com/server-mods/minecraft/myplugin</pre></blockquote>
     *
     * @return description of this com.github.fernthedev.client.plugin, or null if not specified
     */
    public String getWebsite() {
        return website;
    }

    /**
     * Gives a list of other plugins that the com.github.fernthedev.client.plugin requires.
     * <ul>
     * <li>Use the value in the {@link #getName()} of the target com.github.fernthedev.client.plugin to
     *     specify the dependency.
     * <li>If any com.github.fernthedev.client.plugin listed here is not found, your com.github.fernthedev.client.plugin will fail to
     *     load at startup.
     * <li>If multiple plugins list each other in <code>depend</code>,
     *     creating a network with no individual com.github.fernthedev.client.plugin does not list another
     *     com.github.fernthedev.client.plugin in the <a
     *     href=https://en.wikipedia.org/wiki/Circular_dependency>network</a>,
     *     all plugins in that network will fail.
     * <li><code>depend</code> must be in must be in <a
     *     href="http://en.wikipedia.org/wiki/YAML#Lists">YAML list
     *     format</a>.
     * </ul>
     * <p>
     * In the com.github.fernthedev.client.plugin.yml, this entry is named <code>depend</code>.
     * <p>
     * Example:
     * <blockquote><pre>depend:
     *- OnePlugin
     *- AnotherPlugin</pre></blockquote>
     *
     * @return immutable list of the com.github.fernthedev.client.plugin's dependencies
     */
    public List<String> getDepend() {
        return depend;
    }

    /**
     * Gives a list of other plugins that the com.github.fernthedev.client.plugin requires for full
     * functionality. The {@link PluginManager} will make best effort to treat
     * all entries here as if they were a {@link #getDepend() dependency}, but
     * will never fail because of one of these entries.
     * <ul>
     * <li>Use the value in the {@link #getName()} of the target com.github.fernthedev.client.plugin to
     *     specify the dependency.
     * <li>When an unresolvable com.github.fernthedev.client.plugin is listed, it will be ignored and does
     *     not affect load order.
     * <li>When a circular dependency occurs (a network of plugins depending
     *     or soft-dependending each other), it will arbitrarily choose a
     *     com.github.fernthedev.client.plugin that can be resolved when ignoring soft-dependencies.
     * <li><code>softdepend</code> must be in <a
     *     href="http://en.wikipedia.org/wiki/YAML#Lists">YAML list
     *     format</a>.
     * </ul>
     * <p>
     * In the com.github.fernthedev.client.plugin.yml, this entry is named <code>softdepend</code>.
     * <p>
     * Example:
     * <blockquote><pre>softdepend: [OnePlugin, AnotherPlugin]</pre></blockquote>
     *
     * @return immutable list of the com.github.fernthedev.client.plugin's preferred dependencies
     */
    public List<String> getSoftDepend() {
        return softDepend;
    }

    /**
     * Gets the list of plugins that should consider this com.github.fernthedev.client.plugin a
     * soft-dependency.
     * <ul>
     * <li>Use the value in the {@link #getName()} of the target com.github.fernthedev.client.plugin to
     *     specify the dependency.
     * <li>The com.github.fernthedev.client.plugin should load before any other plugins listed here.
     * <li>Specifying another com.github.fernthedev.client.plugin here is strictly equivalent to having the
     *     specified com.github.fernthedev.client.plugin's {@link #getSoftDepend()} include {@link
     *     #getName() this com.github.fernthedev.client.plugin}.
     * <li><code>loadbefore</code> must be in <a
     *     href="http://en.wikipedia.org/wiki/YAML#Lists">YAML list
     *     format</a>.
     * </ul>
     * <p>
     * In the com.github.fernthedev.client.plugin.yml, this entry is named <code>loadbefore</code>.
     * <p>
     * Example:
     * <blockquote><pre>loadbefore:
     *- OnePlugin
     *- AnotherPlugin</pre></blockquote>
     *
     * @return immutable list of plugins that should consider this com.github.fernthedev.client.plugin a
     *     soft-dependency
     */
    public List<String> getLoadBefore() {
        return loadBefore;
    }

    /**
     * Gives the token to prefix com.github.fernthedev.client.plugin-specific logging messages with.
     * <ul>
     * <li>If not specified, the server uses the com.github.fernthedev.client.plugin's {@link #getName()
     *     name}.
     * <li>This should clearly indicate what com.github.fernthedev.client.plugin is being logged.
     * </ul>
     * <p>
     * In the com.github.fernthedev.client.plugin.yml, this entry is named <code>prefix</code>.
     * <p>
     * Example:<blockquote><pre>prefix: ex-why-zee</pre></blockquote>
     *
     * @return the prefixed logging token, or null if not specified
     */
    public String getPrefix() {
        return prefix;
    }

    /**
     * Gives the map of command-name to command-properties. Each entry in this
     * map corresponds to a single command and the respective values are the
     * properties of the command. Each property, <i>with the exception of
     * aliases</i>,
     * <table border=1>
     * <caption>The command section's description</caption>
     * <tr>
     *     <th>Node</th>
     *     <th>Method</th>
     *     <th>Type</th>
     *     <th>Description</th>
     *     <th>Example</th>
     * </tr>
     * </table>
     * The commands are structured as a hiearchy of <a
     * href="http://yaml.org/spec/current.html#id2502325">nested mappings</a>.
     * The primary (top-level, no intendentation) node is
     * `<code>commands</code>', while each individual command name is
     * indented, indicating it maps to some value (in our case, the
     * properties of the table above).
     * <p>
     * Here is an example bringing together the piecemeal examples above, as
     * well as few more definitions:<blockquote><pre>
     *commands:
     *  flagrate:
     *    description: Set yourself on fire.
     *    aliases: [combust_me, combustMe]
     *    permission: inferno.flagrate
     *    permission-message: You do not have /&lt;permission&gt;
     *    usage: Syntax error! Perhaps you meant /&lt;command&gt; PlayerName?
     *  burningdeaths:
     *    description: List how many times you have died by fire.
     *    aliases:
     *    - burning_deaths
     *    - burningDeaths
     *    permission: inferno.burningdeaths
     *    usage: |
     *      /&lt;command&gt; [player]
     *      Example: /&lt;command&gt; - see how many times you have burned to death
     *      Example: /&lt;command&gt; CaptainIce - see how many times CaptainIce has burned to death
     *  # The next command has no description, aliases, etc. defined, but is still valid
     *  # Having an empty declaration is useful for defining the description, permission, and messages from a configuration dynamically
     *  apocalypse:
     *</pre></blockquote>
     * Note: Command names may not have a colon in their name.
     *
     * @return the commands this com.github.fernthedev.client.plugin will register
     */
    public Map<String, Map<String, Object>> getCommands() {
        return commands;
    }

    /**
     * Gives a set of every {@link PluginAwareness} for a com.github.fernthedev.client.plugin. An awareness
     * dictates something that a com.github.fernthedev.client.plugin developer acknowledges when the com.github.fernthedev.client.plugin
     * is compiled. Some implementions may define extra awarenesses that are
     * not included in the API. Any unrecognized
     * awareness (one unsupported or in a future version) will cause a dummy
     * object to be created instead of failing.
     *
     * <ul>
     * <li>Currently only supports the enumerated values in {@link
     *     PluginAwareness.Flags}.
     * <li>Each awareness starts the identifier with bang-at
     *     (<code>!@</code>).
     * <li>Unrecognized (future / unimplemented) entries are quietly replaced
     *     by a generic object that implements PluginAwareness.
     * <li>A type of awareness must be defined by the runtime and acknowledged
     *     by the API, effectively discluding any derived type from any
     *     com.github.fernthedev.client.plugin's classpath.
     * <li><code>awareness</code> must be in <a
     *     href="http://en.wikipedia.org/wiki/YAML#Lists">YAML list
     *     format</a>.
     * </ul>
     * <p>
     * In the com.github.fernthedev.client.plugin.yml, this entry is named <code>awareness</code>.
     * <p>
     * Example:<blockquote><pre>awareness:
     *- !@UTF8</pre></blockquote>
     * <p>
     * <b>Note:</b> Although unknown versions of some future awareness are
     * gracefully substituted, previous versions of Bukkit (ones prior to the
     * first implementation of awareness) will fail to load a com.github.fernthedev.client.plugin that
     * defines any awareness.
     *
     * @return a set containing every awareness for the com.github.fernthedev.client.plugin
     */
    public Set<PluginAwareness> getAwareness() {
        return awareness;
    }

    /**
     * Returns the name of a com.github.fernthedev.client.plugin, including the version. This method is
     * provided for convenience; it uses the {@link #getName()} and {@link
     * #getVersion()} entries.
     *
     * @return a descriptive name of the com.github.fernthedev.client.plugin and respective version
     */
    public String getFullName() {
        return name + " v" + version;
    }

    /**
     * @return unused
     * @deprecated unused
     */
    @Deprecated
    public String getClassLoaderOf() {
        return classLoaderOf;
    }

//    /**
//     * Saves this PluginDescriptionFile to the given writer
//     *
//     * @param writer Writer to output this file to
//     */
//    public void save(Writer writer) {
//        YAML.get().dump(saveMap(), writer);
//    }

    private void loadMap(Map<?, ?> map) throws InvalidDescriptionException {
        try {
            name = rawName = map.get("name").toString();

            if (!name.matches("^[A-Za-z0-9 _.-]+$")) {
                throw new InvalidDescriptionException("name '" + name + "' contains invalid characters.");
            }
            name = name.replace(' ', '_');
        } catch (NullPointerException ex) {
            throw new InvalidDescriptionException(ex, "name is not defined");
        } catch (ClassCastException ex) {
            throw new InvalidDescriptionException(ex, "name is of wrong type");
        }

        try {
            version = map.get("version").toString();
        } catch (NullPointerException ex) {
            throw new InvalidDescriptionException(ex, "version is not defined");
        } catch (ClassCastException ex) {
            throw new InvalidDescriptionException(ex, "version is of wrong type");
        }

        try {
            main = map.get("main").toString();
            if (main.startsWith("org.bukkit.")) {
                throw new InvalidDescriptionException("main may not be within the org.bukkit namespace");
            }
        } catch (NullPointerException ex) {
            throw new InvalidDescriptionException(ex, "main is not defined");
        } catch (ClassCastException ex) {
            throw new InvalidDescriptionException(ex, "main is of wrong type");
        }

        if (map.get("commands") != null) {
            ImmutableMap.Builder<String, Map<String, Object>> commandsBuilder = ImmutableMap.<String, Map<String, Object>>builder();
            try {
                for (Map.Entry<?, ?> command : ((Map<?, ?>) map.get("commands")).entrySet()) {
                    ImmutableMap.Builder<String, Object> commandBuilder = ImmutableMap.<String, Object>builder();
                    if (command.getValue() != null) {
                        for (Map.Entry<?, ?> commandEntry : ((Map<?, ?>) command.getValue()).entrySet()) {
                            if (commandEntry.getValue() instanceof Iterable) {
                                // This prevents internal alias list changes
                                ImmutableList.Builder<Object> commandSubList = ImmutableList.<Object>builder();
                                for (Object commandSubListItem : (Iterable<?>) commandEntry.getValue()) {
                                    if (commandSubListItem != null) {
                                        commandSubList.add(commandSubListItem);
                                    }
                                }
                                commandBuilder.put(commandEntry.getKey().toString(), commandSubList.build());
                            } else if (commandEntry.getValue() != null) {
                                commandBuilder.put(commandEntry.getKey().toString(), commandEntry.getValue());
                            }
                        }
                    }
                    commandsBuilder.put(command.getKey().toString(), commandBuilder.build());
                }
            } catch (ClassCastException ex) {
                throw new InvalidDescriptionException(ex, "commands are of wrong type");
            }
            commands = commandsBuilder.build();
        }

        if (map.get("class-loader-of") != null) {
            classLoaderOf = map.get("class-loader-of").toString();
        }

        depend = makePluginNameList(map, "depend");
        softDepend = makePluginNameList(map, "softdepend");
        loadBefore = makePluginNameList(map, "loadbefore");

        if (map.get("website") != null) {
            website = map.get("website").toString();
        }

        if (map.get("description") != null) {
            description = map.get("description").toString();
        }

        if (map.get("load") != null) {
            try {
                order = PluginLoadOrder.valueOf(((String) map.get("load")).toUpperCase(Locale.ENGLISH).replaceAll("\\W", ""));
            } catch (ClassCastException ex) {
                throw new InvalidDescriptionException(ex, "load is of wrong type");
            } catch (IllegalArgumentException ex) {
                throw new InvalidDescriptionException(ex, "load is not a valid choice");
            }
        }

        if (map.get("authors") != null) {
            ImmutableList.Builder<String> authorsBuilder = ImmutableList.<String>builder();
            if (map.get("author") != null) {
                authorsBuilder.add(map.get("author").toString());
            }
            try {
                for (Object o : (Iterable<?>) map.get("authors")) {
                    authorsBuilder.add(o.toString());
                }
            } catch (ClassCastException ex) {
                throw new InvalidDescriptionException(ex, "authors are of wrong type");
            } catch (NullPointerException ex) {
                throw new InvalidDescriptionException(ex, "authors are improperly defined");
            }
            authors = authorsBuilder.build();
        } else if (map.get("author") != null) {
            authors = ImmutableList.of(map.get("author").toString());
        } else {
            authors = ImmutableList.<String>of();
        }

        if (map.get("awareness") instanceof Iterable) {
            Set<PluginAwareness> awareness = new HashSet<PluginAwareness>();
            try {
                for (Object o : (Iterable<?>) map.get("awareness")) {
                    awareness.add((PluginAwareness) o);
                }
            } catch (ClassCastException ex) {
                throw new InvalidDescriptionException(ex, "awareness has wrong type");
            }
            this.awareness = ImmutableSet.copyOf(awareness);
        }

        try {
            lazyPermissions = (Map<?, ?>) map.get("permissions");
        } catch (ClassCastException ex) {
            throw new InvalidDescriptionException(ex, "permissions are of the wrong type");
        }

        if (map.get("prefix") != null) {
            prefix = map.get("prefix").toString();
        }
    }

    private static List<String> makePluginNameList(final Map<?, ?> map, final String key) throws InvalidDescriptionException {
        final Object value = map.get(key);
        if (value == null) {
            return ImmutableList.of();
        }

        final ImmutableList.Builder<String> builder = ImmutableList.<String>builder();
        try {
            for (final Object entry : (Iterable<?>) value) {
                builder.add(entry.toString().replace(' ', '_'));
            }
        } catch (ClassCastException ex) {
            throw new InvalidDescriptionException(ex, key + " is of wrong type");
        } catch (NullPointerException ex) {
            throw new InvalidDescriptionException(ex, "invalid " + key + " format");
        }
        return builder.build();
    }

    private Map<String, Object> saveMap() {
        Map<String, Object> map = new HashMap<>();

        map.put("name", name);
        map.put("main", main);
        map.put("version", version);
        map.put("order", order.toString());

        if (commands != null) {
            map.put("command", commands);
        }
        if (depend != null) {
            map.put("depend", depend);
        }
        if (softDepend != null) {
            map.put("softdepend", softDepend);
        }
        if (website != null) {
            map.put("website", website);
        }
        if (description != null) {
            map.put("description", description);
        }

        if (authors.size() == 1) {
            map.put("author", authors.get(0));
        } else if (authors.size() > 1) {
            map.put("authors", authors);
        }

        if (classLoaderOf != null) {
            map.put("class-loader-of", classLoaderOf);
        }

        if (prefix != null) {
            map.put("prefix", prefix);
        }

        return map;
    }

    private Map<?, ?> asMap(Object object) throws InvalidDescriptionException {
        if (object instanceof Map) {
            return (Map<?, ?>) object;
        }
        throw new InvalidDescriptionException(object + " is not properly structured.");
    }

    /**
     * @return internal use
     * @deprecated Internal use
     */
    @Deprecated
    public String getRawName() {
        return rawName;
    }
}

