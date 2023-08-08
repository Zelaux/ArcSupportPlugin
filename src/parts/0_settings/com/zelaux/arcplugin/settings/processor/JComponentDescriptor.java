package com.zelaux.arcplugin.settings.processor;


import com.intellij.util.Function;

import javax.swing.*;
import java.util.function.BiConsumer;

public class JComponentDescriptor {
    public final JComponent component;
    public final Function<JComponent, Object> getter;
    public final BiConsumer<JComponent, Object> setter;

    @SuppressWarnings("unchecked")
    public <T extends JComponent> JComponentDescriptor(T component, Function<T, Object> getter, BiConsumer<T, Object> setter) {
        this.component = component;
        this.getter = (Function<JComponent, Object>) getter;
        this.setter = (BiConsumer<JComponent, Object>) setter;
    }

    public Object get() {
        return getter.fun(component);
    }

    public void set(Object value) {
        setter.accept(component, value);
    }

    @Override
    public int hashCode() {
        return component.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof JComponentDescriptor) {
            return ((JComponentDescriptor) obj).component.equals(component);
        }
        return false;
    }
}
