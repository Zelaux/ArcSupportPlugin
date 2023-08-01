package com.zelaux.arcplugin.commandParam.psi;

import com.intellij.lang.ASTNode;
import com.intellij.lang.ParserDefinition;
import com.intellij.lang.PsiParser;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.project.Project;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IFileElementType;
import com.intellij.psi.tree.TokenSet;
import com.zelaux.arcplugin.commandParam.ArcCommandParamLanguage;
import com.zelaux.arcplugin.commandParam.parser.ArcCommandParamsParser;
import com.zelaux.arcplugin.commandParam.parsing.ArcCommandParamsLexer;
import org.jetbrains.annotations.NotNull;

public class ArcCommandParamsParserDefinition implements ParserDefinition {
        public ArcCommandParamsParserDefinition() {
            super();
    }

    @Override
    public @NotNull TokenSet getWhitespaceTokens() {
        return TokenSet.EMPTY;
    }

    public static final IFileElementType FILE = new IFileElementType(ArcCommandParamLanguage.INSTANCE);
    @Override
    public @NotNull Lexer createLexer(Project project) {
        return new ArcCommandParamsLexer();
    }

    @Override
    public @NotNull PsiParser createParser(Project project) {
        return new ArcCommandParamsParser();
    }

    @Override
    public @NotNull IFileElementType getFileNodeType() {
        return FILE;
    }

    @Override
    public @NotNull TokenSet getCommentTokens() {
        return TokenSet.EMPTY;
    }

    @Override
    public @NotNull TokenSet getStringLiteralElements() {
        return TokenSet.EMPTY;
    }

    @Override
    public @NotNull PsiElement createElement(ASTNode node) {
        return CPTypes.Factory.createElement(node);
    }

    @Override
    public @NotNull PsiFile createFile(@NotNull FileViewProvider viewProvider) {
        return new ArcCommandParamsFile(viewProvider);
    }
}
