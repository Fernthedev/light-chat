package com.github.fernthedev.lightchat.core.api.plugin

import com.github.fernthedev.lightchat.core.api.plugin.exception.InvalidDescriptionException
import com.google.common.collect.ImmutableList
import com.google.common.collect.ImmutableMap
import com.google.common.collect.ImmutableSet
import java.io.InputStream

/**
 * This type is the runtime-container for the information in the com.github.fernthedev.client.plugin.yml.
 * All plugins must have a respective com.github.fernthedev.client.plugin.yml. For plugins written in java
 * using the standard com.github.fernthedev.client.plugin loader, this file must be in the root of the jar
 * file.
 *
 *
 * When Bukkit loads a com.github.fernthedev.client.plugin, it needs to know some basic information about
 * it. It reads this information from a YAML file, 'com.github.fernthedev.client.plugin.yml'. This file
 * consists of a set of attributes, each defined on a new line and with no
 * indentation.
 *
 *
 * Every (almost* every) method corresponds with a specific entry in the
 * com.github.fernthedev.client.plugin.yml. These are the **required** entries for every com.github.fernthedev.client.plugin.yml:
 *
 *  * [.getName] - `name`
 *  * [.getVersion] - `version`
 *  * [.getMain] - `main`
 *
 *
 *
 * Failing to include any of these items will throw an exception and cause the
 * server to ignore your com.github.fernthedev.client.plugin.
 *
 *
 * This is a list of the possible yaml keys, with specific details included in
 * the respective method documentations:
 *
 *
 *
 * A com.github.fernthedev.client.plugin.yml example:<blockquote><pre>
 * name: Inferno
 * version: 1.4.1
 * description: This com.github.fernthedev.client.plugin is so 31337. You can set yourself on fire.
 * # We could place every author in the authors list, but chose not to for illustrative purposes
 * # Also, having an author distinguishes that person as the project lead, and ensures their
 * # name is displayed first
 * author: CaptainInflamo
 * authors: [Cogito, verrier, EvilSeph]
 * website: http://www.curse.com/server-mods/minecraft/myplugin
 *
 * main: com.captaininflamo.bukkit.inferno.Inferno
 * depend: [NewFire, FlameWire]
 *
 * commands:
 * flagrate:
 * description: Set yourself on fire.
 * aliases: [combust_me, combustMe]
 * permission: inferno.flagrate
 * usage: Syntax error! Simply type /&lt;command&gt; to ignite yourself.
 * burningdeaths:
 * description: List how many times you have died by fire.
 * aliases: [burning_deaths, burningDeaths]
 * permission: inferno.burningdeaths
 * usage: |
 * /&lt;command&gt; [player]
 * Example: /&lt;command&gt; - see how many times you have burned to death
 * Example: /&lt;command&gt; CaptainIce - see how many times CaptainIce has burned to death
 *
 * permissions:
 * inferno.*:
 * description: Gives access to all Inferno commands
 * children:
 * inferno.flagrate: true
 * inferno.burningdeaths: true
 * inferno.burningdeaths.others: true
 * inferno.flagrate:
 * description: Allows you to ignite yourself
 * default: true
 * inferno.burningdeaths:
 * description: Allows you to see how many times you have burned to death
 * default: true
 * inferno.burningdeaths.others:
 * description: Allows you to see how many times others have burned to death
 * default: op
 * children:
 * inferno.burningdeaths: true
</pre></blockquote> *
 */
class PluginDescriptionFile {
    /**
     * @return internal use
     */
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
    @get:Deprecated("Internal use")
    var rawName: String? = null

    /**
     * Gives the name of the com.github.fernthedev.client.plugin. This name is a unique identifier for
     * plugins.
     *
     *  * Must consist of all alphanumeric characters, underscores, hyphon,
     * and period (a-z,A-Z,0-9, _.-). Any other character will cause the
     * com.github.fernthedev.client.plugin.yml to fail loading.
     *  * Used to determine the name of the com.github.fernthedev.client.plugin's data folder. Data
     * folders are placed in the ./plugins/ directory by default, but this
     * behavior should not be relied on.
     * should be used to reference the data folder.
     *  * It is good practice to name your jar the same as this, for example
     * 'MyPlugin.jar'.
     *  * Case sensitive.
     *  * The is the token referenced in [.getDepend], [     ][.getSoftDepend], and [.getLoadBefore].
     *  * Using spaces in the com.github.fernthedev.client.plugin's name is deprecated.
     *
     *
     *
     * In the com.github.fernthedev.client.plugin.yml, this entry is named `name`.
     *
     *
     * Example:<blockquote><pre>name: MyPlugin</pre></blockquote>
     *
     * @return the name of the com.github.fernthedev.client.plugin
     */
    var name: String? = null
        private set

