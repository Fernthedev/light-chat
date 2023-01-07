package com.github.fernthedev.lightchat.core.util

import org.jetbrains.annotations.Contract
import java.math.BigDecimal
import java.math.BigInteger

/**
 * This translates classes to
 * strings that can be translated back
 * to classes in the client
 * to avoid using java class names.
 */
object ClassTranslator {
    private val classTranslateMap: MutableMap<Class<*>, String> = HashMap()
    private val reverseTranslationMap: MutableMap<String, Class<*>> = HashMap()

    init {
        registerTranslations()
        registerReverseTranslations()
    }

    private fun registerTranslations() {
        registerTranslation(String::class.java, "string")
        registerTranslation(BigInteger::class.java, "big_integer")
        registerTranslation(BigDecimal::class.java, "big_decimal")
        registerTranslation(Double::class.java, "double")
        registerTranslation(Double::class.javaPrimitiveType!!, "double")
        registerTranslation(Long::class.java, "long")
        registerTranslation(Long::class.javaPrimitiveType!!, "long")
        registerTranslation(Byte::class.java, "byte")
        registerTranslation(Byte::class.javaPrimitiveType!!, "byte")
        registerTranslation(Int::class.java, "int")
        registerTranslation(Int::class.javaPrimitiveType!!, "int")
        registerTranslation(Float::class.java, "float")
        registerTranslation(Float::class.javaPrimitiveType!!, "float")
        registerTranslation(Short::class.java, "short")
        registerTranslation(Short::class.javaPrimitiveType!!, "short")
    }

    private fun registerReverseTranslations() {
        registerReverseTranslation(String::class.java, "string")
        registerReverseTranslation(BigInteger::class.java, "big_integer")
        registerReverseTranslation(BigDecimal::class.java, "big_decimal")
        registerReverseTranslation(Double::class.javaPrimitiveType!!, "double")
        registerReverseTranslation(Long::class.javaPrimitiveType!!, "long")
        registerReverseTranslation(Int::class.javaPrimitiveType!!, "int")
        registerReverseTranslation(Byte::class.javaPrimitiveType!!, "byte")
        registerReverseTranslation(Float::class.javaPrimitiveType!!, "float")
        registerReverseTranslation(Short::class.javaPrimitiveType!!, "short")
    }

    fun registerTranslation(clazz: Class<*>, translate: String) {
        classTranslateMap[clazz] = translate
    }

    fun registerReverseTranslation(clazz: Class<*>, translate: String) {
        reverseTranslationMap[translate] = clazz
    }

    @Contract("null -> null")
    fun translate(clazz: Class<*>): String? {
        return classTranslateMap[clazz]
    }

    @Contract("null -> null")
    fun findTranslation(lookup: String): Class<*>? {
        return reverseTranslationMap[lookup]
    }
}