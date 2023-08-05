package com.zelaux.arcplugin.expressions.resolve.methods;

import arc.graphics.Color;
import com.zelaux.arcplugin.expressions.render.ArcColorExpressionRenderer;
import com.zelaux.arcplugin.expressions.render.exp.ValueOfParserRenderer;
import com.zelaux.arcplugin.expressions.resolve.Expression;
import org.jetbrains.uast.UCallExpression;

public class ValueOfExpression extends ArcColorExpressionUCall implements Expression.ExpressionEntryPoint{
    public ValueOfExpression(UCallExpression uElement, String tabTitle) {
        super(uElement, tabTitle);
    }
    private static Color tmpColor=new Color();
    @Override
    public boolean apply(Color target) {
        try{
            Color.valueOf(tmpColor,getParam(ParameterType.STRING,0));
        } catch (Exception e){
            this.unresolved=true;
            target.set(0xff);
            return false;
        }
        target.set(tmpColor);
        return true;
    }

    @Override
    public ArcColorExpressionRenderer createRenderer() {
        return new ValueOfParserRenderer(this);
    }
}
