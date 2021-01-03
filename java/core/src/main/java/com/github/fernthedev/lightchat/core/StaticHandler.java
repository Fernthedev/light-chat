package com.github.fernthedev.lightchat.core;

import com.github.fernthedev.lightchat.core.api.APIUsage;
import com.github.fernthedev.lightchat.core.exceptions.DebugException;
import com.github.fernthedev.lightchat.core.util.Log4jDebug;
import com.google.gson.Gson;
import io.netty.util.CharsetUtil;
import lombok.*;
import org.apache.commons.lang3.SystemUtils;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.slf4j.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;

// Import log4j classes.

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class StaticHandler {

    public static int DEFAULT_PACKET_ID_MAX = 10;

    @Getter
    @Setter
    private static int lineLimit = 8000;

    public static final String END_STRING = "\n\r";

    public static final int AES_KEY_SIZE = 256;
    public static final String AES_KEY_MODE = "AES";
    public static final String AES_CIPHER_TRANSFORMATION = "AES/CBC/PKCS5Padding";


    public static final String PACKET_PACKAGE = "com.github.fernthedev.lightchat.core.packets";
    public static final Charset CHARSET_FOR_STRING = CharsetUtil.UTF_8;
    public static final String OS = System.getProperty("os.name");


    public static final String RSA_CIPHER_TRANSFORMATION = "RSA/ECB/PKCS1Padding";

    private static final Gson gson = new Gson();

    @Getter
    private static final String CIPHER_TRANSFORMATION_OLD = "AES/CBC/PKCS5Padding";

    @Getter
    private static final String KEY_SPEC_TRANSFORMATION = "AES";

    @Getter
    private static final String KEY_FACTORY_STRING = "PBKDF2WithHmacSHA1";

    @Getter
    private static final VersionData VERSION_DATA;

//    public static final String RSA_CIPHER_TRANSFORMATION = "RSA/ECB/OAEPWITHSHA-512ANDMGF1PADDING"; //"RSA/ECB/PKCS1Padding";
    /**
     * Modify if required.
     */
    @Getter
    @Setter
    private static String multicastAddress = "224.0.1.42";

    @Getter
    private static boolean debug = false;

    @Getter
    private static Core core;

    private static boolean log4j;

    static {
        VERSION_DATA = new VersionData(gson.fromJson(getFile("variables.json"), VariablesJSON.class));
        try {
            Class.forName("org.apache.logging.log4j.core.config.Configurator");
            log4j = true;
        } catch (ClassNotFoundException e) {
            log4j = false;
            if (debug) {
                new DebugException("Could not find Log4J though it is not required. Just a warning",e).printStackTrace();
            }
        }
    }



    public static void setDebug(boolean debug) {
        if (StaticHandler.debug != debug && getCore() != null) {
            StaticHandler.getCore().getLogger().info("Set debug mode to: {}", debug);
        }

        StaticHandler.debug = debug;

        Logger logger = null;

        if (getCore() != null && getCore().getLogger() != null)
            logger = getCore().getLogger();

        if (log4j)
            Log4jDebug.setDebug(logger, debug);
    }

    @Synchronized
    public static void setCore(Core core, boolean override) {
        if (StaticHandler.core != null && !override) return;

        boolean ifNull = StaticHandler.core == null;

        StaticHandler.core = core;

        // Updates debug config
        setDebug(debug);
//        Configurator.setLevel(getCore().getLogger().getName(), debug ? Level.DEBUG : Level.INFO);

        if (ifNull)
            PacketRegistry.registerDefaultPackets();
    }

    @APIUsage
    public static boolean checkVersionRequirements(VersionData otherVer) {
        return VERSION_DATA.getVersion().compareTo(otherVer.getMinVersion()) >= 0 && VERSION_DATA.getMinVersion().compareTo(otherVer.getMinVersion()) <= 0;
    }

    @APIUsage
    public static VersionRange getVersionRangeStatus(VersionData otherVersion) {
        return getVersionRangeStatus(VERSION_DATA, otherVersion);
    }

    public static VersionRange getVersionRangeStatus(VersionData versionData, VersionData otherVersion) {
        DefaultArtifactVersion current = versionData.getVersion();
        DefaultArtifactVersion min = versionData.getMinVersion();

        DefaultArtifactVersion otherCurrent = otherVersion.getVersion();
        DefaultArtifactVersion otherMin = otherVersion.getMinVersion();

        // Current version is smaller than the server's required minimum
        if (current.compareTo(otherMin) < 0) {
            return VersionRange.WE_ARE_LOWER;
        } else

            // Current version is larger than server's minimum version
            if (min.compareTo(otherCurrent) > 0) {
                return VersionRange.WE_ARE_HIGHER;
            } else return VersionRange.MATCH_REQUIREMENTS;
    }

    public static void displayVersion() {
        getCore().getLogger().info(ColorCode.GREEN + "Running the version: {} minimum required: {}", StaticHandler.getVERSION_DATA().getVersion(), StaticHandler.getVERSION_DATA().getMinVersion());
    }

    /**
     *
     * @deprecated This was meant to start the server on any OS. Scrapped since the server, core and client are becoming an API
     */
    @Deprecated
    public static void runOnAnyOSConsole(String[] args) {

        String filename = StaticHandler.class.getProtectionDomain().getCodeSource().getLocation().toString().substring(6);

        String[] newArgs = new String[0];

        if (SystemUtils.IS_OS_WINDOWS) {
            newArgs = new String[]{"cmd", "/c", "start", "cmd", "/c", "java -jar \"" + filename + "\" " + Arrays.toString(args)};
        }

        if (SystemUtils.IS_OS_LINUX || SystemUtils.IS_OS_MAC_OSX) {
            newArgs = new String[]{"bash", "bash", "java -jar \"" + filename + "\" " + Arrays.toString(args)};
        }

        List<String> launchArgs = new ArrayList<>(Arrays.asList(newArgs));
        launchArgs.addAll(Arrays.asList(args));

        try {
            Runtime.getRuntime().exec(launchArgs.toArray(new String[]{}));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void setupLoggers() {
        System.setProperty("log4j.configurationFile", "log4j2.xml");
        System.setProperty("log4j2.contextSelector", "org.apache.logging.log4j.core.async.AsyncLoggerContextSelector");
        System.setProperty("terminal.jline", String.valueOf(true));
        //Logger logger = LogManager.getRootLogger();

        //Logger nettyLogger = LoggerFactory.getLogger("io.netty");

    }

    public static String getFile(String fileName) {

        StringBuilder result = new StringBuilder();

        //Get file from resources folder
        ClassLoader classLoader = StaticHandler.class.getClassLoader();

        try (Scanner scanner = new Scanner(Objects.requireNonNull(classLoader.getResourceAsStream(fileName)))) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                result.append(line).append("\n");
            }
        }

        return result.toString();

    }

//    private static LineReader lineReader;
//
//    /**
//     *
//     * @deprecated Using {@link ConsoleHandler} now
//     */
//    @Deprecated
//    public static void setupTerminal(@NonNull Completer completer) {
//        final Terminal terminal = TerminalConsoleAppender.getTerminal();
//
//        if(terminal == null) {
//            System.err.println("Terminal is not supported. Features such as auto complete may not work.");
//            return;
//        }
//
//        lineReader = LineReaderBuilder.builder()
//                .terminal(terminal)
//                .completer(completer)
//                .build();
//
//        System.out.println(terminal + " is the terminal " + completer + " is completer");
//
//        lineReader.setOpt(LineReader.Option.DISABLE_EVENT_EXPANSION);
//        lineReader.unsetOpt(LineReader.Option.INSERT_TAB);
//
//        TerminalConsoleAppender.setReader(lineReader);
//
//       // lineReader.option(LineReader.Option.AUTO_FRESH_LINE,true);
//
//    }
//
//    public static String readLine(String format) {
//        return lineReader.readLine(format);
//
//
//        /*
//        if (System.console() != null) {
//            return System.console().readLine();
//        }
//        //System.out.print(format);
//        BufferedReader reader = new BufferedReader(new InputStreamReader(
//                System.in));
//        try {
//            return reader.readLine();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return null;*/
//    }

    public static String getFile(File file) {

        StringBuilder result = new StringBuilder();

        //Get file from resources folder
        //ClassLoader classLoader = StaticHandler.class.getClassLoader();

        try (Scanner scanner = new Scanner(Objects.requireNonNull(file))) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                result.append(line).append("\n");
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return result.toString();

    }

    @AllArgsConstructor
    @Getter
    public enum VersionRange {
        OTHER_IS_LOWER(-1),
        WE_ARE_HIGHER(-1),
        MATCH_REQUIREMENTS(0),
        WE_ARE_LOWER(1),
        OTHER_IS_HIGHER(1);

        private final int id;
    }

}
