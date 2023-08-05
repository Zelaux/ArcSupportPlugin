package com.zelaux.arcplugin.expressions.resolve.methods;

import arc.func.Cons;
import arc.graphics.Color;
import com.zelaux.arcplugin.expressions.render.ArcColorExpressionRenderer;
import com.zelaux.arcplugin.expressions.render.exp.NoParamFuncitonExpressionRenderer;
import org.jetbrains.uast.UCallExpression;

public class NoParamFunctionExpression extends ArcColorExpressionUCall{
    public final Cons<Color> function;
    public NoParamFunctionExpression(UCallExpression uElement, String tabTitle, Cons<Color> function) {
        super(uElement, tabTitle);
        this.function = function;
    }

    @Override
    public boolean apply(Color target) {
        function.get(target);
        return true;
    }

    @Override
    public ArcColorExpressionRenderer createRenderer() {
        return new NoParamFuncitonExpressionRenderer(this);
    }
}