    /**
     * Gives the fully qualified name of the main class for a com.github.fernthedev.client.plugin. The
     * format should follow the [ClassLoader.loadClass] syntax
     * to successfully be resolved at runtime. For most plugins, this is the
     *
     *  * This must contain the full namespace including the class file
     * itself.
     *  * If your namespace is `org.bukkit.com.github.fernthedev.client.plugin`, and your class
     * file is called `MyPlugin` then this must be
     * `org.bukkit.com.github.fernthedev.client.plugin.MyPlugin`
     *  * No com.github.fernthedev.client.plugin can use `org.bukkit.` as a base package for
     * **any class**, including the main class.
     *
     *
     *
     * In the com.github.fernthedev.client.plugin.yml, this entry is named `main`.
     *
     *
     * Example:
     * <blockquote><pre>main: org.bukkit.com.github.fernthedev.client.plugin.MyPlugin</pre></blockquote>
     *
     * @return the fully qualified main class for the com.github.fernthedev.client.plugin
     */
    var main: String? = null
        private set

    /**
     * @return unused
     */
    @get:Deprecated("unused")
    var classLoaderOf: String? = null
        private set

    /**
     * Gives a list of other plugins that the com.github.fernthedev.client.plugin requires.
     *
     *  * Use the value in the [.getName] of the target com.github.fernthedev.client.plugin to
     * specify the dependency.
     *  * If any com.github.fernthedev.client.plugin listed here is not found, your com.github.fernthedev.client.plugin will fail to
     * load at startup.
     *  * If multiple plugins list each other in `depend`,
     * creating a network with no individual com.github.fernthedev.client.plugin does not list another
     * com.github.fernthedev.client.plugin in the [network](https://en.wikipedia.org/wiki/Circular_dependency),
     * all plugins in that network will fail.
     *  * `depend` must be in must be in [YAML list
 * format](http://en.wikipedia.org/wiki/YAML#Lists).
     *
     *
     *
     * In the com.github.fernthedev.client.plugin.yml, this entry is named `depend`.
     *
     *
     * Example:
     * <blockquote><pre>depend:
     * - OnePlugin
     * - AnotherPlugin</pre></blockquote>
     *
     * @return immutable list of the com.github.fernthedev.client.plugin's dependencies
     */
    var depend: List<String>? = ImmutableList.of()
        private set

    /**
     * Gives a list of other plugins that the com.github.fernthedev.client.plugin requires for full
     * functionality. The [PluginManager] will make best effort to treat
     * all entries here as if they were a [dependency][.getDepend], but
     * will never fail because of one of these entries.
     *
     *  * Use the value in the [.getName] of the target com.github.fernthedev.client.plugin to
     * specify the dependency.
     *  * When an unresolvable com.github.fernthedev.client.plugin is listed, it will be ignored and does
     * not affect load order.
     *  * When a circular dependency occurs (a network of plugins depending
     * or soft-dependending each other), it will arbitrarily choose a
     * com.github.fernthedev.client.plugin that can be resolved when ignoring soft-dependencies.
     *  * `softdepend` must be in [YAML list
 * format](http://en.wikipedia.org/wiki/YAML#Lists).
     *
     *
     *
     * In the com.github.fernthedev.client.plugin.yml, this entry is named `softdepend`.
     *
     *
     * Example:
     * <blockquote><pre>softdepend: [OnePlugin, AnotherPlugin]</pre></blockquote>
     *
     * @return immutable list of the com.github.fernthedev.client.plugin's preferred dependencies
     */
    var softDepend: List<String>? = ImmutableList.of()
        private set

