package com.zelaux.arcplugin.codeInsight.template.postfix;

import com.intellij.codeInsight.daemon.impl.analysis.JavaGenericsUtil;
import com.intellij.codeInsight.template.postfix.templates.*;
import com.intellij.java.JavaBundle;
import com.intellij.openapi.editor.*;
import com.intellij.psi.*;
import com.intellij.util.containers.*;
import com.zelaux.arcplugin.codeInsight.template.postfix.templates.*;
import org.jetbrains.annotations.*;

import java.util.*;

public class ArcPostfixTemplateProvider implements PostfixTemplateProvider{
    public ArcPostfixTemplateProvider() {
        int i=0;
    }

    @Override
    public @NotNull Set<PostfixTemplate> getTemplates(){
        return ContainerUtil.set(
                new IteratorPostfixTemplate("iter",this),
                new SetToSeqPostfixTemplate("seq",this),
                new ToWritesPostfixTemplate("writes",this),
                new ToReadsPostfixTemplate("reads",this),
                new ToReads2PostfixTemplate("reads",this)
        );

    }

    @Override
    public @NotNull @NonNls String getId() {
        return PostfixTemplateProvider.super.getId();
    }
    @NotNull
    @Override
    public String getPresentableName() {
        return "ArcSupport";
    }

    @Override
    public boolean isTerminalSymbol(char currentChar){
        return currentChar=='.' || currentChar=='!';
    }

    @Override
    public void preExpand(@NotNull PsiFile file, @NotNull Editor editor){

    }

    @Override
    public void afterExpand(@NotNull PsiFile file, @NotNull Editor editor){

    }

    @Override
    public @NotNull PsiFile preCheck(@NotNull PsiFile copyFile, @NotNull Editor realEditor, int currentOffset){
        return copyFile;
    }
}
