package com.zelaux.arcplugin.commandParam.psi;

import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface PsiACPParamI extends PsiElement {

    @NotNull
    PsiACPId getId();

    @Nullable
    PsiACPVariadic getVariadic();

    default boolean isVariadic() {
        return getVariadic() != null;
    }
    default boolean isOptional(){
        return this instanceof PsiACPOptionalParam;
    }
    default String getParameterName(){
        return getId().getIdentifier().getText();
    }
}
