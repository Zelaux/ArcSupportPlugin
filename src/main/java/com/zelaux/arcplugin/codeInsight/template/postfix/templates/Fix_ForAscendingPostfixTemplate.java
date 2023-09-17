package com.zelaux.arcplugin.codeInsight.template.postfix.templates;


import com.intellij.codeInsight.template.postfix.templates.*;
import com.intellij.codeInsight.template.postfix.templates.editable.*;
import com.intellij.psi.*;
import com.zelaux.arcplugin.activities.startup.*;
import org.jetbrains.annotations.*;

import java.util.*;

@SuppressWarnings("PostfixTemplateDescriptionNotFound")
public  class Fix_ForAscendingPostfixTemplate extends ForIndexedPostfixTemplate{
    @SuppressWarnings("RedundantSuppression")
    public Fix_ForAscendingPostfixTemplate(@NotNull JavaPostfixTemplateProvider provider){
        super("fori", "for ($type$ $index$ = 0; $index$ < $bound$; $index$++) {\n$END$\n}",
        "for (int i = 0; i < expr.length; i++)", provider);

        //for 2023.2
        //noinspection RedundantCast,unchecked
        PostfixTemplateFix.editConditions((Set<JavaPostfixTemplateExpressionCondition>)myExpressionConditions);

    }

    @Override
    protected @Nullable String getExpressionBound(@NotNull PsiExpression expr){
        return PostfixTemplateFix.getExpressionBound(expr);
    }
}