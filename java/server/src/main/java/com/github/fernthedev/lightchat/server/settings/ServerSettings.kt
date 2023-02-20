package com.github.fernthedev.lightchat.server.settings

import com.github.fernthedev.fernutils.thread.ThreadUtils
import com.github.fernthedev.lightchat.core.CoreSettings
import org.apache.commons.lang3.StringUtils
import org.apache.commons.lang3.Validate
import org.apache.commons.lang3.tuple.ImmutablePair
import org.apache.commons.lang3.tuple.Pair
import java.lang.reflect.Field
import java.util.*
import java.util.function.Function
import java.util.stream.Collectors


data class ServerSettings(
    @SettingValue
    var port: Int = 2000,

    @SettingValue
    var password: String = "password",

    @SettingValue
    var rsaKeySize: Int = 4096,

    @SettingValue(name = "multicast")
    var useMulticast: Boolean = false,

    @SettingValue(name = "usepassword")
    var passwordRequiredForLogin: Boolean = false,

    @SettingValue(name = "usenativetransport")
    var useNativeTransport: Boolean = true,

    @SettingValue(name = "codec")
    var jsonCodec: String = DEFAULT_CODEC,
) : CoreSettings() {

    @Deprecated("")
    fun setNewValue(oldValue: String, newValue: String) {
        var value: Any = newValue
        when (newValue.lowercase(Locale.getDefault())) {
            "true" -> value = true
            "false" -> value = false
        }
        if (StringUtils.isNumeric(newValue)) {
            value = try {
                newValue.toInt()
            } catch (ignored: NumberFormatException) {
                throw IllegalArgumentException("Incorrect integer value")
            }
        }
        require(!(newValue == "" || oldValue == "")) { "Values cannot be empty" }
        when (oldValue.lowercase(Locale.getDefault())) {
            "password" -> password = value.toString()
            else -> throw IllegalArgumentException("No such value named $oldValue found")
        }
    }

    @Deprecated("Use {@link #getSettingValues(boolean)} or {@link #getSettingValuesAsync(boolean)} instead")
    fun getSettingNames(editable: Boolean): List<String> {
        val stringList: MutableList<String> = ArrayList()
        for (field in javaClass.declaredFields) {
            if (field.isAnnotationPresent(SettingValue::class.java)) {
                val settingValue = field.getAnnotation(
                    SettingValue::class.java
                )
                if (!editable) continue
                var name = settingValue.name
                if (name == "") name = field.name
                stringList.add(name)
            }
        }
        return stringList
    }

    fun getSettingValues(editable: Boolean): Map<String, List<String>> {
        val stringList: MutableMap<String, List<String>> = HashMap()
        for (field in javaClass.declaredFields) {
            if (field.isAnnotationPresent(SettingValue::class.java)) {
                val settingValue = field.getAnnotation(
                    SettingValue::class.java
                )
                if (editable && !settingValue.editable) continue
                var name = settingValue.name
                var possibleValues: MutableList<String> = ArrayList(listOf(*settingValue.values))
                if (possibleValues.isEmpty()) {
                    // ENUM
                    if (field.isEnumConstant) {
                        possibleValues = Arrays.stream(field.javaClass.enumConstants).map { s: Field ->
                            try {
                                return@map s[this].toString()
                            } catch (e: IllegalAccessException) {
                                return@map null
                            }
                        }.collect(Collectors.toList())
                    }
                    // Boolean
                    if (Boolean::class.javaPrimitiveType == field.type) {
                        possibleValues.add("true")
                        possibleValues.add("false")
                    }
                }
                if (name == "") name = field.name
                stringList[name] = possibleValues
            }
        }
        return stringList
    }

    fun getSettingValuesAsync(editable: Boolean): Map<String, List<String>> {
        val ob = ThreadUtils.runFunctionListAsync(
            listOf(*javaClass.declaredFields),
            Function<Field, Pair<String, List<String>>?> { field: Field ->
                if (field.isAnnotationPresent(SettingValue::class.java)) {
                    val settingValue = field.getAnnotation(SettingValue::class.java)
                    if (!settingValue.editable && editable) return@Function null
                    var name = settingValue.name
                    var possibleValues: MutableList<String> = ArrayList(listOf(*settingValue.values))
                    if (possibleValues.isEmpty()) {
                        // ENUM
                        if (field.isEnumConstant) {
                            possibleValues = Arrays.stream(field.javaClass.enumConstants).map { s: Field ->
                                try {
                                    return@map s[this].toString()
                                } catch (e: IllegalAccessException) {
                                    return@map null
                                }
                            }.collect(Collectors.toList())


//                        Arrays.stream(constants).forEach(field1 -> {
//                            try {
//                                possibleValues.add(field1.get(this).toString());
//
//                            } catch (IllegalAccessException e) {
//                                e.printStackTrace();
//                            }
//                        });
//                        System.out.println("Enum values: " + possibleValues);
                        }
                        // Boolean
                        if (Boolean::class.javaPrimitiveType == field.type) {
                            possibleValues.add("true")
                            possibleValues.add("false")
                        }
                    }
                    if (name == "") name = field.name
                    return@Function ImmutablePair<String, List<String>>(name, possibleValues)
                }
                null
            })

        // 51 ms parallel
        try {
            ob.runThreads(ThreadUtils.ThreadExecutors.CACHED_THREADS.executorService)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
        val values = ob.getValuesAndAwait(10)
        val returnValues: MutableMap<String, List<String>> = HashMap()
        assert(values != null)
        for (field in values!!.keys) {
            val pair = values[field]
            returnValues[pair!!.key] = pair.right
        }
        return returnValues
    }

    fun setValue(key: String, `val`: String) {
        for (field in javaClass.declaredFields) {
            if (field.isAnnotationPresent(SettingValue::class.java)) {
                val settingValue = field.getAnnotation(
                    SettingValue::class.java
                )
                var name = settingValue.name
                if (name == "") name = field.name
                if (name.equals(key, ignoreCase = true)) {
                    try {
                        require(settingValue.editable) { "You cannot edit a value which is not editable" }
                        var wrappedVal: Any
                        require(!(!field.type.isPrimitive && !String::class.java.isAssignableFrom(field.type))) { "Setting value is not primitive type or string which is not supported." }
                        if (Boolean::class.javaPrimitiveType == field.type || Boolean::class.java.isAssignableFrom(field.type)) {
                            wrappedVal = java.lang.Boolean.parseBoolean(`val`)
                            require(
                                wrappedVal.toString().equals(`val`, ignoreCase = true)
                            ) { "Value cannot be $`val` must be true/false" }
                        } else if (Int::class.javaPrimitiveType == field.type || Int::class.java.isAssignableFrom(field.type)) {
                            wrappedVal = `val`.toInt()
                        } else if (Long::class.javaPrimitiveType == field.type || Long::class.java.isAssignableFrom(
                                field.type
                            )
                        ) {
                            wrappedVal = `val`.toLong()
                        } else if (Double::class.javaPrimitiveType == field.type || Double::class.java.isAssignableFrom(
                                field.type
                            )
                        ) {
                            wrappedVal = `val`.toDouble()
                        } else if (Short::class.javaPrimitiveType == field.type || Short::class.java.isAssignableFrom(
                                field.type
                            )
                        ) {
                            wrappedVal = `val`.toShort()
                        } else if (String::class.java == field.type || String::class.java.isAssignableFrom(field.type)) {
                            wrappedVal = `val`
                        } else {
                            throw IllegalArgumentException(
                                "Value must be of type " + field.type
                                        + " e.g if boolean then true or a number if int"
                            )
                        }
                        Validate.notNull(wrappedVal)

                        field[this] = wrappedVal
                        return
                    } catch (e: IllegalAccessException) {
                        e.printStackTrace()
                    }
                }
            }
        }
        throw IllegalArgumentException("No such value named $key found")
    }

    fun setValue(key: String, `val`: Any?) {
        for (field in javaClass.declaredFields) {
            if (field.isAnnotationPresent(SettingValue::class.java)) {
                val settingValue = field.getAnnotation(
                    SettingValue::class.java
                )
                var name = settingValue.name
                if (name == "") name = field.name
                if (name.equals(key, ignoreCase = true)) {
                    try {
                        require(settingValue.editable) { "You cannot edit a value which is not editable" }

                        field[this] = `val`
                        return
                    } catch (e: IllegalAccessException) {
                        e.printStackTrace()
                    }
                }
            }
        }
        throw IllegalArgumentException("No such value named $key found")
    }

    fun getValue(key: String): Any {
        for (field in javaClass.declaredFields) {
            if (field.isAnnotationPresent(SettingValue::class.java)) {
                val settingValue = field.getAnnotation(
                    SettingValue::class.java
                )
                var name = settingValue.name
                if (name == "") name = field.name
                if (name.equals(key, ignoreCase = true)) {
                    try {
                        return field[this]
                    } catch (e: IllegalAccessException) {
                        e.printStackTrace()
                    }
                }
            }
        }
        throw IllegalArgumentException("No such value named $key found")
    }

    companion object {
        private val emptyObject = arrayOfNulls<Any>(0)
    }
}