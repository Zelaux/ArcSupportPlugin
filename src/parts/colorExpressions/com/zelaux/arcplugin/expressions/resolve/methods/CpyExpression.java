package com.zelaux.arcplugin.expressions.resolve.methods;

import arc.graphics.Color;
import org.jetbrains.uast.UCallExpression;

public class CpyExpression extends StaticSetColorExpression {
    public CpyExpression(UCallExpression callExpression, String tabTitle) {
        super(callExpression, UCallExpression.class, tabTitle, it -> ((UCallExpression) it).getReceiver());
    }

    @Override
    public boolean apply(Color target) {
        if (getTargetExpression() == null) return false;
        return super.apply(target);
    }
}
