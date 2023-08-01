// This is a generated file. Not intended for manual editing.
package com.zelaux.arcplugin.commandParam.psi;

import com.intellij.psi.tree.IElementType;
import com.intellij.psi.PsiElement;
import com.intellij.lang.ASTNode;
import com.zelaux.arcplugin.commandParam.parsing.psi.tree.CommandParamElementType;
import com.zelaux.arcplugin.commandParam.parsing.psi.tree.CommandParamTokenType;
import com.zelaux.arcplugin.commandParam.psi.impl.*;

public interface CPTypes {

  IElementType CP_ID = new CommandParamElementType("CP_ID");
  IElementType CP_OPTIONAL_PARAM = new CommandParamElementType("CP_OPTIONAL_PARAM");
  IElementType CP_PARAM = new CommandParamElementType("CP_PARAM");
  IElementType CP_PARAM_LIST = new CommandParamElementType("CP_PARAM_LIST");
  IElementType CP_REQUIRED_PARAM = new CommandParamElementType("CP_REQUIRED_PARAM");
  IElementType CP_VARIADIC = new CommandParamElementType("CP_VARIADIC");

  IElementType CP_DOTS = new CommandParamTokenType("...");
  IElementType CP_IDENTIFIER = new CommandParamTokenType("IDENTIFIER");
  IElementType CP_LEFT_ARROW = new CommandParamTokenType("<");
  IElementType CP_LEFT_BRACKET = new CommandParamTokenType("[");
  IElementType CP_RIGHT_ARROW = new CommandParamTokenType(">");
  IElementType CP_RIGHT_BRACKET = new CommandParamTokenType("]");
  IElementType CP_SPACE = new CommandParamTokenType(" ");

  class Factory {
    public static PsiElement createElement(ASTNode node) {
      IElementType type = node.getElementType();
      if (type == CP_ID) {
        return new PsiArcCommandParamsIdImpl(node);
      }
      else if (type == CP_OPTIONAL_PARAM) {
        return new PsiArcCommandParamsOptionalParamImpl(node);
      }
      else if (type == CP_PARAM_LIST) {
        return new PsiArcCommandParamsParamListImpl(node);
      }
      else if (type == CP_REQUIRED_PARAM) {
        return new PsiArcCommandParamsRequiredParamImpl(node);
      }
      else if (type == CP_VARIADIC) {
        return new PsiArcCommandParamsVariadicImpl(node);
      }
      throw new AssertionError("Unknown element type: " + type);
    }
  }
}
