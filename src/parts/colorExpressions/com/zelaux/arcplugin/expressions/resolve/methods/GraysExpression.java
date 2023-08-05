package com.zelaux.arcplugin.expressions.resolve.methods;

import arc.graphics.Color;
import com.zelaux.arcplugin.expressions.render.ArcColorExpressionRenderer;
import com.zelaux.arcplugin.expressions.render.exp.GraysExpressionRenderer;
import com.zelaux.arcplugin.expressions.resolve.Expression;
import org.jetbrains.uast.UCallExpression;

public class GraysExpression extends ArcColorExpressionUCall implements Expression.ExpressionEntryPoint {

     public GraysExpression(UCallExpression uElement, String tabTitle) {
        super(uElement, tabTitle);
    }
     public GraysExpression(UCallExpression uElement) {
        super(uElement, "grays()");
    }
    public Float currentValue(){
         return getParam(ParameterType.FLOAT,0);
    }

    @Override
    public boolean apply(Color target) {
        Float currentValue = currentValue();
        if(currentValue==null)return false;
        target.set(currentValue,currentValue,currentValue,1f);
        return true;
    }

    @Override
    public ArcColorExpressionRenderer createRenderer() {
        return new GraysExpressionRenderer(this);
    }
}
