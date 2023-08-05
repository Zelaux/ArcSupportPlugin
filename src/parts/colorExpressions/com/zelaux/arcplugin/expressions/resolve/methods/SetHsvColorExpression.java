package com.zelaux.arcplugin.expressions.resolve.methods;

import arc.graphics.Color;
import com.zelaux.arcplugin.expressions.render.exp.SetHsvColorExpressionRenderer;
import org.jetbrains.uast.UCallExpression;

public class SetHsvColorExpression extends SetFloatsExpression {
    public final float[] maxValues;

    public SetHsvColorExpression(UCallExpression uElement, float maxHue, float maxSaturation, float maxValue,boolean hasAlpha, String tabTitle) {
        super(uElement, tabTitle, hasAlpha);
        this.maxValues = new float[]{
                maxHue, maxSaturation, maxValue
        };
    }
    public SetHsvColorExpression(UCallExpression uElement, boolean useMapping,boolean hasAlpha, String tabTitle) {
        this(uElement,360f,useMapping?100f:1f,useMapping?100f:1f,hasAlpha,tabTitle);
    }

    @Override
    protected void apply(Color target, Float r, Float g, Float b, float a) {
        target.fromHsv(r/maxValues[0]*360f, g/maxValues[1], b/maxValues[2]).a(1f);
    }

    @Override
    public SetHsvColorExpressionRenderer createRenderer() {
        return new SetHsvColorExpressionRenderer(this);
    }
}
