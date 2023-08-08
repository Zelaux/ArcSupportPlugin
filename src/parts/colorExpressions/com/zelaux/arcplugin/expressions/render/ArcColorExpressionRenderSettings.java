package com.zelaux.arcplugin.expressions.render;

import arc.struct.ObjectMap;
import arc.struct.Seq;
import com.intellij.ui.colorpicker.ColorPickerBuilderKt;
import com.zelaux.arcplugin.expressions.resolve.ArcColorExpression;
import com.zelaux.arcplugin.expressions.resolve.ArcColorExpressionSequence;
import com.zelaux.arcplugin.settings.MySettingsState;
import org.jetbrains.annotations.Nullable;

import java.awt.*;

public class ArcColorExpressionRenderSettings {
    public final ArcColorExpressionSequence sequence;
    public final boolean isWritable;
    private final ObjectMap<ArcColorExpression, Runnable> listeners = new ObjectMap<>();
    private final Seq<Runnable> staticListeners = new Seq<>();

    public ArcColorExpressionRenderSettings(ArcColorExpressionSequence sequence, boolean isWritable) {
        this.sequence = sequence;
        this.isWritable = isWritable;
    }

    public Dimension calculateSliderSize() {
        if (isListView())
            return new Dimension(ColorPickerBuilderKt.PICKER_PREFERRED_WIDTH, 150 / 4);
        else {
            return new Dimension(ColorPickerBuilderKt.PICKER_PREFERRED_WIDTH, 150 / 2);
        }
    }

    public ArcColorExpressionSequence sequence() {
        return sequence;
    }

    public boolean isWritable() {
        return isWritable;
    }

    public void registerListener(@Nullable ArcColorExpression expression, Runnable runnable) {
        if (!isWritable()) return;
        if (expression == null) {
            staticListeners.add(runnable);
        } else {
            listeners.put(expression, runnable);
        }
    }

    public void fireUpdate(@Nullable ArcColorExpression expression) {
        for (ObjectMap.Entry<ArcColorExpression, Runnable> listener : listeners) {
            if (listener.key == expression) continue;
            listener.value.run();
        }
        staticListeners.each(Runnable::run);
    }

    public boolean isListView() {
        return MySettingsState.getInstance().viewColorExprSeqAsList;
    }
}
