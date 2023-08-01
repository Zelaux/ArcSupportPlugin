package com.zelaux.arcplugin.codeInsight.template.postfix.templates;


import com.intellij.codeInsight.template.*;
import com.intellij.codeInsight.template.impl.*;
import com.intellij.codeInsight.template.postfix.templates.*;
import com.intellij.codeInsight.template.postfix.util.*;
import com.intellij.psi.*;
import com.zelaux.arcplugin.activities.startup.*;
import org.jetbrains.annotations.*;


@SuppressWarnings("PostfixTemplateDescriptionNotFound")
public class Fix_ForDescendingPostfixTemplate extends ForIndexedPostfixTemplate{
    public Fix_ForDescendingPostfixTemplate(@NotNull JavaPostfixTemplateProvider provider){
        super("forr", "for ($type$ $index$ = $bound$; $index$ $sign$ 0; $index$--) {\n$END$\n}",
        "for (int i = expr.length-1; i >= 0; i--)", provider);

        PostfixTemplateFix.editConditions(this.getExpressionConditions());
    }

    @NotNull
    private static String getSign(@NotNull PsiElement element){
        return element instanceof PsiExpression && JavaPostfixTemplatesUtils.isNumber(((PsiExpression)element).getType()) ? ">" : ">=";
    }

    @Override
    protected void addTemplateVariables(@NotNull PsiElement element, @NotNull Template template){
        super.addTemplateVariables(element, template);
        template.addVariable("sign", new TextExpression(getSign(element)), false);
    }

    @Nullable
    @Override
    protected String getExpressionBound(@NotNull PsiExpression expr){
        String result = PostfixTemplateFix.getExpressionBound(expr);
        return result == null || JavaPostfixTemplatesUtils.isNumber(expr.getType()) ? result : result + " - 1";
    }
}