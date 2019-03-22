package com.github.fernthedev.universal;

import com.google.common.io.Resources;
import com.google.gson.Gson;
import lombok.Getter;
import org.json.simple.JSONObject;

import java.io.*;
import java.nio.charset.Charset;
import java.util.Objects;
import java.util.Scanner;

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

    public static String readLine(String format)  {
        if (System.console() != null) {
            return System.console().readLine(format);
        }
        System.out.print(format);
        BufferedReader reader = new BufferedReader(new InputStreamReader(
                System.in));
        try {
            return reader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
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

}
