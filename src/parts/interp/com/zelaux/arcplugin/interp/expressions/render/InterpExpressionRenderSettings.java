package com.zelaux.arcplugin.interp.expressions.render;

import arc.struct.ObjectMap;
import arc.struct.Seq;
import com.intellij.ui.colorpicker.ColorPickerBuilderKt;
import com.zelaux.arcplugin.expressions.resolve.ArcColorExpression;
import com.zelaux.arcplugin.expressions.resolve.ArcColorExpressionSequence;
import com.zelaux.arcplugin.settings.ArcPluginSettingsState;
import org.jetbrains.annotations.Nullable;

import java.awt.*;

public class InterpExpressionRenderSettings {
    public final boolean isWritable;

    public InterpExpressionRenderSettings( boolean isWritable) {
        this.isWritable = isWritable;
    }

    public boolean isWritable() {
        return isWritable;
    }

}
