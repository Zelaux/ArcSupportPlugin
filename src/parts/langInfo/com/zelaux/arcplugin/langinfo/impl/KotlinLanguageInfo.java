package com.zelaux.arcplugin.langinfo.impl;

import com.intellij.openapi.project.Project;
import com.zelaux.arcplugin.langinfo.LanguageInfo;
import com.zelaux.arcplugin.langinfo.MyElementFactory;
import com.zelaux.arcplugin.langinfo.impl.kotlin.KotlinElementFactory;
import org.jetbrains.kotlin.psi.KtPsiFactory;

public class KotlinLanguageInfo implements LanguageInfo {
    @Override
    public MyElementFactory getNewFactory(Project project) {
        return new KotlinElementFactory(project);
    }
}
