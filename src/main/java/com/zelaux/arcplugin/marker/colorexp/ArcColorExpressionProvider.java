/*
package com.zelaux.arcplugin.marker.colorexp;

import com.intellij.psi.*;
import com.zelaux.arcplugin.marker.*;
import com.zelaux.arcplugin.parsers.colorexpr.ColorExpParserSequence;
import org.jetbrains.annotations.*;

public class ArcColorExpressionProvider implements ExpParserSeqProvider{
    @Override
    public @Nullable ColorExpParserSequence expressionParserSequenceFrom(@NotNull PsiElement element){
        if(!(element instanceof PsiIdentifier)) return null;
        PsiIdentifier identifier = (PsiIdentifier)element;
        PsiElement parent__ = identifier.getParent();
        if(!(parent__ instanceof PsiReference || parent__ instanceof PsiReferenceExpression)) return null;
        PsiElement referenceExpression = identifier.getParent();
        if(!(referenceExpression.getParent() instanceof PsiCallExpression)) return null;

        return null;
//        return SmartColorResolver.resolveColor((PsiCallExpression)referenceExpression.getParent());
    }
}
*/
