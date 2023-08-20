package com.zelaux.arcplugin.commandParam.highlight;

import com.intellij.lexer.Lexer;
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase;
import com.intellij.psi.tree.IElementType;
import com.zelaux.arcplugin.commandParam.parsing.ArcCommandParamsLexer;
import com.zelaux.arcplugin.commandParam.psi.CPTypes;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

import static com.intellij.openapi.editor.colors.TextAttributesKey.createTextAttributesKey;

 class ArcCommandParamsSyntaxHighlighter extends SyntaxHighlighterBase {
    public static final TextAttributesKey IDENTIFIER =
            createTextAttributesKey("JSONPATH.IDENTIFIER", DefaultLanguageHighlighterColors.INSTANCE_FIELD);

    public static final TextAttributesKey BRACKETS =
            createTextAttributesKey("JSONPATH.BRACKETS", DefaultLanguageHighlighterColors.BRACKETS);

    public static final TextAttributesKey DOT =
            createTextAttributesKey("JSONPATH.DOT", DefaultLanguageHighlighterColors.DOT);

    private static final Map<IElementType, TextAttributesKey> ourMap;

    static {
        ourMap = new HashMap<>();

        fillMap(ourMap, IDENTIFIER, CPTypes.IDENTIFIER);
        fillMap(ourMap, BRACKETS,
                CPTypes.LEFT_BRACKET, CPTypes.RIGHT_BRACKET,CPTypes.LEFT_ARROW,CPTypes.RIGHT_ARROW);
        fillMap(ourMap, DOT, CPTypes.DOTS);
    }

    public ArcCommandParamsSyntaxHighlighter() {
        super();
    }

    @Override
    public @NotNull Lexer getHighlightingLexer() {
        return new ArcCommandParamsLexer();
    }

    @Override
    public TextAttributesKey @NotNull [] getTokenHighlights(IElementType tokenType) {
        return pack(ourMap.get(tokenType));
    }
}