    /**
     * Gets the list of plugins that should consider this com.github.fernthedev.client.plugin a
     * soft-dependency.
     *
     *  * Use the value in the [.getName] of the target com.github.fernthedev.client.plugin to
     * specify the dependency.
     *  * The com.github.fernthedev.client.plugin should load before any other plugins listed here.
     *  * Specifying another com.github.fernthedev.client.plugin here is strictly equivalent to having the
     * specified com.github.fernthedev.client.plugin's [.getSoftDepend] include [     ][.getName].
     *  * `loadbefore` must be in [YAML list
 * format](http://en.wikipedia.org/wiki/YAML#Lists).
     *
     *
     *
     * In the com.github.fernthedev.client.plugin.yml, this entry is named `loadbefore`.
     *
     *
     * Example:
     * <blockquote><pre>loadbefore:
     * - OnePlugin
     * - AnotherPlugin</pre></blockquote>
     *
     * @return immutable list of plugins that should consider this com.github.fernthedev.client.plugin a
     * soft-dependency
     */
    var loadBefore: List<String> = ImmutableList.of()
        private set

    /**
     * Gives the version of the com.github.fernthedev.client.plugin.
     *
     *  * Version is an arbitrary string, however the most common format is
     * MajorRelease.MinorRelease.Build (eg: 1.4.1).
     *  * Typically you will increment this every time you release a new
     * feature or bug fix.
     *  * Displayed when a user types `/version PluginName`
     *
     *
     *
     * In the com.github.fernthedev.client.plugin.yml, this entry is named `version`.
     *
     *
     * Example:<blockquote><pre>version: 1.4.1</pre></blockquote>
     *
     * @return the version of the com.github.fernthedev.client.plugin
     */
    var version: String? = null
        private set

    /**
     * Gives the map of command-name to command-properties. Each entry in this
     * map corresponds to a single command and the respective values are the
     * properties of the command. Each property, *with the exception of
     * aliases*,
     * <table border=1>
     * <caption>The command section's description</caption>
     * <tr>
     * <th>Node</th>
     * <th>Method</th>
     * <th>Type</th>
     * <th>Description</th>
     * <th>Example</th>
    </tr> *
    </table> *
     * The commands are structured as a hiearchy of [nested mappings](http://yaml.org/spec/current.html#id2502325).
     * The primary (top-level, no intendentation) node is
     * ``commands`', while each individual command name is
     * indented, indicating it maps to some value (in our case, the
     * properties of the table above).
     *
     *
     * Here is an example bringing together the piecemeal examples above, as
     * well as few more definitions:<blockquote><pre>
     * commands:
     * flagrate:
     * description: Set yourself on fire.
     * aliases: [combust_me, combustMe]
     * permission: inferno.flagrate
     * permission-message: You do not have /&lt;permission&gt;
     * usage: Syntax error! Perhaps you meant /&lt;command&gt; PlayerName?
     * burningdeaths:
     * description: List how many times you have died by fire.
     * aliases:
     * - burning_deaths
     * - burningDeaths
     * permission: inferno.burningdeaths
     * usage: |
     * /&lt;command&gt; [player]
     * Example: /&lt;command&gt; - see how many times you have burned to death
     * Example: /&lt;command&gt; CaptainIce - see how many times CaptainIce has burned to death
     * # The next command has no description, aliases, etc. defined, but is still valid
     * # Having an empty declaration is useful for defining the description, permission, and messages from a configuration dynamically
     * apocalypse:
    </pre></blockquote> *
     * Note: Command names may not have a colon in their name.
     *
     * @return the commands this com.github.fernthedev.client.plugin will register
     */
    var commands: Map<String, Map<String, Any>>? = null
        private set

    /**
     * Gives a human-friendly description of the functionality the com.github.fernthedev.client.plugin
     * provides.
     *
     *  * The description can have multiple lines.
     *  * Displayed when a user types `/version PluginName`
     *
     *
     *
     * In the com.github.fernthedev.client.plugin.yml, this entry is named `description`.
     *
     *
     * Example:
     * <blockquote><pre>description: This com.github.fernthedev.client.plugin is so 31337. You can set yourself on fire.</pre></blockquote>
     *
     * @return description of this com.github.fernthedev.client.plugin, or null if not specified
     */
    var description: String? = null
        private set

