package com.zelaux.arcplugin.codeInsight.template.postfix.templates;

import com.intellij.codeInsight.template.Template;
import com.intellij.codeInsight.template.postfix.templates.PostfixTemplateProvider;
import com.intellij.psi.PsiElement;
import com.intellij.util.containers.ContainerUtil;
import com.zelaux.arcplugin.codeInsight.template.postfix.MyPostfixTemplate;
import com.zelaux.arcplugin.codeInsight.template.postfix.conditions.JavaPostfixTemplateExpressionTypeCondition;
import org.jetbrains.annotations.NotNull;

import java.io.DataInput;
import java.io.DataOutput;

public class ToReadsPostfixTemplate extends MyPostfixTemplate {
    public ToReadsPostfixTemplate(@NotNull String templateName,
                                  @NotNull PostfixTemplateProvider provider) {
        super(templateName,
                "new arc.util.io.Reads($EXPR$)",
                "dataInput->new Reads(dataInput)",
                ContainerUtil.newHashSet(
                        new JavaPostfixTemplateExpressionTypeCondition("Is DataInput", it -> TemplateUtils.isInstance(it, DataInput.class.getCanonicalName()))),
                provider);
    }

    @Override
    protected void setupTemplateVariables(@NotNull PsiElement element, @NotNull Template template) {

    }
}
