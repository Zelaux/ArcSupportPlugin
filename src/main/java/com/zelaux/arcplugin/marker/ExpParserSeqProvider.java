package com.zelaux.arcplugin.marker;

import com.intellij.openapi.extensions.*;
import com.intellij.psi.*;
import com.zelaux.arcplugin.marker.colorexp.*;
import com.zelaux.arcplugin.parsers.*;
import org.jetbrains.annotations.*;

public interface ExpParserSeqProvider{
    ExtensionPointName<ExpParserSeqProvider> EP_NAME = ExtensionPointName.create("com.zelaux.arcplugin.expressionParserSequenceProvider");
    @SuppressWarnings("rawtypes")
    @Nullable
    ExpressionParserSequence expressionParserSequenceFrom(@NotNull PsiElement element);
}
