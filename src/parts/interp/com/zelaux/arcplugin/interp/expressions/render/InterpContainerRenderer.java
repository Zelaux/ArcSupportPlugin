package com.zelaux.arcplugin.interp.expressions.render;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Ref;
import com.zelaux.arcplugin.PluginIcons;
import com.zelaux.arcplugin.expressions.render.ExpressionSequenceRenderer;
import com.zelaux.arcplugin.expressions.resolve.ExpressionSequence;
import com.zelaux.arcplugin.interp.expressions.resolve.InterpContainer;
import com.zelaux.arcplugin.ui.picker.popup.SimplePopup;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

public class InterpContainerRenderer implements ExpressionSequenceRenderer<InterpContainerRenderer> {
    public final InterpContainer self;

    public InterpContainerRenderer(InterpContainer self) {
        this.self = self;
    }

    @Override
    public Icon getIcon() {
        return PluginIcons.Graph;
    }


    @NotNull
    @Override
    public Color getIconColor() {
        return java.awt.Color.white;
    }

    @Override
    public void showPopup(Project project, Editor editor, boolean writable) {
        SimplePopup.lambdaPopup.show(project, editor, it ->
                self.myExpression.createRenderer().getTabComponent(project, it, Ref.create(new InterpExpressionRenderSettings(writable)))
        );
    }

    @Override
    public Icon mergeIcons(MyTuple<ExpressionSequence<?>, InterpContainerRenderer, Color>[] array) {
        return getIcon();
    }
}
