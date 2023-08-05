package com.zelaux.arcplugin.utils;

import java.util.function.Supplier;

public class LazyValue<T> implements Supplier<T> {
    protected static final Object UNSET = new Object();
    protected final Object lock = new Object();
    protected final Supplier<T> init;
    protected volatile Object value = UNSET;

    public LazyValue(Supplier<T> init) {
        this.init = init;
    }

    public static <T> LazyValue<T> create(Supplier<T> init) {
        return new LazyValue<>(init);
    }

    public void reset() {
        if (value != UNSET) {
            synchronized (lock) {
                value = UNSET;
            }
        }
    }

    @Override
    public T get() {
        Object _v1 = value;
        if (_v1 == UNSET) {
            synchronized (lock) {
                Object _v2 = value;
                if (_v2 == UNSET) {
                    value = init.get();
                }
            }
        }
        //noinspection unchecked
        return (T) value;
    }
}
