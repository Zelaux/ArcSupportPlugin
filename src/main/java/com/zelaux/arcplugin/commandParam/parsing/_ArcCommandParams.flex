package com.zelaux.arcplugin.commandParam.parser;

import com.intellij.lexer.FlexLexer;
import com.intellij.psi.tree.IElementType;
import com.zelaux.arcplugin.commandParam.psi.CPTypes;
import com.intellij.psi.TokenType;
%%

%{
  public _ArcCommandParamsLexer() {
    this((java.io.Reader)null);
  }
%}

%public
%class _ArcCommandParamsLexer
%implements FlexLexer
%function advance
%type IElementType
%unicode

//IDENTIFIER=([:letter:]|[_])([:letter:]|[_\-0-9])*
//IDENTIFIER=([:letter:]|[_])([:letter:]|[_\-0-9])+
//IDENTIFIER=(\\.*|[^\s<>\[\].]+)+


%%

<YYINITIAL> {
  " "                                  { return CPTypes.CP_SPACE; }
  "..."                                  { return CPTypes.CP_DOTS; }
  "["                                  { return CPTypes.CP_LEFT_BRACKET; }
  "]"                                  { return CPTypes.CP_RIGHT_BRACKET; }
  "<"                                 { return CPTypes.CP_LEFT_ARROW; }
  ">"                                  { return CPTypes.CP_RIGHT_ARROW; }
  (\\.*|[^\s<>\[\].]+)+                        { return CPTypes.CP_IDENTIFIER; }
}

[^] {
    return TokenType.BAD_CHARACTER;
}