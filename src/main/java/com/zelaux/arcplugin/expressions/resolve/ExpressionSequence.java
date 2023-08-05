package com.zelaux.arcplugin.expressions.resolve;

import com.intellij.openapi.util.TextRange;
import com.zelaux.arcplugin.expressions.render.ExpressionSequenceRenderer;

public interface ExpressionSequence<EXPR extends Expression<?>> {
     ExpressionSequenceRenderer<?> createRenderer();

    TextRange getTextRange();

    void add(int i, EXPR expression);
    void add( EXPR expression);
}
