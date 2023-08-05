package com.zelaux.arcplugin.expressions.resolve.methods;

import arc.func.Func;
import arc.graphics.Color;
import com.intellij.psi.PsiElement;
import com.zelaux.arcplugin.colorExpression.ColorExpressionResolver;
import com.zelaux.arcplugin.expressions.render.exp.StaticSetColorParserRenderer;
import com.zelaux.arcplugin.expressions.resolve.ArcColorExpression;
import com.zelaux.arcplugin.expressions.resolve.Expression;
import com.zelaux.arcplugin.langinfo.LanguageInfo;
import com.zelaux.arcplugin.langinfo.MyElementFactory;
import com.zelaux.arcplugin.utils.ColorUtils;
import com.zelaux.arcplugin.utils.LazyValue;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.uast.UElement;
import org.jetbrains.uast.UExpression;

public class StaticSetColorExpression extends ArcColorExpression implements Expression.ExpressionEntryPoint {
    private final Color myTmpColor = new Color();
    public final Func<UElement, UExpression> mapper;
    public final LazyValue<@Nullable Color> myColor = LazyValue.create(() -> {
        UExpression target = getTargetExpression();
        myTmpColor.set(0xff);
        java.awt.Color resolved = ColorExpressionResolver.resolveColor(target);
        if (resolved == null) return null;
        return ColorUtils.toArc(resolved, myTmpColor);
    });

    public StaticSetColorExpression(UExpression uElement, Class<? extends UElement> uelementClass, String tabTitle) {
        this(uElement, uelementClass, tabTitle, it -> (UExpression) it);
    }

    public StaticSetColorExpression(UElement uElement, Class<? extends UElement> uelementClass, String tabTitle, Func<UElement, UExpression> mapper) {
        super(uElement, uelementClass, tabTitle);
        this.mapper = mapper;
    }

    public UExpression getTargetExpression() {
        return StaticSetColorExpression.this.mapper.get(getUElement());
    }

    @Override
    public boolean apply(Color target) {
        Color color = myColor.get();
        if(color==null)return false;
        target.set(color);
        return true;

    }

    @Override
    public StaticSetColorParserRenderer createRenderer() {
        return new StaticSetColorParserRenderer(this);
    }

    public void replaceTarget(String newText) {
        executeAlarm(() -> {
            PsiElement sourcePsi = getTargetExpression().getSourcePsi();
            if (sourcePsi == null) return;
            @Nullable MyElementFactory factory = LanguageInfo.elementFactoryFor(sourcePsi.getLanguage(), sourcePsi.getProject());
            if (factory == null) return;
            sourcePsi.replace(factory.createExpressionFromText(newText, sourcePsi));
            invalidateUElement();
        });
    }
}
