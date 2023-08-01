package com.zelaux.arcplugin.marker.colorexp;

import com.intellij.openapi.extensions.*;
import com.intellij.psi.*;
import com.zelaux.arcplugin.parsers.colorexpr.*;
import org.jetbrains.annotations.*;

public interface ElementColorExpressionProvider{
    ExtensionPointName<ElementColorExpressionProvider> EP_NAME = ExtensionPointName.create("com.zelaux.arcplugin.colorExpressionProvider");
    @Nullable
    ColorExpParserSequence getColorExpressionFrom(@NotNull PsiElement element);

}
