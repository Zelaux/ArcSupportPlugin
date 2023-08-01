// This is a generated file. Not intended for manual editing.
package com.zelaux.arcplugin.commandParam.psi.impl;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static com.zelaux.arcplugin.commandParam.psi.CPTypes.*;
import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.zelaux.arcplugin.commandParam.psi.*;

public abstract class PsiArcCommandParamsParamImpl extends ASTWrapperPsiElement implements PsiArcCommandParamsParam {

  public PsiArcCommandParamsParamImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull PsiArcCommandParamsVisitor visitor) {
    visitor.visitParam(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof PsiArcCommandParamsVisitor) accept((PsiArcCommandParamsVisitor)visitor);
    else super.accept(visitor);
  }

}