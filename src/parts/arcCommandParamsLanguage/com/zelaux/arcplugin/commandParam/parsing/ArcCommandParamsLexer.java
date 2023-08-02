package com.zelaux.arcplugin.commandParam.parsing;

import com.intellij.lexer.FlexAdapter;
import com.zelaux.arcplugin.commandParam.parser._ArcCommandParamsLexer;

public class ArcCommandParamsLexer extends FlexAdapter {
    public ArcCommandParamsLexer() {
        super(new _ArcCommandParamsLexer(null));
    }
}
