package com.zelaux.arcplugin.marker.interp;

import com.intellij.psi.*;
import com.zelaux.arcplugin.marker.*;
import com.zelaux.arcplugin.parsers.*;
import com.zelaux.arcplugin.parsers.colorexpr.*;
import com.zelaux.arcplugin.parsers.interp.*;
import org.jetbrains.annotations.*;

public class InterpParserProvider implements ExpParserSeqProvider{
    @Override
    public @Nullable InterpParser expressionParserSequenceFrom(@NotNull PsiElement element){
        if(!(element instanceof PsiReferenceExpression)) return null;
        InterpParser parser = new InterpParser(element,element.getText(), (_p, elem) -> elem);
        if(!parser.validate()){
            return null;
        }
        return parser ;
    }
}
