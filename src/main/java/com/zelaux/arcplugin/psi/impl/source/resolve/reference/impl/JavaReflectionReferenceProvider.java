package com.zelaux.arcplugin.psi.impl.source.resolve.reference.impl;

import com.intellij.psi.*;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

abstract class JavaReflectionReferenceProvider extends PsiReferenceProvider {
    @Override
    public PsiReference @NotNull [] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext context) {
        if (element instanceof PsiLiteralExpression) {
            PsiLiteralExpression literal = (PsiLiteralExpression)element;
            if (literal.getValue() instanceof String) {
                PsiElement parent = element.getParent();
                if (parent instanceof PsiExpressionList) {
                    PsiElement grandParent = parent.getParent();
                    if (grandParent instanceof PsiMethodCallExpression) {
                        PsiReferenceExpression methodReference = ((PsiMethodCallExpression)grandParent).getMethodExpression();
                        PsiReference[] references = getReferencesByMethod(literal, methodReference, context);
                        if (references != null) {
                            return references;
                        }
                    }
                }
            }
        }
        return PsiReference.EMPTY_ARRAY;
    }

    protected abstract PsiReference @Nullable [] getReferencesByMethod(@NotNull PsiLiteralExpression literalArgument,
                                                                       @NotNull PsiReferenceExpression methodReference,
                                                                       @NotNull ProcessingContext context);
}
