package com.zelaux.arcplugin.commandParam;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.fileTypes.LanguageFileType;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.openapi.util.NlsSafe;
import com.zelaux.arcplugin.PluginIcons;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class ArcCommandParamFileType extends LanguageFileType {

    protected ArcCommandParamFileType() {
        super(ArcCommandParamLanguage.INSTANCE);
    }  @NotNull
    public static final ArcCommandParamFileType INSTANCE = new ArcCommandParamFileType();


    @Override
    public @NonNls @NotNull String getName() {
        return "ArcCommandParams";
    }

    @Override
    public @NlsContexts.Label @NotNull String getDescription() {
        return "ArcCommandParams";
    }

    @Override
    public @NlsSafe @NotNull String getDefaultExtension() {
        return "arccommandparams";
    }

    @Override
    public Icon getIcon() {
    return PluginIcons.PluginIcon;
    }
}
