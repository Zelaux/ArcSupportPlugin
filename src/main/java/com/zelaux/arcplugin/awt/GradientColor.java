package com.zelaux.arcplugin.awt;

import java.awt.*;
import java.awt.color.*;

public class GradientColor extends Color{
    public GradientColor(int r, int g, int b, float progress){
        super(r, g, b);
        this.progress = progress;
    }

    public GradientColor(int r, int g, int b, int a, float progress){
        super(r, g, b, a);
        this.progress = progress;
    }

    public GradientColor(int rgb, float progress){
        super(rgb);
        this.progress = progress;
    }

    public GradientColor(int rgba, boolean hasalpha, float progress){
        super(rgba, hasalpha);
        this.progress = progress;
    }

    public GradientColor(float r, float g, float b, float progress){
        super(r, g, b);
        this.progress = progress;
    }

    public GradientColor(float r, float g, float b, float a, float progress){
        super(r, g, b, a);
        this.progress = progress;
    }
public final float progress;
    public GradientColor(ColorSpace cspace, float[] components, float alpha, float progress){
        super(cspace, components, alpha);
        this.progress = progress;
    }
}
