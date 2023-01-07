package com.github.fernthedev.lightchat.core.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

/**
 * This translates classes to
 * strings that can be translated back
 * to classes in the client
 * to avoid using java class names.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ClassTranslator {

    private static final Map<Class<?>, String> classTranslateMap = new HashMap<>();
    private static final Map<String, Class<?>> reverseTranslationMap = new HashMap<>();

    static {
        registerTranslations();
        registerReverseTranslations();
    }

    private static void registerTranslations() {
        registerTranslation(String.class, "string");
        registerTranslation(BigInteger.class, "big_integer");
        registerTranslation(BigDecimal.class, "big_decimal");
        registerTranslation(Double.class, "double");
        registerTranslation(double.class, "double");
        registerTranslation(Long.class, "long");
        registerTranslation(long.class, "long");
        registerTranslation(Byte.class, "byte");
        registerTranslation(byte.class, "byte");
        registerTranslation(Integer.class, "int");
        registerTranslation(int.class, "int");
        registerTranslation(Float.class, "float");
        registerTranslation(float.class, "float");
        registerTranslation(Short.class, "short");
        registerTranslation(short.class, "short");
    }

    private static void registerReverseTranslations() {
        registerReverseTranslation(String.class, "string");
        registerReverseTranslation(BigInteger.class, "big_integer");
        registerReverseTranslation(BigDecimal.class, "big_decimal");
        registerReverseTranslation(double.class, "double");
        registerReverseTranslation(long.class, "long");
        registerReverseTranslation(int.class, "int");
        registerReverseTranslation(byte.class, "byte");
        registerReverseTranslation(float.class, "float");
        registerReverseTranslation(short.class, "short");
    }

    public static void registerTranslation(@NonNull Class<?> clazz, @NonNull String translate) {
        classTranslateMap.put(clazz, translate);
    }

    public static void registerReverseTranslation(@NonNull Class<?> clazz, @NonNull String translate) {
        reverseTranslationMap.put(translate, clazz);
    }

    @Contract("null -> null")
    @Nullable
    public static String translate(Class<?> clazz) {
        return classTranslateMap.get(clazz);
    }
    @Contract("null -> null")
    @Nullable
    public static Class<?> findTranslation(String lookup) {
        return reverseTranslationMap.get(lookup);
    }

}
