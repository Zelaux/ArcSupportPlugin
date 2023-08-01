package com.zelaux.arcplugin.psi.impl.source.resolve.reference.impl;

import com.intellij.codeInsight.completion.CompletionConfidence;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.impl.source.resolve.reference.impl.JavaReflectionReferenceContributor;
import com.intellij.util.ThreeState;
import org.jetbrains.annotations.NotNull;

public class ArcReflectionCompletionConfidence extends CompletionConfidence {
    @Override
    public @NotNull ThreeState shouldSkipAutopopup(@NotNull PsiElement contextElement, @NotNull PsiFile psiFile, int offset) {
        final PsiElement literal = contextElement.getParent();
        if (literal != null &&
                (ArcReflectionReferenceContributor.Holder.FIELD_1.accepts(literal) ||
                        ArcReflectionReferenceContributor.Holder.FIELD_2.accepts(literal) ||
                        ArcReflectionReferenceContributor.Holder.METHOD_1.accepts(literal) ||
                        ArcReflectionReferenceContributor.Holder.METHOD_2.accepts(literal) ||
                        ArcReflectionReferenceContributor.Holder.CLASS_PATTERN.accepts(literal))) {
            return ThreeState.NO;
        }
        return super.shouldSkipAutopopup(contextElement, psiFile, offset);
    }
}
