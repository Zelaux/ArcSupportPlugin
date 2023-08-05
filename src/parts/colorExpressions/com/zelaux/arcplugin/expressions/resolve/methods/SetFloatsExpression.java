package com.zelaux.arcplugin.expressions.resolve.methods;

import arc.graphics.Color;
import com.zelaux.arcplugin.expressions.render.exp.SetFloatArrayColorRenderer;
import com.zelaux.arcplugin.expressions.resolve.Expression;
import org.jetbrains.uast.UCallExpression;

public class SetFloatsExpression extends ArcColorExpressionUCall implements Expression.ExpressionEntryPoint {
    public final boolean hasAlpha;

    public SetFloatsExpression(UCallExpression uElement, String tabTitle, boolean hasAlpha) {
        super(uElement, tabTitle);
        this.hasAlpha = hasAlpha;
    }

    public SetFloatsExpression(UCallExpression uElement, boolean hasAlpha, String methodName) {
        super(uElement, methodName + (hasAlpha ? "(float,float,float,float)" : "(float,float,float)"));
        this.hasAlpha = hasAlpha;
    }


    @Override
    public boolean apply(Color target) {
        Float r = getParam(ParameterType.FLOAT, 0);
        if(r==null)return false;
        Float g = getParam(ParameterType.FLOAT, 1);
        if(g==null)return false;
        Float b = getParam(ParameterType.FLOAT, 2);
        if(b==null) return false;
        float a=1f;
        if(hasAlpha){
            Float param = getParam(ParameterType.FLOAT, 3);
            if(param==null)return false;
            a=param;
        }
        apply(target, r, g, b, a);
        return true;
    }

    protected void apply(Color target, Float r, Float g, Float b, float a) {
        target.set(r, g, b, a);
    }

    @Override
    public SetFloatArrayColorRenderer createRenderer() {
        return new SetFloatArrayColorRenderer(this);
    }
}
