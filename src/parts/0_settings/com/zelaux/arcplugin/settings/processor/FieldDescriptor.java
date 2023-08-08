package com.zelaux.arcplugin.settings.processor;

import org.jetbrains.annotations.NotNull;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.util.Objects;

public class FieldDescriptor {
    private static final MethodHandles.Lookup lookup = MethodHandles.lookup();
    @NotNull
    public final Field field;
    @NotNull
    public final SettingField annotation;

    public final boolean separatorBefore;
    public final boolean separatorAfter;
    @NotNull
    public final MethodHandle getter;
    @NotNull
    public final MethodHandle setter;
    public final String name;
    public Object defaultValue;

    public FieldDescriptor(@NotNull Field field, @NotNull SettingField annotation) {

        this.field = Objects.requireNonNull(field);
        this.name = field.getName();
        field.setAccessible(true);
        Separator separator = field.getAnnotation(Separator.class);
        if (separator != null) {
            separatorBefore = separator.before();
            separatorAfter = !separator.before();
        } else {
            separatorBefore = separatorAfter = false;
        }
        try {
            this.getter = lookup.unreflectGetter(field);
            this.setter = lookup.unreflectSetter(field);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        this.annotation = Objects.requireNonNull(annotation);
    }

    public Object get(Object object) {
        try {
            return getter.invoke(object);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public void set(Object object, Object value) {
        try {
            setter.invoke(object, value);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public void setDefault(Object object) {
        defaultValue = get(object);
    }

    public void reset(Object object) {
        set(object, defaultValue);
    }
}
