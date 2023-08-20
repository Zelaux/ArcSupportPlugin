// This is a generated file. Not intended for manual editing.
package com.zelaux.arcplugin.commandParam.psi;

import com.intellij.psi.tree.IElementType;
import com.intellij.psi.PsiElement;
import com.intellij.lang.ASTNode;
import com.zelaux.arcplugin.commandParam.parsing.psi.tree.CommandParamElementType;
import com.zelaux.arcplugin.commandParam.parsing.psi.tree.CommandParamTokenType;
import com.zelaux.arcplugin.commandParam.psi.impl.*;

public interface CPTypes {

  IElementType ID = new CommandParamElementType("ID");
  IElementType OPTIONAL_PARAM = new CommandParamElementType("OPTIONAL_PARAM");
  IElementType PARAM = new CommandParamElementType("PARAM");
  IElementType PARAM_LIST = new CommandParamElementType("PARAM_LIST");
  IElementType REQUIRED_PARAM = new CommandParamElementType("REQUIRED_PARAM");
  IElementType VARIADIC = new CommandParamElementType("VARIADIC");

  IElementType DOTS = new CommandParamTokenType("...");
  IElementType IDENTIFIER = new CommandParamTokenType("IDENTIFIER");
  IElementType LEFT_ARROW = new CommandParamTokenType("<");
  IElementType LEFT_BRACKET = new CommandParamTokenType("[");
  IElementType RIGHT_ARROW = new CommandParamTokenType(">");
  IElementType RIGHT_BRACKET = new CommandParamTokenType("]");
  IElementType SPACE = new CommandParamTokenType(" ");

  class Factory {
    public static PsiElement createElement(ASTNode node) {
      IElementType type = node.getElementType();
      if (type == ID) {
        return new PsiACPIdImpl(node);
      }
      else if (type == OPTIONAL_PARAM) {
        return new PsiACPOptionalParamImpl(node);
      }
      else if (type == PARAM_LIST) {
        return new PsiACPParamListImpl(node);
      }
      else if (type == REQUIRED_PARAM) {
        return new PsiACPRequiredParamImpl(node);
      }
      else if (type == VARIADIC) {
        return new PsiACPVariadicImpl(node);
      }
      throw new AssertionError("Unknown element type: " + type);
    }
  }
}
