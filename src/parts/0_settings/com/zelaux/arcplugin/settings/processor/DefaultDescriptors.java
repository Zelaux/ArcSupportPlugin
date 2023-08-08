package com.zelaux.arcplugin.settings.processor;

import com.intellij.openapi.ui.ComboBox;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.fields.IntegerField;
import com.intellij.util.ui.CheckBox;
import javaslang.Function1;
import javaslang.Function2;

import javax.script.ScriptEngine;
import javax.swing.*;

public class DefaultDescriptors {
    static void register(ScriptEngine engine) {
        engine.put("intField", (Func2<Integer, Integer, JComponentDescriptor>) (min, max) -> {
            return new JComponentDescriptor(
                    new IntegerField("_", min, max),
                    IntegerField::getValue,
                    (param, val) -> param.setValue((Integer) val));
        });
        engine.put("checkBox", (Func0<JComponentDescriptor>) () -> {
            return new JComponentDescriptor(
                    new JBCheckBox(),
                    AbstractButton::isSelected,
                    (param, val) -> {
                        param.setSelected( (Boolean) val);
                    });
        });
        engine.put("comboBox", (Func1<Object[], JComponentDescriptor>) (objects) -> {
            return new JComponentDescriptor(new ComboBox<>(objects),
                    JComboBox::getSelectedItem,
                    JComboBox::setSelectedItem);
        });
    }

    @FunctionalInterface
    public interface Func2<P1, P2, R> {
        R apply(P1 p1, P2 p2);
    }

    @FunctionalInterface
    public interface Func1<P1, R> {
        R apply(P1 p1);
    }
    @FunctionalInterface
    public interface Func0< R> {
        R apply();
    }

    @FunctionalInterface
    public interface Func3<P1, P2, P3, R> {
        R apply(P1 p1, P2 p2, P3 p3);
    }

    public static class IntegerDescriptor {

    }
}
