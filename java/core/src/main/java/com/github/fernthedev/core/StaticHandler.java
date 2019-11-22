package com.github.fernthedev.core;

import com.google.common.io.Resources;
import com.google.gson.Gson;
import io.netty.util.CharsetUtil;
import lombok.Getter;
import lombok.NonNull;
import lombok.Synchronized;
import net.minecrell.terminalconsole.TerminalConsoleAppender;
import org.apache.commons.lang3.SystemUtils;
import org.apache.logging.log4j.util.PropertiesUtil;
import org.jline.reader.Completer;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.terminal.Terminal;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;

// Import log4j classes.

public class StaticHandler {

    public static final int LINE_LIMIT = 8000;
    public static final String END_STRING = "\n\r";
    public static final int KEY_SIZE = 4096;
    public static final int AES_KEY_SIZE = 256;
    public static final String AES_KEY_MODE = "AES";
    public static final String AES_CIPHER_TRANSFORMATION = "AES/CBC/PKCS5Padding";


    public static final String PACKET_PACKAGE = "com.github.fernthedev.core.packets";
    public static final String address = "224.0.1.42";

    private static final Gson gson = new Gson();
    public static final Charset CHARSET_FOR_STRING = CharsetUtil.UTF_8;

    public static boolean isDebug = false;
    private static String version = null;
    public static String os = System.getProperty("os.name");
    public static boolean isLight = false;

    @Getter
    private static final String cipherTransformationOld = "AES/CBC/PKCS5Padding";

//    public static final String RSA_CIPHER_TRANSFORMATION = "RSA/ECB/OAEPWITHSHA-512ANDMGF1PADDING"; //"RSA/ECB/PKCS1Padding";

    public static final String RSA_CIPHER_TRANSFORMATION = "RSA/ECB/PKCS1Padding";


    @Getter
    private static final String keySpecTransformation = "AES";

    @Getter
    private static final String KeyFactoryString = "PBKDF2WithHmacSHA1";

    @Synchronized
    public static void setCore(Core core) {
        StaticHandler.core = core;
        PacketRegistry.registerDefaultPackets();
    }

    @Getter
    private static Core core;

    public static String getVersion() {
        return version;
    }

    public StaticHandler() {
        TranslateData translateData = gson.fromJson(getFile("variables.json"),TranslateData.class);

        version = translateData.getVersion();


    }


    public static void runOnAnyOSConsole(String[] args) {

        String filename = StaticHandler.class.getProtectionDomain().getCodeSource().getLocation().toString().substring(6);

        String[] newArgs = new String[0];

        if(SystemUtils.IS_OS_WINDOWS) {
            newArgs = new String[]{"cmd", "/c", "start", "cmd", "/c", "java -jar \"" + filename + "\" " + Arrays.toString(args)};
        }

        if(SystemUtils.IS_OS_LINUX || SystemUtils.IS_OS_MAC_OSX) {
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

    private static LineReader lineReader;


    public static void setupLoggers() {
        System.setProperty("log4j.configurationFile","log4j2.xml");
        System.setProperty("log4j2.contextSelector","org.apache.logging.log4j.core.async.AsyncLoggerContextSelector");
        System.setProperty("terminal.jline", String.valueOf(true));
        //Logger logger = LogManager.getRootLogger();

        //Logger nettyLogger = LoggerFactory.getLogger("io.netty");

    }

    @Deprecated
    public static void setupTerminal(@NonNull Completer completer) {
        final Terminal terminal = TerminalConsoleAppender.getTerminal();

        if(terminal == null) {
            System.err.println("Terminal is not supported. Features such as auto complete may not work.");
            return;
        }

        lineReader = LineReaderBuilder.builder()
                .terminal(terminal)
                .completer(completer)
                .build();

        lineReader.setOpt(LineReader.Option.DISABLE_EVENT_EXPANSION);
        lineReader.unsetOpt(LineReader.Option.INSERT_TAB);

        TerminalConsoleAppender.setReader(lineReader);

       // lineReader.option(LineReader.Option.AUTO_FRESH_LINE,true);

    }

    public static String readLine(String format) {
        return lineReader.readLine(format);


        /*
        if (System.console() != null) {
            return System.console().readLine();
        }
        //System.out.print(format);
        BufferedReader reader = new BufferedReader(new InputStreamReader(
                System.in));
        try {
            return reader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;*/
    }

    private class TranslateData {
        private String version;

        private String getVersion() {
            return version;
        }
    }

    public static String getFile(String fileName) {

        StringBuilder result = new StringBuilder();

        //Get file from resources folder
        ClassLoader classLoader = StaticHandler.class.getClassLoader();

        try (Scanner scanner = new Scanner(Objects.requireNonNull(classLoader.getResourceAsStream(fileName)))) {
            while(scanner.hasNextLine()) {
                String line = scanner.nextLine();
                result.append(line).append("\n");
            }
        }

        return result.toString();

    }

    public static String getFile(File file) {

        StringBuilder result = new StringBuilder();

        //Get file from resources folder
        //ClassLoader classLoader = StaticHandler.class.getClassLoader();

        try (Scanner scanner = new Scanner(Objects.requireNonNull(file))) {
            while(scanner.hasNextLine()) {
                String line = scanner.nextLine();
                result.append(line).append("\n");
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return result.toString();

    }


    private static Boolean getOptionalBooleanProperty(String name) {
        String value = PropertiesUtil.getProperties().getStringProperty(name);
        if (value == null) {
            return null;
        }

        if (value.equalsIgnoreCase("true")) {
            return Boolean.TRUE;
        } else if (value.equalsIgnoreCase("false"))  {
            return Boolean.FALSE;
        } else {
            return null;
        }
    }

    public static String readResource(final String fileName, Charset charset) throws IOException {
        return Resources.toString(Resources.getResource(fileName), charset);
    }

}