    /**
     * Gives the list of authors for the com.github.fernthedev.client.plugin.
     *
     *  * Gives credit to the developer.
     *  * Used in some server error messages to provide helpful feedback on
     * who to contact when an error occurs.
     *  * A bukkit.org forum handle or email address is recommended.
     *  * Is displayed when a user types `/version PluginName`
     *  * `authors` must be in [YAML list
 * format](http://en.wikipedia.org/wiki/YAML#Lists).
     *
     *
     *
     * In the com.github.fernthedev.client.plugin.yml, this has two entries, `author` and
     * `authors`.
     *
     *
     * Single author example:
     * <blockquote><pre>author: CaptainInflamo</pre></blockquote>
     * Multiple author example:
     * <blockquote><pre>authors: [Cogito, verrier, EvilSeph]</pre></blockquote>
     * When both are specified, author will be the first entry in the list, so
     * this example:
     * <blockquote><pre>author: Grum
     * authors:
     * - feildmaster
     * - amaranth</pre></blockquote>
     * Is equivilant to this example:
     * <pre>authors: [Grum, feildmaster, aramanth]</pre>
     *
     * @return an immutable list of the com.github.fernthedev.client.plugin's authors
     */
    var authors: List<String>? = null
        private set

    /**
     * Gives the com.github.fernthedev.client.plugin's or com.github.fernthedev.client.plugin's author's website.
     *
     *  * A link to the Curse page that includes documentation and downloads
     * is highly recommended.
     *  * Displayed when a user types `/version PluginName`
     *
     *
     *
     * In the com.github.fernthedev.client.plugin.yml, this entry is named `website`.
     *
     *
     * Example:
     * <blockquote><pre>website: http://www.curse.com/server-mods/minecraft/myplugin</pre></blockquote>
     *
     * @return description of this com.github.fernthedev.client.plugin, or null if not specified
     */
    var website: String? = null
        private set

    /**
     * Gives the token to prefix com.github.fernthedev.client.plugin-specific logging messages with.
     *
     *  * If not specified, the server uses the com.github.fernthedev.client.plugin's [     name][.getName].
     *  * This should clearly indicate what com.github.fernthedev.client.plugin is being logged.
     *
     *
     *
     * In the com.github.fernthedev.client.plugin.yml, this entry is named `prefix`.
     *
     *
     * Example:<blockquote><pre>prefix: ex-why-zee</pre></blockquote>
     *
     * @return the prefixed logging token, or null if not specified
     */
    var prefix: String? = null
        private set

    /**
     * Gives the phase of server startup that the com.github.fernthedev.client.plugin should be loaded.
     *
     *  * Possible values are in [PluginLoadOrder].
     *  * Defaults to [PluginLoadOrder.POSTWORLD].
     *  * Certain caveats apply to each phase.
     *  * When different, [.getDepend], [.getSoftDepend], and
     * [.getLoadBefore] become relative in order loaded per-phase.
     * If a com.github.fernthedev.client.plugin loads at `STARTUP`, but a dependency loads
     * at `POSTWORLD`, the dependency will not be loaded before
     * the com.github.fernthedev.client.plugin is loaded.
     *
     *
     *
     * In the com.github.fernthedev.client.plugin.yml, this entry is named `load`.
     *
     *
     * Example:<blockquote><pre>load: STARTUP</pre></blockquote>
     *
     * @return the phase when the com.github.fernthedev.client.plugin should be loaded
     */
    var load = PluginLoadOrder.POSTWORLD
        private set
    private var lazyPermissions: Map<*, *>? = null

