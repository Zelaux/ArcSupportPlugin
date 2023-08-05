package com.zelaux.arcplugin.interp.expressions.resolve;

import arc.math.Interp;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.uast.UExpression;

public class InterpStaticExpression extends InterpExpression {
    public final Interp interp;
    public InterpStaticExpression(@NotNull UExpression element, Interp interp) {
        super(element);
        this.interp = interp;
    }

    @Override
    public @Nullable Interp getInterpolation() {
        return interp;
    }
}
