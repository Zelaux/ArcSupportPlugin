package com.zelaux.arcplugin.codeInsight.template.postfix.templates;

import com.intellij.codeInsight.template.Template;
import com.intellij.codeInsight.template.postfix.templates.PostfixTemplateProvider;
import com.intellij.psi.PsiElement;
import com.intellij.util.containers.ContainerUtil;
import com.zelaux.arcplugin.codeInsight.template.postfix.MyPostfixTemplate;
import com.zelaux.arcplugin.codeInsight.template.postfix.conditions.JavaPostfixTemplateExpressionTypeCondition;
import org.jetbrains.annotations.NotNull;

public class ToReads2PostfixTemplate extends MyPostfixTemplate {
    public ToReads2PostfixTemplate(@NotNull String templateName, @NotNull PostfixTemplateProvider provider) {
        super(templateName,
                "new arc.util.io.Reads(new java.io.DataInputStream(new java.io.ByteArrayInputStream($EXPR$)))",
                "bytes->new Reads(new DataInput(bytes))",
                ContainerUtil.newHashSet(new JavaPostfixTemplateExpressionTypeCondition("Is byte[]", it -> it.equalsToText("byte[]"))),
                provider);
    }

    @Override
    protected void setupTemplateVariables(@NotNull PsiElement element, @NotNull Template template) {

    }
}
