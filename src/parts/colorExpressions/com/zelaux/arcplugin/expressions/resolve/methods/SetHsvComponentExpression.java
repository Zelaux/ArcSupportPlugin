package com.zelaux.arcplugin.expressions.resolve.methods;

import arc.graphics.Color;
import com.zelaux.arcplugin.expressions.render.exp.HsvComponentSetParserRenderer;
import com.zelaux.arcplugin.expressions.render.exp.HueComponentSetParserRenderer;
import com.zelaux.arcplugin.expressions.resolve.methods.hsv.HsvComponent;
import org.jetbrains.uast.UCallExpression;

public class SetHsvComponentExpression extends ArcColorExpressionUCall {
    public final HsvComponent component;

    public SetHsvComponentExpression(UCallExpression uElement, String tabTitle, HsvComponent component) {
        super(uElement, tabTitle);
        this.component = component;
    }

    public Float currentValue() {
        return getParam(ParameterType.FLOAT, 0);
    }

    @Override
    public boolean apply(Color target) {
        Float currentValue = currentValue();
        if (currentValue == null) return false;
        component.set(target, currentValue);
        return true;
//        return false;
    }

    @Override
    public HsvComponentSetParserRenderer createRenderer() {
        return component == HsvComponent.hue ? new HueComponentSetParserRenderer(this) : new HsvComponentSetParserRenderer(this);
    }
}
