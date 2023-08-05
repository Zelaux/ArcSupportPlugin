package com.zelaux.arcplugin.expressions.resolve.methods;

import arc.graphics.Color;
import com.zelaux.arcplugin.expressions.render.ArcColorExpressionRenderer;
import com.zelaux.arcplugin.expressions.render.exp.SetIntColorRenderer;
import com.zelaux.arcplugin.expressions.resolve.Expression;
import org.jetbrains.uast.UCallExpression;

public class SetIntExpression extends ArcColorExpressionUCall implements Expression.ExpressionEntryPoint {
    public final boolean hasAplha;

    public SetIntExpression(UCallExpression uElement, String tabTitle, boolean hasAplha) {
        super(uElement, tabTitle);
        this.hasAplha = hasAplha;
    }


    @Override
    public boolean apply(Color target) {
        Integer param = getParam(ParameterType.INT, 0);
        if (param == null) return false;
        if (hasAplha) {
            target.set(param);
        } else {
            target.rgb888(param);
        }
        return true;
    }

    @Override
    public ArcColorExpressionRenderer createRenderer() {
        return new SetIntColorRenderer(this);
    }
}
