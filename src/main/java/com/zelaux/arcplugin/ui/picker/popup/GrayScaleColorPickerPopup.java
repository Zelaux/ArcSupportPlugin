package com.zelaux.arcplugin.ui.picker.popup;

import com.intellij.ide.*;
import com.intellij.openapi.util.*;
import com.intellij.openapi.wm.*;
import com.intellij.ui.*;
import com.intellij.ui.colorpicker.*;
import com.intellij.ui.picker.*;
import com.zelaux.arcplugin.ui.colorpicker.*;
import com.zelaux.arcplugin.ui.picker.listeners.*;
import org.jetbrains.annotations.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

public class GrayScaleColorPickerPopup extends ColorPickerPopup<GrayScaleColorListener>{
    private static final GrayScaleColorPickerPopup instance = new GrayScaleColorPickerPopup();
    public static GrayScaleColorPickerPopup instance(Color color, boolean hasAlpha){
        return (GrayScaleColorPickerPopup)instance.set(hasAlpha, color);
    }

    private GrayScaleColorPickerPopup(){

        super("GrayScaleColorChooser.RecentColors");
    }

    @Override
    protected void showDialog(@NotNull GrayScaleColorListener listener){
        Color color = ColorPicker.showDialog(IdeFocusManager.getGlobalInstance().getFocusOwner(), IdeBundle.message("dialog.title.choose.color"),
        currentColor, showAlpha, null, showAlphaAsPercent);
        if(color != null){
            getColorChanged(listener, color, null);
        }
    }

    @Override
    protected void getColorChanged(@NotNull GrayScaleColorListener listener, Color color, Object source){
        listener.colorChanged(color.getRed() / 255f, color.getAlpha() / 255f, source);
    }

    @Override
    protected @NotNull ColorPickerJPanelBuilder colorPickerBuilder(Ref<LightCalloutPopup> ref, GrayScaleColorListener inputData){
//        List<Color> recentColors = recentColorsComponent.getRecentColors();
        ColorPickerJPanelBuilder builder = new ColorPickerJPanelBuilder(showAlpha, showAlphaAsPercent);
        builder
        .setOriginalColor(currentColor)
//        .addSaturationBrightnessComponent()
        .addCustomComponent(BrightnessComponent::new)
//        .addColorAdjustPanel(new MaterialGraphicalColorPipetteProvider())
        .addCustomComponent(it -> new GrayScaleColorAdjustPanel(it, new MaterialGraphicalColorPipetteProvider(), showAlpha))
        .addColorValuePanel().withFocus();
       /* if(!recentColors.isEmpty()){
            builder*//*.addSeparator()*//*
            .addCustomComponent(colorPickerModel -> new RecentColorsPalette(colorPickerModel, recentColors));
        }*/
        builder
        .addColorListener((color, source) -> updatePointer(ref), true)
//        .addColorListener((color, source) -> recentColorsComponent.saveRecentColors(recentColorsComponent.appendColor(color, recentColors, 20)), false)
        .focusWhenDisplay(true)
        .setFocusCycleRoot(true)
        .addKeyAction(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), cancelPopup(ref))
        .addKeyAction(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), applyColor(ref));
        return builder;
    }

}
