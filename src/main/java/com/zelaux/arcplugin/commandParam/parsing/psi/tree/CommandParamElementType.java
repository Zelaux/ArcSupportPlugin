package com.zelaux.arcplugin.commandParam.parsing.psi.tree;

import com.intellij.psi.tree.IElementType;
import com.zelaux.arcplugin.commandParam.ArcCommandParamLanguage;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public class CommandParamElementType extends IElementType {
    public CommandParamElementType(@NonNls @NotNull String debugName) {
        super(debugName, ArcCommandParamLanguage.INSTANCE);
    }
}
