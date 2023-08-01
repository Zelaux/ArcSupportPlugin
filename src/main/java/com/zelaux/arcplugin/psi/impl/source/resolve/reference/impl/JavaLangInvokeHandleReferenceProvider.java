package com.zelaux.arcplugin.psi.impl.source.resolve.reference.impl;

import com.intellij.psi.*;
import com.intellij.psi.impl.source.resolve.reference.impl.JavaLangInvokeHandleReference;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

public class JavaLangInvokeHandleReferenceProvider extends PsiReferenceProvider {
    @Override
    public PsiReference @NotNull [] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext context) {
        if (element instanceof PsiLiteralExpression) {
            final PsiLiteralExpression literal = (PsiLiteralExpression)element;
            if (literal.getValue() instanceof String) {
                final PsiElement parent = element.getParent();
                if (parent instanceof PsiExpressionList) {
                    final PsiExpression[] expressions = ((PsiExpressionList)parent).getExpressions();
                    final PsiExpression qualifier = expressions.length != 0 ? expressions[0] : null;
                    if (qualifier != null) {
                        return new PsiReference[]{new JavaLangInvokeHandleReference(literal, qualifier)};
                    }
                }
            }
        }
        return PsiReference.EMPTY_ARRAY;
    }
}
