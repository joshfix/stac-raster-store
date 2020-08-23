package com.joshfix.stac.utility;

/**
 * @author joshfix
 */
public class PropertyResolver {

    public static String getPropertyValue(String key, String defaultValue) {

        String value = System.getenv(key);
        if (null != value) {
            return value;
        }
        value = System.getProperty(key);
        if (null != value) {
            return value;
        }

        return defaultValue;
    }

}