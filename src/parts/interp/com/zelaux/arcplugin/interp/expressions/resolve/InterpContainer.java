package com.zelaux.arcplugin.interp.expressions.resolve;

import com.intellij.openapi.util.TextRange;
import com.zelaux.arcplugin.expressions.render.ExpressionSequenceRenderer;
import com.zelaux.arcplugin.expressions.resolve.ExpressionSequence;
import com.zelaux.arcplugin.interp.expressions.render.InterpContainerRenderer;
import org.jetbrains.annotations.NotNull;

public class InterpContainer implements ExpressionSequence<InterpExpression> {
    public final InterpExpression myExpression;

    public InterpContainer(InterpExpression expression) {
        myExpression = expression;
    }

    @Override
    public InterpContainerRenderer createRenderer() {
        return new InterpContainerRenderer(this);
    }

    @NotNull
    @Override
    public TextRange getTextRange() {
        return myExpression.getUElement().getSourcePsi().getTextRange();
    }
    @Override
    public void add(int i, InterpExpression expression) {
throw new UnsupportedOperationException();
    }

    @Override
    public void add(InterpExpression expression) {
throw new UnsupportedOperationException();
    }
}
