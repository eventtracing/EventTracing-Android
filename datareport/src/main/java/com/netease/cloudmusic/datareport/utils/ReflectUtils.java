package com.netease.cloudmusic.datareport.utils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ReflectUtils {

    /**
     * 获取某个类实例的成员变量
     */
    public static Object getField(Class<?> clazz, String fieldName, Object instance) {
        Field field;
        try {
            field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(instance);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 获取某个类的静态成员变量
     */
    public static Object getField(Class<?> clazz, String fieldName) {
        Field field;
        try {
            field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(null);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


//=========================== 针对 AbsListView 的优化 =========================

    public static final String TAG = "ReflectionUtil";

    private static Method sForNameMethod;
    private static Method sGetFieldMethod;

    static {
        try {
            sForNameMethod = Class.class.getDeclaredMethod("forName", String.class);
            sGetFieldMethod = Class.class.getDeclaredMethod("getDeclaredField", String.class);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public static Object getListField(Class<?> clazz, String fieldName, Object instance) {
        Field field;
        Object value;
        try {
            field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);
            value = field.get(instance);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            value = getFieldObj(instance, clazz.getName(), fieldName, null);
        }
        return value;
    }

    private static Object getFieldObj(Object src, String clzName, String filedName, Object defObj) {
        Object result = new Object();
        if (canReflection()) {
            try {
                Class<?> clz = (Class<?>) sForNameMethod.invoke(null, clzName);
                Field field = (Field) sGetFieldMethod.invoke(clz, filedName);
                if (field != null) {
                    field.setAccessible(true);
                    result = field.get(src);
                }
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }

        return result;
    }

    private static boolean canReflection() {
        boolean canReflection = true;
        if (sForNameMethod == null || sGetFieldMethod == null) {
            canReflection = false;
        }
        return canReflection;
    }
}
