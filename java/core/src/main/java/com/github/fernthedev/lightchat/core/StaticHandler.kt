package com.github.fernthedev.lightchat.core

import com.github.fernthedev.lightchat.core.api.APIUsage
import com.github.fernthedev.lightchat.core.exceptions.DebugException
import com.github.fernthedev.lightchat.core.packets.PacketJSON
import com.github.fernthedev.lightchat.core.util.Log4jDebug
import com.google.gson.Gson
import org.apache.commons.lang3.SystemUtils
import org.slf4j.Logger
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.util.*

// Import log4j classes.
object StaticHandler {
    var DEFAULT_PACKET_ID_MAX = 10
    const val AES_KEY_SIZE = 256
    const val AES_KEY_MODE = "AES"
    const val AES_CIPHER_TRANSFORMATION = "AES/GCM/NoPadding"
    val PACKET_JSON_PACKAGE: String by lazy { PacketJSON::class.java.packageName }

    val OS: String = System.getProperty("os.name")

    const val RSA_CIPHER_TRANSFORMATION = "RSA/ECB/PKCS1Padding"


    @JvmStatic
    val VERSION_DATA: VersionData
    //    public static final String RSA_CIPHER_TRANSFORMATION = "RSA/ECB/OAEPWITHSHA-512ANDMGF1PADDING"; //"RSA/ECB/PKCS1Padding";
    /**
     * Modify if required.
     */
    var multicastAddress = "224.0.1.42"
    private var debug = false

    @JvmStatic
    lateinit var core: Core
        private set

    private var log4j = false

    init {
        VERSION_DATA = VersionData(Gson().fromJson(getFile("variables.json"), VariablesJSON::class.java))
        try {
            Class.forName("org.apache.logging.log4j.core.config.Configurator")
            log4j = true
        } catch (e: ClassNotFoundException) {
            log4j = false
            if (debug) {
                DebugException("Could not find Log4J though it is not required. Just a warning", e).printStackTrace()
            }
        }
    }

    @JvmStatic
    fun setDebug(debug: Boolean) {
        var logger: Logger? = null
        if (StaticHandler::core.isInitialized) {
            if (StaticHandler.debug != debug) {
                core.logger.info("Set debug mode to: {}", debug)
            }
            logger = core.logger
        }
        StaticHandler.debug = debug
        if (log4j) Log4jDebug.setDebug(logger, debug)
    }

    @Synchronized
    @JvmStatic
    fun setCore(core: Core, override: Boolean) {
        if (StaticHandler::core.isInitialized && !override) return

        val initialized = StaticHandler::core.isInitialized
        StaticHandler.core = core

        // Updates debug config
        setDebug(debug)
        if (!initialized) {
            PacketJsonRegistry.registerDefaultPackets()
        }
    }

    @APIUsage
    fun checkVersionRequirements(otherVer: VersionData): Boolean {
        return VERSION_DATA.version >= otherVer.minVersion && VERSION_DATA.minVersion <= otherVer.minVersion
    }

    @JvmStatic
    @APIUsage
    fun getVersionRangeStatus(otherVersion: VersionData): VersionRange {
        return getVersionRangeStatus(VERSION_DATA, otherVersion)
    }

    @APIUsage
    fun getVersionRangeStatus(versionData: VersionData, otherVersion: VersionData): VersionRange {
        val current = versionData.version
        val min = versionData.minVersion
        val otherCurrent = otherVersion.version
        val otherMin = otherVersion.minVersion

        // Current version is smaller than the server's required minimum
        return if (current < otherMin) {
            VersionRange.WE_ARE_LOWER
        } else {
            // Current version is larger than server's minimum version
            if (min > otherCurrent) {
                VersionRange.WE_ARE_HIGHER
            } else {
                VersionRange.MATCH_REQUIREMENTS
            }
        }
    }

    fun displayVersion() {
        core.logger.info(
            ColorCode.GREEN.toString() + "Running the version: {} minimum required: {}",
            VERSION_DATA.version,
            VERSION_DATA.minVersion
        )
    }

    @Deprecated("This was meant to start the server on any OS. Scrapped since the server, core and client are becoming an API")
    fun runOnAnyOSConsole(args: Array<String>) {
        val filename = StaticHandler::class.java.protectionDomain.codeSource.location.toString().substring(6)
        var newArgs = arrayOfNulls<String>(0)
        if (SystemUtils.IS_OS_WINDOWS) {
            newArgs =
                arrayOf("cmd", "/c", "start", "cmd", "/c", "java -jar \"" + filename + "\" " + args.contentToString())
        }
        if (SystemUtils.IS_OS_LINUX || SystemUtils.IS_OS_MAC_OSX) {
            newArgs = arrayOf("bash", "bash", "java -jar \"" + filename + "\" " + args.contentToString())
        }
        val launchArgs: MutableList<String> = ArrayList(listOf(*newArgs))
        launchArgs.addAll(listOf(*args))
        try {
            Runtime.getRuntime().exec(launchArgs.toTypedArray())
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    @JvmStatic
    fun setupLoggers() {
        System.setProperty("log4j.configurationFile", "log4j2.xml")
        System.setProperty("log4j2.contextSelector", "org.apache.logging.log4j.core.async.AsyncLoggerContextSelector")
        System.setProperty("terminal.jline", true.toString())
        //Logger logger = LogManager.getRootLogger();

        //Logger nettyLogger = LoggerFactory.getLogger("io.netty");
    }

    private fun getFile(fileName: String): String {
        val result = StringBuilder()

        //Get file from resources folder
        val classLoader = StaticHandler::class.java.classLoader
        Scanner(Objects.requireNonNull(classLoader.getResourceAsStream(fileName))).use { scanner ->
            while (scanner.hasNextLine()) {
                val line = scanner.nextLine()
                result.append(line).append("\n")
            }
        }
        return result.toString()
    }

    fun getFile(file: File): String {
        val result = StringBuilder()

        //Get file from resources folder
        try {
            Scanner(Objects.requireNonNull(file)).use { scanner ->
                while (scanner.hasNextLine()) {
                    val line = scanner.nextLine()
                    result.append(line).append("\n")
                }
            }
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }
        return result.toString()
    }

    @JvmStatic
    fun isDebug(): Boolean {
        return debug
    }

    enum class VersionRange(val id: Int) {
        OTHER_IS_LOWER(-1), WE_ARE_HIGHER(-1), MATCH_REQUIREMENTS(0), WE_ARE_LOWER(1), OTHER_IS_HIGHER(1)

    }
}