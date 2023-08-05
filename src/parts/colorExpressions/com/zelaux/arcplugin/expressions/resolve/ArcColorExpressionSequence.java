package com.zelaux.arcplugin.expressions.resolve;

import arc.util.Tmp;
import com.intellij.openapi.util.TextRange;
import com.zelaux.arcplugin.expressions.render.ArcColorExpressionSequenceRenderer;
import com.zelaux.arcplugin.expressions.render.ExpressionSequenceRenderer;
import com.zelaux.arcplugin.utils.ColorUtils;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ArcColorExpressionSequence implements ExpressionSequence<ArcColorExpression> {
    final List<ArcColorExpression> list = new ArrayList<>();

    public Color calculateResultAwtColor() {
        applyColorUntil(null, Tmp.c1.set(0xff));
        return ColorUtils.toAwt(Tmp.c1);
    }

    public boolean isEmpty() {
        return list.isEmpty();
    }

    @Override
    public ExpressionSequenceRenderer<?> createRenderer() {
        return new ArcColorExpressionSequenceRenderer(this);
    }

    @Override
    public TextRange getTextRange() {
        return list.get(list.size() - 1).getUElement().getSourcePsi().getTextRange();
    }

    @Override
    public void add(int i, ArcColorExpression expression) {
        list.add(0, expression);
    }

    @Override
    public void add(ArcColorExpression arcColorExpression) {
        list.add(arcColorExpression);
    }

    public void applyColorUntil(@Nullable ArcColorExpression expression, arc.graphics.Color color) {
        for (int i = 0; i < list.size(); i++) {
            ArcColorExpression exp = list.get(i);
            if (exp == expression) break;
            exp.apply(color);
        }
    }

    public List<ArcColorExpression> list() {
        return list;
    }
}
