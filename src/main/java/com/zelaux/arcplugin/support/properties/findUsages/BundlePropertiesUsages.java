package com.zelaux.arcplugin.support.properties.findUsages;

import com.intellij.psi.*;
import com.intellij.usages.impl.rules.*;
import org.jetbrains.annotations.*;

public class BundlePropertiesUsages implements UsageTypeProvider{
    @Override
    public @Nullable UsageType getUsageType(@NotNull PsiElement element){
//        System.out.println(element);
        return null;
    }
}
