package com.zelaux.arcplugin.commandParam.highlight;

import com.intellij.lang.BracePair;
import com.intellij.lang.PairedBraceMatcher;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IElementType;
import com.zelaux.arcplugin.commandParam.psi.CPTypes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ArcCommandParamsPairedBraceMatcher implements PairedBraceMatcher {

    private static final BracePair[] pairs = {
            new BracePair(CPTypes.LEFT_BRACKET, CPTypes.RIGHT_BRACKET,true),
            new BracePair(CPTypes.LEFT_ARROW, CPTypes.RIGHT_ARROW,true)
    };

    @Override
    public BracePair @NotNull [] getPairs() {
        return pairs;
    }

    @Override
    public boolean isPairedBracesAllowedBeforeType(@NotNull IElementType lbraceType, @Nullable IElementType contextType) {
        return true;
    }

    @Override
    public int getCodeConstructStart(PsiFile file, int openingBraceOffset) {
        return openingBraceOffset;
    }
}
