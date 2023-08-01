package com.zelaux.arcplugin.codeInsight.template.postfix.templates;

import com.intellij.codeInsight.template.Template;
import com.intellij.codeInsight.template.postfix.templates.PostfixTemplateProvider;
import com.intellij.codeInsight.template.postfix.templates.editable.JavaEditablePostfixTemplate;
import com.intellij.codeInsight.template.postfix.templates.editable.JavaPostfixTemplateExpressionCondition;
import com.intellij.psi.PsiElement;
import com.intellij.util.containers.ContainerUtil;
import com.zelaux.arcplugin.codeInsight.template.postfix.MyPostfixTemplate;
import com.zelaux.arcplugin.codeInsight.template.postfix.conditions.JavaPostfixTemplateExpressionTypeCondition;
import org.jetbrains.annotations.NotNull;

import java.io.DataOutput;
import java.util.Set;

public class ToWritesPostfixTemplate extends MyPostfixTemplate {
    public ToWritesPostfixTemplate(@NotNull String templateName,
                                   @NotNull PostfixTemplateProvider provider) {
        super(templateName,
                "new arc.util.io.Writes($EXPR$)",
                "dataOutput->new Writes(dataOutput)",
                ContainerUtil.newHashSet(
                        new JavaPostfixTemplateExpressionTypeCondition("Is DataOutput", it -> TemplateUtils.isInstance(it, DataOutput.class.getCanonicalName()))),
                provider);
    }

    @Override
    protected void setupTemplateVariables(@NotNull PsiElement element, @NotNull Template template) {

    }
}
