package com.zelaux.arcplugin.colorExpression;

import arc.graphics.Color;
import arc.util.pooling.Pool;
import arc.util.pooling.Pools;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.zelaux.arcplugin.expressions.resolve.ArcColorExpressionSequence;
import com.zelaux.arcplugin.expressions.resolve.methods.StaticSetColorExpression;
import com.zelaux.arcplugin.marker.ExpressionSequenceProvider;
import com.zelaux.arcplugin.utils.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.uast.UCallExpression;
import org.jetbrains.uast.UExpression;
import org.jetbrains.uast.UReferenceExpression;
import org.jetbrains.uast.UastUtils;

public class ColorFieldAssignLineMarkerProvider implements ExpressionSequenceProvider {

    private static final Pool<Color> colorPool = ColorUtils.colorPool;

    @Override
    public ArcColorExpressionSequence expressionParserSequenceFrom(@NotNull PsiElement element) {
        if (!(element instanceof PsiIdentifier)) return null;
        PsiElement myReference = PsiTreeUtil.findFirstParent(element, it -> it instanceof PsiReferenceExpression);
        if (myReference == null || myReference.getParent() instanceof PsiReferenceExpression) return null;
        PsiElement __parent = PsiTreeUtil.findFirstParent(myReference, it -> it instanceof PsiAssignmentExpression || it instanceof PsiVariable);
        UExpression expression = null;
        if (__parent instanceof PsiAssignmentExpression assignmentExpression) {
            PsiExpression rExpression = assignmentExpression.getRExpression();
            if (PsiTreeUtil.isAncestor(rExpression, element, false)) {
                expression = UastUtils.findContaining(rExpression, UExpression.class);
            }
        } else if (__parent instanceof PsiVariable variable) {
            PsiExpression rExpression = variable.getInitializer();
            if (PsiTreeUtil.isAncestor(rExpression, element, false)) {
                expression = UastUtils.findContaining(rExpression, UExpression.class);
            }
        } else {
            UCallExpression containing = UastUtils.findContaining(element, UCallExpression.class);
            boolean isUnresolved = ColorExpressionResolver.resolve(containing) == null;
            if (isUnresolved)
                if (containing != null) {
                    for (UExpression argument : containing.getValueArguments()) {
                        PsiElement sourcePsi = argument.getSourcePsi();
                        if (PsiTreeUtil.isAncestor(sourcePsi, element, false)) {
                            expression = argument;
                            break;

                        }
                    }
                } else{
                    UastUtils.findContaining(myReference,UReferenceExpression.class);
                }
        }
        if (expression == null) return null;
        StaticSetColorExpression colorExpression = new StaticSetColorExpression(expression, UExpression.class, "");
        Color obtain = colorPool.obtain();
        try {
            if (!colorExpression.apply(obtain)) return null;
        } finally {
            colorPool.free(obtain);
        }
        ArcColorExpressionSequence sequence = new ArcColorExpressionSequence();
        sequence.add(colorExpression);
        return sequence;
    }

}
