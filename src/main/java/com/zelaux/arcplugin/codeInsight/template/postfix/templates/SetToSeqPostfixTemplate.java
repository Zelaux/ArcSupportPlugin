package com.zelaux.arcplugin.codeInsight.template.postfix.templates;

import arc.util.Structs;
import com.intellij.codeInsight.template.Template;
import com.intellij.codeInsight.template.impl.MacroCallNode;
import com.intellij.codeInsight.template.impl.TextExpression;
import com.intellij.codeInsight.template.impl.VariableNode;
import com.intellij.codeInsight.template.macro.IterableComponentTypeMacro;
import com.intellij.codeInsight.template.macro.SuggestVariableNameMacro;
import com.intellij.codeInsight.template.postfix.templates.PostfixTemplateProvider;
import com.intellij.codeInsight.template.postfix.templates.editable.JavaEditablePostfixTemplate;
import com.intellij.pom.java.LanguageLevel;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiType;
import com.intellij.psi.codeStyle.JavaFileCodeStyleFacade;
import com.intellij.util.containers.ContainerUtil;
import com.zelaux.arcplugin.codeInsight.template.postfix.conditions.JavaPostfixTemplateExpressionTypeCondition;
import org.jetbrains.annotations.NotNull;

public class SetToSeqPostfixTemplate extends JavaEditablePostfixTemplate {


    public SetToSeqPostfixTemplate(@NotNull String templateName, @NotNull PostfixTemplateProvider provider) {
        super(templateName, "$EXPR$.iterator().toSeq()$END$", "expr.iterator().toArray()",
                ContainerUtil.newHashSet(
                        new JavaPostfixTemplateExpressionTypeCondition("is arc set", TemplateUtils::isArcSetType)),
                LanguageLevel.JDK_1_5, true, provider);
    }

    @Override
    protected void addTemplateVariables(@NotNull PsiElement element, @NotNull Template template) {
        /*String finalPart = JavaFileCodeStyleFacade.forContext(element.getContainingFile()).isGenerateFinalLocals() ? "final " : null;
        if (finalPart != null) {
            template.addVariable("FINAL", new TextExpression(finalPart), false);
        }*/
    }
}
