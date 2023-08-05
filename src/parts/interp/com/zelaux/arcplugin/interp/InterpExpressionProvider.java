package com.zelaux.arcplugin.interp;

import arc.math.Interp;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiLambdaExpressionType;
import com.intellij.psi.PsiType;
import com.zelaux.arcplugin.expressions.resolve.ExpressionSequence;
import com.zelaux.arcplugin.interp.expressions.resolve.InterpExpression;
import com.zelaux.arcplugin.marker.ExpressionSequenceProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.uast.*;

import java.util.List;

public class InterpExpressionProvider implements ExpressionSequenceProvider {

    @Override
    public @Nullable ExpressionSequence<?> expressionParserSequenceFrom(@NotNull PsiElement element) {

        UIdentifier identifier = UastContextKt.getUastParentOfType(element, UIdentifier.class);
        if (identifier == null) return null;
        UExpression expression = UastContextKt.getUastParentOfType(element, UExpression.class);
        if (expression == null) return null;
        PsiType expressionType = expression.getExpressionType();
        if (expressionType == null)
            return null;
        if (expressionType instanceof PsiLambdaExpressionType) {
            PsiLambdaExpressionType type = (PsiLambdaExpressionType) expressionType;
            PsiType interfaceType = type.getExpression().getFunctionalInterfaceType();
            if (!isInterpType(interfaceType)) return null;
        } else if (!expressionType.getCanonicalText().equals(Interp.class.getCanonicalName())) {
            return null;
        }
        if (expression instanceof ULambdaExpression) {
            ULambdaExpression expr = (ULambdaExpression) expression;
            List<UParameter> parameters = expr.getParameters();
            if (parameters.isEmpty()) return null;
            //noinspection UElementAsPsi
            if (parameters.get(0).getNameIdentifier() != element) {
                return null;
            }
        } else {
            UElement uastParent = identifier.getUastParent();
            if (uastParent == null) return null;
            if (uastParent.getUastParent() != expression) {
                return null;
            }
        }
        int i = 0;
        InterpExpression resolved = InterpResolver.resolve(expression);
        return resolved == null ? null : resolved.asSequence();
    }

    private boolean isInterpType(PsiType type) {
        if (type == null) return false;
        if (type.getCanonicalText().equals(Interp.class.getCanonicalName())) return true;
        for (PsiType superType : type.getSuperTypes()) {
            if (isInterpType(superType)) return true;
        }
        return false;
    }
}
