package com.formssi.generator.util;

import com.formssi.common.core.exception.ConstructionException;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Properties;

/**
 * @author lizhangyu
 * @version 1.0
 * @date 2024/11/7 9:04
 */
public class ClassUtil {

    private static final ClassLoader systemClassLoader = ClassLoader.getSystemClassLoader();

    public ClassUtil() {
    }


    public static Class<?> loadClass(String className) {
        return loadClass((ClassLoader)null, className);
    }

    public static Class<?> loadClass(ClassLoader classLoader, String className) {
        try {
            return classLoader == null ? Class.forName(className) : classLoader.loadClass(className);
        } catch (NoClassDefFoundError | ClassNotFoundException var3) {
            return null;
        }
    }

    public static <T> T tryInstance(ClassLoader classLoader, String className) {
        return tryInstance(classLoader, className, (Properties)null);
    }

    public static <T> T tryInstance(ClassLoader classLoader, String className, Properties prop) {
        Class<?> clz = loadClass(classLoader, className);
        return tryInstance(clz, prop);
    }

    public static <T> T tryInstance(Class<?> clz, Properties prop) {
        if (clz == null) {
            return null;
        } else {
            try {
                return newInstance(clz, prop);
            } catch (Exception var3) {
                Exception e = var3;
                throw new IllegalStateException(e);
            }
        }
    }

    public static <T> T newInstance(Class<?> clz) throws ConstructionException {
        return newInstance((Class)clz, (Properties)null);
    }

    public static <T> T newInstance(Class<?> clz, Properties prop) throws ConstructionException {
        try {
            return prop == null ? (T) clz.getDeclaredConstructor().newInstance() : (T) clz.getConstructor(Properties.class).newInstance(prop);
        } catch (Exception var3) {
            Exception e = var3;
            throw new ConstructionException(e);
        }
    }

    public static Class<?> getTypeClass(Type type) {
        if (type instanceof Class) {
            return (Class)type;
        } else {
            return type instanceof ParameterizedType ? getTypeClass(((ParameterizedType)type).getRawType()) : Object.class;
        }
    }
}
