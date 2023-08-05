package com.zelaux.arcplugin.expressions.resolve.methods;

import arc.graphics.Color;
import com.intellij.psi.PsiElement;
import com.zelaux.arcplugin.expressions.render.ArcColorExpressionRenderer;
import com.zelaux.arcplugin.expressions.render.exp.LerpColorParserRenderer;
import com.zelaux.arcplugin.expressions.resolve.ArcColorExpression;
import com.zelaux.arcplugin.utils.ColorUtils;
import com.zelaux.arcplugin.utils.LazyValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.uast.UCallExpression;
import org.jetbrains.uast.UElement;

public class LerpExpression extends ArcColorExpressionUCall {

    public LerpExpression(UCallExpression uElement, String tabTitle) {
        super(uElement, tabTitle);
    }    public final LazyValue<@Nullable ArcColorExpression> innerColor = LazyValue.create(() -> {

        return getStaticSetColorParser(RECEIVER);
    });

    @Nullable
    protected ArcColorExpression getStaticSetColorParser(int index) {
        if (parameterOffset == 0 && index==RECEIVER) {
            return null;
        }
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
    }    public final LazyValue<ArcColorExpression> secondColor = LazyValue.create(() ->

            getStaticSetColorParser(0));

    @Override
    public boolean isEntryPoint() {
        return parameterOffset == 1 && getClass() == LerpExpression.class;
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
        Color tmpColor = ColorUtils.obtainColor();
        Color tmpColor2 = ColorUtils.obtainColor();
        tmpColor.set(target);
        if (a != null) a.apply(tmpColor);

        secondColor.get().apply(tmpColor2);
        target.set(tmpColor).lerp(tmpColor2, progress);
        ColorUtils.freeColors(tmpColor,tmpColor2);
        return true;
    }

    @Override
    public ArcColorExpressionRenderer createRenderer() {
        return new LerpColorParserRenderer(this);
    }




}