    /**
     * Gives a set of every [PluginAwareness] for a com.github.fernthedev.client.plugin. An awareness
     * dictates something that a com.github.fernthedev.client.plugin developer acknowledges when the com.github.fernthedev.client.plugin
     * is compiled. Some implementions may define extra awarenesses that are
     * not included in the API. Any unrecognized
     * awareness (one unsupported or in a future version) will cause a dummy
     * object to be created instead of failing.
     *
     *
     *  * Currently only supports the enumerated values in [     ].
     *  * Each awareness starts the identifier with bang-at
     * (`!@`).
     *  * Unrecognized (future / unimplemented) entries are quietly replaced
     * by a generic object that implements PluginAwareness.
     *  * A type of awareness must be defined by the runtime and acknowledged
     * by the API, effectively discluding any derived type from any
     * com.github.fernthedev.client.plugin's classpath.
     *  * `awareness` must be in [YAML list
 * format](http://en.wikipedia.org/wiki/YAML#Lists).
     *
     *
     *
     * In the com.github.fernthedev.client.plugin.yml, this entry is named `awareness`.
     *
     *
     * Example:<blockquote><pre>awareness:
     * - !@UTF8</pre></blockquote>
     *
     *
     * **Note:** Although unknown versions of some future awareness are
     * gracefully substituted, previous versions of Bukkit (ones prior to the
     * first implementation of awareness) will fail to load a com.github.fernthedev.client.plugin that
     * defines any awareness.
     *
     * @return a set containing every awareness for the com.github.fernthedev.client.plugin
     */
    var awareness: Set<PluginAwareness> = ImmutableSet.of()
        private set
    //    public PluginDescriptionFile(final InputStream stream) throws InvalidDescriptionException {
    //        loadMap(asMap(YAML.get().load(stream)));
    //    }
    /**
     * Loads a PluginDescriptionFile from the specified reader
     *
     * @param reader The reader
     * @throws InvalidDescriptionException If the PluginDescriptionFile is
     * invalid
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
    constructor(pluginName: String, pluginVersion: String?, mainClass: String?) {
        name = pluginName.replace(' ', '_')
        version = pluginVersion
        main = mainClass
    }

    constructor(stream: InputStream?)

    val fullName: String
        /**
         * Returns the name of a com.github.fernthedev.client.plugin, including the version. This method is
         * provided for convenience; it uses the [.getName] and [ ][.getVersion] entries.
         *
         * @return a descriptive name of the com.github.fernthedev.client.plugin and respective version
         */
        get() = "$name v$version"

