package com.github.fernthedev.light;

import lombok.Data;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Data
public class Settings {

    @SettingValue
    private String password = "password";

    @SettingValue(name = "multicast")
    private boolean useMulticast = false;

    @SettingValue(name = "usepassword")
    private boolean passwordRequiredForLogin = false;

    @SettingValue(name = "usenativetransport")
    private boolean useNativeTransport = true;


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

        if(StringUtils.isNumeric( newValue)) {
            try {
                value = Integer.parseInt(newValue);
            } catch (NumberFormatException ignored) {
                throw new IllegalArgumentException("Incorrect integer value");
            }
        }

        if(newValue.equals("") || oldValue.equals("")) {
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

    public List<String> getSettingNames(boolean editable) {
        List<String> stringList = new ArrayList<>();
        for (Field field : getClass().getDeclaredFields()) {
            if(field.isAnnotationPresent(SettingValue.class)) {
                SettingValue settingValue = field.getAnnotation(SettingValue.class);

                if(!editable) continue;

                String name = settingValue.name();

                if(name.equals("")) name = field.getName();



                stringList.add(name);
            }
        }

        return stringList;
    }

    public void setValue(@NonNull String key, String val) {
        for (Field field : getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(SettingValue.class)) {
                SettingValue settingValue = field.getAnnotation(SettingValue.class);

                String name = settingValue.name();

                if (name.equals("")) name = field.getName();

                if (name.equalsIgnoreCase(key)) {
                    try {

                        if(!settingValue.editable()) {
                            throw new IllegalArgumentException("You cannot edit a value which is not editable");
                        }

                        Object wrappedVal = null;

                        if(!field.getClass().isPrimitive() && field.getClass().isInstance(String.class)) {
                            throw new IllegalArgumentException("Setting value is not primitive type or string which is not supported.");
                        }

                        if (boolean.class.equals(field.getType())) {
                            wrappedVal = Boolean.parseBoolean(val);
                        } else if (int.class.equals(field.getType())) {
                            wrappedVal = Integer.parseInt(val);
                        } else if (long.class.equals(field.getType())) {
                            wrappedVal = Long.parseLong(val);
                        } else if (double.class.equals(field.getType())) {
                            wrappedVal = Double.parseDouble(val);
                        } else if (short.class.equals(field.getType())) {
                            wrappedVal = Short.parseShort(val);
                        } else if (String.class.equals(field.getType())) {
                            wrappedVal = val;
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

                        if(!settingValue.editable()) {
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
            if(field.isAnnotationPresent(SettingValue.class)) {
                SettingValue settingValue = field.getAnnotation(SettingValue.class);

                String name = settingValue.name();

                if(name.equals("")) name = field.getName();

                if(name.equalsIgnoreCase(key)) {
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

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD})
    public @interface SettingValue {
        String name() default "";

        boolean editable() default true;
    }
}
