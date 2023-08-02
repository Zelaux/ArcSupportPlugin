package com.zelaux.arcplugin.commandParam.psi;

import com.intellij.extapi.psi.PsiFileBase;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.psi.FileViewProvider;
import com.zelaux.arcplugin.commandParam.ArcCommandParamFileType;
import com.zelaux.arcplugin.commandParam.ArcCommandParamLanguage;
import org.jetbrains.annotations.NotNull;

public class ArcCommandParamsFile extends PsiFileBase {
    public ArcCommandParamsFile(@NotNull FileViewProvider viewProvider) {
        super(viewProvider, ArcCommandParamLanguage.INSTANCE);
    }

    @Override
    public @NotNull FileType getFileType() {
        return ArcCommandParamFileType.INSTANCE;
    }

    @Override
    public String toString() {
        return "ArcCommandParamsFile";
    }
}