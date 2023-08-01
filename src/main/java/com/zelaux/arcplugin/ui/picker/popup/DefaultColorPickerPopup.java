package com.zelaux.arcplugin.ui.picker.popup;

import com.intellij.ide.*;
import com.intellij.openapi.util.*;
import com.intellij.openapi.wm.*;
import com.intellij.ui.*;
import com.intellij.ui.colorpicker.*;
import com.intellij.ui.picker.*;
import com.zelaux.arcplugin.ui.colorpicker.*;
import org.jetbrains.annotations.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

public class DefaultColorPickerPopup extends ColorPickerPopup<ColorListener>{
    private static final DefaultColorPickerPopup instance = new DefaultColorPickerPopup();
    public static DefaultColorPickerPopup instance(Color color, boolean hasAlpha){
        return (DefaultColorPickerPopup)instance.set(hasAlpha, color);
    }
    private DefaultColorPickerPopup(){

        super("ColorChooser.RecentColors");
    }

    @Override
    protected void getColorChanged(@NotNull ColorListener listener, Color color, Object source){
        listener.colorChanged(color, source);
    }

    @Override
    protected void showDialog(@NotNull ColorListener listener){
        Color color = ColorPicker.showDialog(IdeFocusManager.getGlobalInstance().getFocusOwner(), IdeBundle.message("dialog.title.choose.color"),
        currentColor, showAlpha, null, showAlphaAsPercent);
        if(color != null){
            listener.colorChanged(color, null);
        }
    }

    @Override
    protected @NotNull ColorPickerJPanelBuilder colorPickerBuilder(Ref<LightCalloutPopup> ref, ColorListener inputData){
        List<Color> recentColors = recentColorsComponent.getRecentColors();
        ColorPickerJPanelBuilder builder = new ColorPickerJPanelBuilder(this.showAlpha, this.showAlphaAsPercent);
        builder
        .setOriginalColor(currentColor)
        .addSaturationBrightnessComponent()
        .addColorAdjustPanel(new MaterialGraphicalColorPipetteProvider())
        .addColorValuePanel().withFocus();
        if(!recentColors.isEmpty()){
            builder/*.addSeparator()*/
            .addCustomComponent(colorPickerModel -> new RecentColorsPalette(colorPickerModel, recentColors));
        }
        builder
        .addColorListener((color, source) -> updatePointer(ref), true)
        .addColorListener((color, source) -> recentColorsComponent.saveRecentColors(recentColorsComponent.appendColor(color, recentColors, 20)), false)
        .focusWhenDisplay(true)
        .setFocusCycleRoot(true)
        .addKeyAction(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), cancelPopup(ref))
        .addKeyAction(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), applyColor(ref));
        return builder;
    }

}
