package com.formssi.workflow.utils;

import java.util.Map;

public class TypeSafeUtils {
    public static Integer safeGetInteger(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return (value instanceof Integer) ? (Integer) value : null;
    }

    public static String safeGetString(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return (value instanceof String) ? (String) value : null;
    }

    public static String safeGetNestedString(Map<String, Object> map, String parentKey, String childKey) {
        Object parent = map.get(parentKey);
        if (parent instanceof Map) {
            return safeGetString((Map<String, Object>) parent, childKey);
        }
        return null;
    }
}
