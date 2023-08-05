package com.zelaux.arcplugin.expressions.resolve.methods;

import arc.graphics.Color;
import com.zelaux.arcplugin.expressions.render.exp.HueShiftExpressionRenderer;
import com.zelaux.arcplugin.expressions.render.exp.ShiftHsvExpressionRenderer;
import com.zelaux.arcplugin.expressions.resolve.methods.hsv.HsvComponent;
import org.jetbrains.uast.UCallExpression;

public class ShiftHsvExpression extends ArcColorExpressionUCall {
    public final HsvComponent component;

    public ShiftHsvExpression(UCallExpression uElement, String tabTitle, HsvComponent component) {
        super(uElement, tabTitle);
        this.component = component;
    }

    public Float currentValue(){
        return getParam(ParameterType.FLOAT,0);
    }
    @Override
    public boolean apply(Color target) {
        Float currentValue = currentValue();
        if(currentValue==null)return false;
        component.shifter.get(target,currentValue);
        return true;
    }

    @Override
    public ShiftHsvExpressionRenderer createRenderer() {
        return component==HsvComponent.hue?new HueShiftExpressionRenderer(this):new ShiftHsvExpressionRenderer(this);
    }
}
