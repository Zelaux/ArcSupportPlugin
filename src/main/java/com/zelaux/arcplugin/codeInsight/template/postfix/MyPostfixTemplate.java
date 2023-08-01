package com.zelaux.arcplugin.codeInsight.template.postfix;

import com.intellij.codeInsight.template.Template;
import com.intellij.codeInsight.template.impl.MacroCallNode;
import com.intellij.codeInsight.template.impl.TextExpression;
import com.intellij.codeInsight.template.impl.VariableNode;
import com.intellij.codeInsight.template.macro.ExpressionTypeMacro;
import com.intellij.codeInsight.template.macro.SuggestVariableNameMacro;
import com.intellij.codeInsight.template.postfix.templates.PostfixTemplateProvider;
import com.intellij.codeInsight.template.postfix.templates.editable.JavaEditablePostfixTemplate;
import com.intellij.codeInsight.template.postfix.templates.editable.JavaPostfixTemplateExpressionCondition;
import com.intellij.pom.java.LanguageLevel;
import com.intellij.psi.PsiElement;
import com.intellij.psi.codeStyle.JavaFileCodeStyleFacade;
import com.intellij.util.containers.ContainerUtil;
import com.zelaux.arcplugin.codeInsight.template.postfix.conditions.JavaPostfixTemplateExpressionTypeCondition;
import com.zelaux.arcplugin.codeInsight.template.postfix.templates.TemplateUtils;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public abstract class MyPostfixTemplate extends JavaEditablePostfixTemplate {
    public MyPostfixTemplate(@NotNull String templateName,
                             @Language("text")
                             @NotNull String templateText,
                             @NotNull String example,
                             @NotNull Set<JavaPostfixTemplateExpressionCondition> expressionConditions,
            /*@NotNull LanguageLevel minimumLanguageLevel,*/
            /*boolean useTopmostExpression,*/
                             @NotNull PostfixTemplateProvider provider) {
        super(templateName,
                templateText,
                example,
                expressionConditions,
                /*minimumLanguageLevel*/        LanguageLevel.JDK_1_5,
                /*useTopmostExpression*/true,
                provider);
    }

    protected abstract void setupTemplateVariables(@NotNull PsiElement element, @NotNull Template template);

    @Override
    protected final void addTemplateVariables(@NotNull PsiElement element, @NotNull Template template) {
        setupTemplateVariables(element, template);
    }

}
