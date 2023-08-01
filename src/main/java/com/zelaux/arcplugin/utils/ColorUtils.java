package com.zelaux.arcplugin.utils;

import arc.math.Mathf;
import com.intellij.openapi.util.*;
import com.intellij.psi.*;
import com.zelaux.arcplugin.awt.*;
import com.zelaux.arcplugin.colorViewer.*;
import org.jetbrains.annotations.*;

import java.awt.*;
import java.util.function.*;

@SuppressWarnings("UseJBColor")
public class ColorUtils{
    @NotNull
    public static Color lerp(@NotNull Color a, @NotNull Color b, float progress){
        return new GradientColor(
        lerp(a.getRed(), b.getRed(), progress),
        lerp(a.getGreen(), b.getGreen(), progress),
        lerp(a.getBlue(), b.getBlue(), progress),
        lerp(a.getAlpha(), b.getAlpha(), progress),
        progress
        );
    }

    private static float mapComponent(@NotNull Color current, @NotNull Color a, @NotNull Color b, @NotNull Function<Color, Integer> function){
        int value = function.apply(current);
        int froma = function.apply(a);
        int toa = function.apply(b);
        if(froma == toa) return Float.POSITIVE_INFINITY;
        return Mathf.map(value, froma, toa, 0, 1);
    }

    @NotNull
    public static Color lerp(@NotNull Color a, @NotNull Color b, int alpha, float progress){
        return new GradientColor(
        lerp(a.getRed(), b.getRed(), progress),
        lerp(a.getGreen(), b.getGreen(), progress),
        lerp(a.getBlue(), b.getBlue(), progress),
        alpha, progress
        );
    }

    private static int lerp(int a, int b, float progress){
        return (int)Mathf.clamp(a + (b - a) * progress, 0f, 255f);
    }

    public static float progress(@NotNull Color color, @NotNull Color colorA, @NotNull Color colorB){
        float rProgress = mapComponent(color, colorA, colorB, Color::getRed);
        float gProgress = mapComponent(color, colorA, colorB, Color::getGreen);
        float bProgress = mapComponent(color, colorA, colorB, Color::getBlue);
        for(float progress : new float[]{rProgress, gProgress, bProgress}){
            if(progress == Float.POSITIVE_INFINITY){
                continue;
            }
            return progress;
        }
        float aProgress = mapComponent(color, colorA, colorB, Color::getAlpha);
        return aProgress == Float.POSITIVE_INFINITY ? 0f : aProgress;
    }
}
