package com.zelaux.arcplugin.commandParam.psi;

import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface PsiArcCommandParamsParamI extends PsiElement {

    @NotNull
    PsiArcCommandParamsId getId();

    @Nullable
    PsiArcCommandParamsVariadic getVariadic();

    default boolean isVariadic() {
        return getVariadic() != null;
    }
    default boolean isOptional(){
        return this instanceof PsiArcCommandParamsOptionalParam;
    }
    default String getParameterName(){
        return getId().getIdentifier().getText();
    }
}
