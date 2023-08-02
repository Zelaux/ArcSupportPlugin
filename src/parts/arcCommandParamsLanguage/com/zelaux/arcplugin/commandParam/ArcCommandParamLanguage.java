package com.zelaux.arcplugin.commandParam;

import com.intellij.lang.Language;
import com.intellij.openapi.fileTypes.LanguageFileType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ArcCommandParamLanguage extends Language {

    protected ArcCommandParamLanguage() {
        super("ArcCommandParams");
    }  @NotNull
    public static final ArcCommandParamLanguage INSTANCE = new ArcCommandParamLanguage();

    @Override
    public @Nullable LanguageFileType getAssociatedFileType() {
        return ArcCommandParamFileType.INSTANCE;
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "ArcCommandParams";
    }

}
