package com.github.fernthedev.universal;

import com.google.common.io.Resources;
import com.google.gson.Gson;
import lombok.Getter;
import org.apache.commons.lang3.SystemUtils;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;
import org.jline.reader.Completer;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.json.simple.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;

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


    private static Terminal terminal;

    public static void setupTerminal(Completer completer, Logger logger) {
        try {
            terminal = TerminalBuilder.builder()
                    .build();

        } catch (IOException e) {
            e.printStackTrace();
        }

        lineReader = LineReaderBuilder.builder()
                .terminal(terminal)
                .completer(completer)
                .build();

        lineReader.option(LineReader.Option.AUTO_FRESH_LINE,true);
        logger.addAppender(new LogAppender());

    }

    public static synchronized String readLine(String format) {

        new Thread(() -> {
            while(lineReader.isReading()) {

            }
        }).start();

        String l = lineReader.readLine(format);
        return l;


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
        ClassLoader classLoader = StaticHandler.class.getClassLoader();

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

    public static String readResource(final String fileName, Charset charset) throws IOException {
        return Resources.toString(Resources.getResource(fileName), charset);
    }

    private static class LogAppender extends AppenderSkeleton {


        /**
         * Subclasses of <code>AppenderSkeleton</code> should implement this
         * method to perform actual logging. See also {@link #doAppend
         * AppenderSkeleton.doAppend} method.
         *
         * @param event
         * @since 0.9.0
         */
        @Override
        protected void append(LoggingEvent event) {
            if (lineReader.isReading()) {
                lineReader.callWidget(LineReader.CLEAR);
                lineReader.callWidget(LineReader.REDRAW_LINE);
                lineReader.callWidget(LineReader.REDISPLAY);
                lineReader.getTerminal().writer().flush();
            }


        }

        /**
         * Release any resources allocated within the appender such as file
         * handles, network connections, etc.
         *
         * <p>It is a programming error to append to a closed appender.
         *
         * @since 0.8.4
         */
        @Override
        public void close() {

        }

        /**
         * Configurators call this method to determine if the appender
         * requires a layout. If this method returns <code>true</code>,
         * meaning that layout is required, then the configurator will
         * configure an layout using the configuration information at its
         * disposal.  If this method returns <code>false</code>, meaning that
         * a layout is not required, then layout configuration will be
         * skipped even if there is available layout configuration
         * information at the disposal of the configurator..
         *
         * <p>In the rather exceptional case, where the appender
         * implementation admits a layout but can also work without it, then
         * the appender should return <code>true</code>.
         *
         * @since 0.8.4
         */
        @Override
        public boolean requiresLayout() {
            return false;
        }
    }

}
