package com.zelaux.arcplugin.marker;

import com.intellij.openapi.extensions.ExtensionPointName;
import com.intellij.psi.PsiElement;
import com.zelaux.arcplugin.expressions.resolve.ExpressionSequence;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ExpressionSequenceProvider {
    ExtensionPointName<ExpressionSequenceProvider> EP_NAME = ExtensionPointName.create("com.zelaux.arcplugin.expressionParserSequenceProvider");

    @SuppressWarnings("rawtypes")
    @Nullable
    ExpressionSequence<?> expressionParserSequenceFrom(@NotNull PsiElement element);
}
