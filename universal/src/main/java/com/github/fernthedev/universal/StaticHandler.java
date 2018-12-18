package com.github.fernthedev.universal;

import com.google.common.io.Resources;
import com.google.gson.Gson;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.json.simple.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
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



    public static String getVersion() {
        return version;
    }

    public StaticHandler() {
        TranslateData translateData = gson.fromJson(getFile("variables.json"),TranslateData.class);

        version = translateData.getVersion();
        Logger.getLogger("io.netty").setLevel(Level.OFF);
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
