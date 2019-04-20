package com.github.fernthedev.universal;

import com.google.common.io.Resources;
import com.google.gson.Gson;
import lombok.Getter;
import lombok.NonNull;
import net.minecrell.terminalconsole.TerminalConsoleAppender;
import okio.BufferedSource;
import okio.Okio;
import okio.Source;
import org.apache.commons.lang3.SystemUtils;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.util.PropertiesUtil;
import org.jline.reader.Completer;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.terminal.Terminal;
import org.json.simple.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

// Import log4j classes.

public class StaticHandler {
    public static String address = "230.0.0.0";

    public static JSONObject jsonObject = new JSONObject();

    private static Gson gson = new Gson();

    public static boolean isDebug = false;
    private static String version = null;
    public static String os = System.getProperty("os.name");
    public static boolean isLight = false;

    @Getter
    private static final String cipherTransformation = "AES/CBC/PKCS5Padding";

    @Getter
    private static final String ObjecrCipherTrans = "AES";

    @Getter
    private static final String keySpecTransformation = "AES";

    @Getter
    private static final String KeyFactoryString = "PBKDF2WithHmacSHA1";

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
        //System.setProperty("terminal.jline", String.valueOf(true));
        //Logger logger = LogManager.getRootLogger();

        //Logger nettyLogger = LoggerFactory.getLogger("io.netty");

    }

    public static void setupTerminal(@NonNull Completer completer) {
        final Terminal terminal = TerminalConsoleAppender.getTerminal();

        Validate.notNull(terminal);

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
        InputStream string = classLoader.getResourceAsStream(fileName);

        try (Source fileSource = Okio.source(Objects.requireNonNull(string));
             BufferedSource bufferedSource = Okio.buffer(fileSource)) {
            while(true) {
                String line = bufferedSource.readUtf8Line();
                if(line == null) break;

                result.append(line).append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result.toString();

    }

    public static String getFile(File file) {

        StringBuilder result = new StringBuilder();

        //Get file from resources folder
        //ClassLoader classLoader = StaticHandler.class.getClassLoader();

        try (Source fileSource = Okio.source(file);
             BufferedSource bufferedSource = Okio.buffer(fileSource)) {

            while(true) {
                String line = bufferedSource.readUtf8Line();
                if(line == null) break;

                result.append(line).append("\n");
            }
        } catch (IOException e) {
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
