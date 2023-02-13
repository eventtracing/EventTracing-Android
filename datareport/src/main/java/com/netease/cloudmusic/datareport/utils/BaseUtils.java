package com.netease.cloudmusic.datareport.utils;

import java.util.Collection;
import java.util.Map;

public class BaseUtils {

    public static <T> T nullAs(T obj, T def) {
        return obj == null ? def : obj;
    }

    public static <T> int size(Collection<T> list) {
        return list == null ? 0 : list.size();
    }

    public static boolean isEmpty(Map<?, ?> map) {
        return map == null || map.isEmpty();
    }


    public static boolean isEmpty(Collection collection) {
        return collection == null || collection.isEmpty();
    }

}
