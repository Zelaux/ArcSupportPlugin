package com.zelaux.arcplugin.expressions.resolve.methods.hsv;

import arc.graphics.Color;

public enum HsvComponent {
    hue(360f, Color::hue, Color::hue, Color::shiftHue),
    saturation(1f, Color::saturation, Color::saturation, Color::shiftSaturation),
    value(1f, Color::value, Color::value, Color::shiftValue),
    ;
    public final float maxValue;
    public final Cons setter;
    public final Prov getter;
    public final Cons shifter;

    public void set(Color color,float value){
        setter.get(color,value);
    }
    public float get(Color color){
        return getter.get(color);
    }
    public void shift(Color color,float value){
        shifter.get(color,value);
    }
    HsvComponent(float maxValue, Cons setter, Prov getter, Cons shifter) {
        this.maxValue = maxValue;
        this.setter = setter;
        this.getter = getter;
        this.shifter = shifter;
    }

    public interface Prov {
        float get(Color color);
    }

    public interface Cons {
        void get(Color color, float value);
    }
}

