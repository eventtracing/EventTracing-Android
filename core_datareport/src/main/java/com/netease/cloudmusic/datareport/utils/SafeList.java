package com.netease.cloudmusic.datareport.utils;

import java.util.ArrayList;

/**
 * 一个在get/set时会自动填充容量的list，使用者可以直接get/set任意位置的对象
 */
@SuppressWarnings("AlibabaAbstractClassShouldStartWithAbstractNaming")
public abstract class SafeList<T> extends ArrayList<T> {

    public SafeList(int initialCapacity) {
        super(initialCapacity);
    }

    @Override
    public T get(int index) {
        ensureElement(index + 1);
        return super.get(index);
    }

    @Override
    public T set(int index, T element) {
        ensureElement(index + 1);
        return super.set(index, element);
    }

    private void ensureElement(int count) {
        while (size() < count) {
            add(initValue());
        }
    }

    /**
     * 当需要自动填充容量时，使用这个函数返回的对象填充
     *
     * @return 默认的元素
     */
    public abstract T initValue();
}
