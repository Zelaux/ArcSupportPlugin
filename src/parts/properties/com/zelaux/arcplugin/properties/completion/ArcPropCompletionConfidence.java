package com.zelaux.arcplugin.properties.completion;

import com.intellij.codeInsight.completion.CompletionConfidence;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.ThreeState;
import org.jetbrains.annotations.NotNull;

public class ArcPropCompletionConfidence extends CompletionConfidence {
    @Override
    public @NotNull ThreeState shouldSkipAutopopup(@NotNull PsiElement contextElement, @NotNull PsiFile psiFile, int offset) {
        final PsiElement literal = contextElement.getParent();
        if (literal != null && PropCompletionContributor.SELECTOR.accepts(literal)) {
            return ThreeState.NO;
        }
        return super.shouldSkipAutopopup(contextElement, psiFile, offset);
    }
}
