package com.zelaux.arcplugin.expressions.resolve.methods;

import arc.graphics.Color;
import com.intellij.psi.PsiElement;
import com.zelaux.arcplugin.expressions.render.ArcColorExpressionRenderer;
import com.zelaux.arcplugin.expressions.render.exp.LerpColorParserRenderer;
import com.zelaux.arcplugin.expressions.resolve.ArcColorExpression;
import com.zelaux.arcplugin.utils.LazyValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.uast.UCallExpression;
import org.jetbrains.uast.UElement;

public class LerpExpression extends ArcColorExpressionUCall {
    protected final static Color tmpColor = new Color();
    protected final static Color tmpColor2 = new Color();
    public final LazyValue<@Nullable ArcColorExpression> innerColor = LazyValue.create(() -> {
        if (parameterOffset == 0) {
            return null;
        }
        return getStaticSetColorParser(RECEIVER);
    });
    public final LazyValue<ArcColorExpression> secondColor = LazyValue.create(() ->

            getStaticSetColorParser(0));

    @NotNull
    protected ArcColorExpression getStaticSetColorParser(int index) {
        return new StaticSetColorExpression(getUElement(), uelementClass, "NONE", it -> getParamExpression(index)) {
            @Override
            protected UElement calculateUElement(PsiElement psiElement) {
                return LerpExpression.this.calculateUElement(psiElement);
            }

            @Override
            public void invalidateUElement() {
                super.invalidateUElement();
                LerpExpression.this.invalidateUElement();
            }
        };
    }

    public LerpExpression(UCallExpression uElement, String tabTitle) {
        super(uElement, tabTitle);
    }

    @Override
    public void invalidateUElement() {
        super.invalidateUElement();
        innerColor.reset();
        secondColor.reset();
    }

    public Float progress() {
        return getParam(ParameterType.FLOAT, 1);
    }

    @Override
    public boolean apply(Color target) {
        Float progress = progress();
        if (progress == null) return false;
        ArcColorExpression a = innerColor.get();
        tmpColor.set(target);
        if (a != null) a.apply(tmpColor);

        secondColor.get().apply(tmpColor2);
        target.set(tmpColor).lerp(tmpColor2, progress);
        return true;
    }

    @Override
    public ArcColorExpressionRenderer createRenderer() {
        return new LerpColorParserRenderer(this);
    }
}
