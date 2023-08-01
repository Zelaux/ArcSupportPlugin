package com.zelaux.arcplugin.ui.picker.popup;

import com.intellij.ide.*;
import com.intellij.openapi.util.*;
import com.intellij.openapi.wm.*;
import com.intellij.psi.*;
import com.intellij.ui.*;
import com.intellij.ui.colorpicker.*;
import com.intellij.ui.picker.*;
import com.zelaux.arcplugin.marker.result.*;
import com.zelaux.arcplugin.ui.colorpicker.*;
import com.zelaux.arcplugin.utils.*;
import org.jetbrains.annotations.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class GradientColorPickerPopup extends ColorPickerPopup<Pair<GradientColorViewer, PsiElement>>{
    private static final GradientColorPickerPopup instance = new GradientColorPickerPopup();

    protected GradientColorPickerPopup(){
        super("colorChooserColorsKey");
    }

    public static GradientColorPickerPopup instance(Color color, boolean hasAlpha){
        return (GradientColorPickerPopup)instance.set(hasAlpha, color);
    }

    @Override
    protected void getColorChanged(@NotNull Pair<GradientColorViewer, PsiElement> elementPair, Color color, Object source){
        GradientColorViewer listener = elementPair.first;
        listener.apply(elementPair.second, ColorUtils.progress(color, listener.colorA, listener.colorB), color.getAlpha());
    }

    @Override
    protected void showDialog(@NotNull Pair<GradientColorViewer, PsiElement> listener){

        Color color = ColorPicker.showDialog(IdeFocusManager.getGlobalInstance().getFocusOwner(), IdeBundle.message("dialog.title.choose.color"),
        currentColor, showAlpha, null, showAlphaAsPercent);
        if(color != null){
            getColorChanged(listener, color, null);
        }
    }

    @Override
    protected @NotNull ColorPickerJPanelBuilder colorPickerBuilder(Ref<LightCalloutPopup> ref, Pair<GradientColorViewer, PsiElement> inputData){
        GradientColorViewer gradientColorViewer = inputData.first;


        ColorPickerJPanelBuilder builder = new ColorPickerJPanelBuilder(showAlpha, showAlphaAsPercent);
        builder
        .setOriginalColor(currentColor)
//        .addSaturationBrightnessComponent()
        .addCustomComponent(model -> new ColorPickerGradientComponent(model, gradientColorViewer.colorA, gradientColorViewer.colorB))
//        .addColorAdjustPanel(new MaterialGraphicalColorPipetteProvider())
        .addCustomComponent(it -> new GrayScaleColorAdjustPanel(it, new MaterialGraphicalColorPipetteProvider(), showAlpha))
        .addColorValuePanel().withFocus();
        builder.addColorListener((color, source) -> updatePointer(ref), true)
        .focusWhenDisplay(true)
        .setFocusCycleRoot(true)
        .addKeyAction(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), cancelPopup(ref))
        .addKeyAction(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), applyColor(ref))
        ;
        return builder;
    }
}