    //    /**
    //     * Saves this PluginDescriptionFile to the given writer
    //     *
    //     * @param writer Writer to output this file to
    //     */
    //    public void save(Writer writer) {
    //        YAML.get().dump(saveMap(), writer);
    //    }
    @Throws(InvalidDescriptionException::class)
    private fun loadMap(map: Map<*, *>) {
        try {
            rawName = map["name"].toString()
            name = rawName
            if (!name!!.matches("^[A-Za-z0-9 _.-]+$".toRegex())) {
                throw InvalidDescriptionException("name '$name' contains invalid characters.")
            }
            name = name!!.replace(' ', '_')
        } catch (ex: NullPointerException) {
            throw InvalidDescriptionException(ex, "name is not defined")
        } catch (ex: ClassCastException) {
            throw InvalidDescriptionException(ex, "name is of wrong type")
        }
        version = try {
            map["version"].toString()
        } catch (ex: NullPointerException) {
            throw InvalidDescriptionException(ex, "version is not defined")
        } catch (ex: ClassCastException) {
            throw InvalidDescriptionException(ex, "version is of wrong type")
        }
        try {
            main = map["main"].toString()
            if (main!!.startsWith("org.bukkit.")) {
                throw InvalidDescriptionException("main may not be within the org.bukkit namespace")
            }
        } catch (ex: NullPointerException) {
            throw InvalidDescriptionException(ex, "main is not defined")
        } catch (ex: ClassCastException) {
            throw InvalidDescriptionException(ex, "main is of wrong type")
        }
        if (map["commands"] != null) {
            val commandsBuilder = ImmutableMap.builder<String, Map<String, Any>>()
            try {
                for ((key, value) in (map["commands"] as Map<*, *>?)!!) {
                    val commandBuilder = ImmutableMap.builder<String, Any>()
                    if (value != null) {
                        for ((key1, value1) in value as Map<*, *>) {
                            if (value1 is Iterable<*>) {
                                // This prevents internal alias list changes
                                val commandSubList = ImmutableList.builder<Any>()
                                for (commandSubListItem in value1) {
                                    if (commandSubListItem != null) {
                                        commandSubList.add(commandSubListItem)
                                    }
                                }
                                commandBuilder.put(key1.toString(), commandSubList.build())
                            } else if (value1 != null) {
                                commandBuilder.put(key1.toString(), value1)
                            }
                        }
                    }
                    commandsBuilder.put(key.toString(), commandBuilder.build())
                }
            } catch (ex: ClassCastException) {
                throw InvalidDescriptionException(ex, "commands are of wrong type")
            }
            commands = commandsBuilder.build()
        }
        if (map["class-loader-of"] != null) {
            classLoaderOf = map["class-loader-of"].toString()
        }
        depend = makePluginNameList(map, "depend")
        softDepend = makePluginNameList(map, "softdepend")
        loadBefore = makePluginNameList(map, "loadbefore")
        if (map["website"] != null) {
            website = map["website"].toString()
        }
        if (map["description"] != null) {
            description = map["description"].toString()
        }
        if (map["load"] != null) {
            try {
                load = PluginLoadOrder.valueOf((map["load"] as String?)!!.uppercase().replace("\\W".toRegex(), ""))
            } catch (ex: ClassCastException) {
                throw InvalidDescriptionException(ex, "load is of wrong type")
            } catch (ex: IllegalArgumentException) {
                throw InvalidDescriptionException(ex, "load is not a valid choice")
            }
        }
        authors = if (map["authors"] != null) {
            val authorsBuilder = ImmutableList.builder<String>()
            if (map["author"] != null) {
                authorsBuilder.add(map["author"].toString())
            }
            try {
                for (o in (map["authors"] as Iterable<*>?)!!) {
                    authorsBuilder.add(o.toString())
                }
            } catch (ex: ClassCastException) {
                throw InvalidDescriptionException(ex, "authors are of wrong type")
            } catch (ex: NullPointerException) {
                throw InvalidDescriptionException(ex, "authors are improperly defined")
            }
            authorsBuilder.build()
        } else if (map["author"] != null) {
            ImmutableList.of(map["author"].toString())
        } else {
            ImmutableList.of()
        }
        if (map["awareness"] is Iterable<*>) {
            val awareness: MutableSet<PluginAwareness> = HashSet()
            try {
                for (o in (map["awareness"] as Iterable<*>?)!!) {
                    awareness.add(o as PluginAwareness)
                }
            } catch (ex: ClassCastException) {
                throw InvalidDescriptionException(ex, "awareness has wrong type")
            }
            this.awareness = ImmutableSet.copyOf(awareness)
        }
        lazyPermissions = try {
            map["permissions"] as Map<*, *>?
        } catch (ex: ClassCastException) {
            throw InvalidDescriptionException(ex, "permissions are of the wrong type")
        }
        if (map["prefix"] != null) {
            prefix = map["prefix"].toString()
        }
    }

    private fun saveMap(): Map<String, Any?> {
        val map: MutableMap<String, Any?> = HashMap()
        map["name"] = name
        map["main"] = main
        map["version"] = version
        map["order"] = load.toString()
        if (commands != null) {
            map["command"] = commands
        }
        if (depend != null) {
            map["depend"] = depend
        }
        if (softDepend != null) {
            map["softdepend"] = softDepend
        }
        if (website != null) {
            map["website"] = website
        }
        if (description != null) {
            map["description"] = description
        }
        if (authors!!.size == 1) {
            map["author"] = authors!![0]
        } else if (authors!!.size > 1) {
            map["authors"] = authors
        }
        if (classLoaderOf != null) {
            map["class-loader-of"] = classLoaderOf
        }
        if (prefix != null) {
            map["prefix"] = prefix
        }
        return map
    }

    @Throws(InvalidDescriptionException::class)
    private fun asMap(`object`: Any): Map<*, *> {
        if (`object` is Map<*, *>) {
            return `object`
        }
        throw InvalidDescriptionException("$`object` is not properly structured.")
    }

    companion object {
        @Throws(InvalidDescriptionException::class)
        private fun makePluginNameList(map: Map<*, *>, key: String): List<String> {
            val value = map[key] ?: return ImmutableList.of()
            val builder = ImmutableList.builder<String>()
            try {
                for (entry in value as Iterable<*>) {
                    builder.add(entry.toString().replace(' ', '_'))
                }
            } catch (ex: ClassCastException) {
                throw InvalidDescriptionException(ex, "$key is of wrong type")
            } catch (ex: NullPointerException) {
                throw InvalidDescriptionException(ex, "invalid $key format")
            }
            return builder.build()
        }
    }
}