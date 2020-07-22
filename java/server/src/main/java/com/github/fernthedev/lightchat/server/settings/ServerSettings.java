package com.github.fernthedev.lightchat.server.settings;

import com.github.fernthedev.fernutils.thread.ThreadUtils;
import com.github.fernthedev.fernutils.thread.multiple.TaskInfoFunctionList;
import com.github.fernthedev.lightchat.core.CoreSettings;
import com.github.fernthedev.lightchat.core.encryption.codecs.CodecEnum;
import lombok.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

@EqualsAndHashCode(callSuper = true)
@Getter
@Setter
@Data
@ToString
public class ServerSettings extends CoreSettings {

    @SettingValue
    private int port = 2000;

    @SettingValue
    private String password = "password";

    @SettingValue
    private int rsaKeySize = 4096;

    @SettingValue(name = "multicast")
    private boolean useMulticast = false;

    @SettingValue(name = "usepassword")
    private boolean passwordRequiredForLogin = false;

    @SettingValue(name = "usenativetransport")
    private boolean useNativeTransport = true;

    @SettingValue(name = "codec")
    protected CodecEnum jsonCodec = DEFAULT_CODEC;

    @Deprecated
    public void setNewValue(@NonNull String oldValue, @NonNull String newValue) {

        Object value = newValue;

        switch (newValue.toLowerCase()) {
            case "true":
                value = true;
                break;
            case "false":
                value = false;
                break;
        }

        if (StringUtils.isNumeric(newValue)) {
            try {
                value = Integer.parseInt(newValue);
            } catch (NumberFormatException ignored) {
                throw new IllegalArgumentException("Incorrect integer value");
            }
        }

        if (newValue.equals("") || oldValue.equals("")) {
            throw new IllegalArgumentException("Values cannot be empty");
        }


        switch (oldValue.toLowerCase()) {
            case "password":
                setPassword((String) value);
                break;
            default:
                throw new IllegalArgumentException("No such value named " + oldValue + " found");
        }

    }

    /**
     * @deprecated Use {@link #getSettingValues(boolean)} or {@link #getSettingValuesAsync(boolean)} instead
     */
    @Deprecated
    public List<String> getSettingNames(boolean editable) {
        List<String> stringList = new ArrayList<>();
        for (Field field : getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(SettingValue.class)) {
                SettingValue settingValue = field.getAnnotation(SettingValue.class);

                if (!editable) continue;

                String name = settingValue.name();

                if (name.equals("")) name = field.getName();


                stringList.add(name);
            }
        }

        return stringList;
    }

    public Map<String, List<String>> getSettingValues(boolean editable) {
        Map<String, List<String>> stringList = new HashMap<>();

        for (Field field : getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(SettingValue.class)) {
                SettingValue settingValue = field.getAnnotation(SettingValue.class);

                if (editable && !settingValue.editable()) continue;

                String name = settingValue.name();
                List<String> possibleValues = new ArrayList<>(Arrays.asList(settingValue.values()));

                if (possibleValues.isEmpty()) {
                    // ENUM
                    if (field.isEnumConstant()) {
                        possibleValues = Arrays.stream(field.getClass().getEnumConstants()).map(s -> {
                            try {
                                return s.get(this).toString();
                            } catch (IllegalAccessException e) {
                                return null;
                            }
                        }).collect(Collectors.toList());


//                        Arrays.stream(constants).forEach(field1 -> {
//                            try {
//                                possibleValues.add(field1.get(this).toString());
//
//                            } catch (IllegalAccessException e) {
//                                e.printStackTrace();
//                            }
//                        });
                    }
                    // Boolean
                    if (boolean.class.equals(field.getType())) {
                        possibleValues.add("true");
                        possibleValues.add("false");
                    }
                }


                if (name.equals("")) name = field.getName();


                stringList.put(name, possibleValues);
            }
        }



        return stringList;
    }

    public Map<String, List<String>> getSettingValuesAsync(boolean editable) {
        TaskInfoFunctionList<Field, Pair<String, List<String>>> ob = ThreadUtils.runFunctionListAsync(Arrays.asList(getClass().getDeclaredFields()), (field -> {


            if (field.isAnnotationPresent(SettingValue.class)) {
                SettingValue settingValue = field.getAnnotation(SettingValue.class);

                if (!settingValue.editable() && editable) return null;

                String name = settingValue.name();

                List<String> possibleValues = new ArrayList<>(Arrays.asList(settingValue.values()));


                if (possibleValues.isEmpty()) {
                    // ENUM
                    if (field.isEnumConstant()) {
                        possibleValues = Arrays.stream(field.getClass().getEnumConstants()).map(s -> {
                            try {
                                return s.get(this).toString();
                            } catch (IllegalAccessException e) {
                                return null;
                            }
                        }).collect(Collectors.toList());


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
                    if (boolean.class.equals(field.getType())) {
                        possibleValues.add("true");
                        possibleValues.add("false");
                    }
                }


                if (name.equals("")) name = field.getName();
                return new ImmutablePair<>(name, possibleValues);
            }
            return null;
        }));

        // 51 ms parallel
        try {
            ob.runThreads(ThreadUtils.ThreadExecutors.CACHED_THREADS.getExecutorService());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Map<Field, Pair<String, List<String>>> values = ob.getValuesAndAwait(10);


        Map<String, List<String>> returnValues = new HashMap<>();

        assert values != null;
        for (Field field : values.keySet()) {
            Pair<String, List<String>> pair = values.get(field);
            returnValues.put(pair.getKey(), pair.getRight());
        }

        return returnValues;
    }

    public void setValue(@NonNull String key, String val) {
        for (Field field : getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(SettingValue.class)) {
                SettingValue settingValue = field.getAnnotation(SettingValue.class);

                String name = settingValue.name();

                if (name.equals("")) name = field.getName();

                if (name.equalsIgnoreCase(key)) {
                    try {

                        if (!settingValue.editable()) {
                            throw new IllegalArgumentException("You cannot edit a value which is not editable");
                        }

                        Object wrappedVal;

                        if (!field.getType().isPrimitive() && !String.class.isAssignableFrom(field.getType())) {
                            throw new IllegalArgumentException("Setting value is not primitive type or string which is not supported.");
                        }

                        if (boolean.class.equals(field.getType()) || Boolean.class.isAssignableFrom(field.getType())) {

                            wrappedVal = Boolean.parseBoolean(val);
                            if (!wrappedVal.toString().equalsIgnoreCase(val)) throw new IllegalArgumentException("Value cannot be " + val + " must be true/false");

                        } else if (int.class.equals(field.getType()) || Integer.class.isAssignableFrom(field.getType())) {
                            wrappedVal = Integer.parseInt(val);
                        } else if (long.class.equals(field.getType()) || Long.class.isAssignableFrom(field.getType())) {
                            wrappedVal = Long.parseLong(val);
                        } else if (double.class.equals(field.getType()) || Double.class.isAssignableFrom(field.getType())) {
                            wrappedVal = Double.parseDouble(val);
                        } else if (short.class.equals(field.getType()) || Short.class.isAssignableFrom(field.getType())) {
                            wrappedVal = Short.parseShort(val);
                        } else if (String.class.equals(field.getType()) || String.class.isAssignableFrom(field.getType())) {
                            wrappedVal = val;
                        } else {
                            throw new IllegalArgumentException("Value must be of type " + field.getType()
                                    + " e.g if boolean then true or a number if int");
                        }

                        Validate.notNull(wrappedVal);

//                        if(field.getDeclaringClass().isInstance(val.getClass())) {
                        field.set(this, wrappedVal);
                        return;
//                        } else {
//                            throw new IllegalArgumentException("Value has to be ")
//                        }
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        throw new IllegalArgumentException("No such value named " + key + " found");
    }

    public void setValue(@NonNull String key, Object val) {
        for (Field field : getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(SettingValue.class)) {
                SettingValue settingValue = field.getAnnotation(SettingValue.class);

                String name = settingValue.name();

                if (name.equals("")) name = field.getName();

                if (name.equalsIgnoreCase(key)) {
                    try {

                        if (!settingValue.editable()) {
                            throw new IllegalArgumentException("You cannot edit a value which is not editable");
                        }

//                        if(field.getDeclaringClass().isInstance(val.getClass())) {
                        field.set(this, val);
                        return;
//                        } else {
//                            throw new IllegalArgumentException("Value has to be ")
//                        }
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        throw new IllegalArgumentException("No such value named " + key + " found");
    }

    public Object getValue(@NonNull String key) {
        for (Field field : getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(SettingValue.class)) {
                SettingValue settingValue = field.getAnnotation(SettingValue.class);

                String name = settingValue.name();

                if (name.equals("")) name = field.getName();

                if (name.equalsIgnoreCase(key)) {
                    try {
                        return field.get(this);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        throw new IllegalArgumentException("No such value named " + key + " found");

//        switch (key.toLowerCase()) {
//            case "password":
//                return getPassword();
//            case "usemulticast":
//                return isUseMulticast();
//            case "passwordlogin":
//                return isPasswordRequiredForLogin();
//            default:
//                return null;
//        }
    }

    private static final Object[] emptyObject = new Object[0];


    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD})
    public @interface SettingValue {
        @NonNull String name() default "";

        boolean editable() default true;

        @NonNull String[] values() default {};
    }
}
