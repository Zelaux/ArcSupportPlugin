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

public class PsiACPParamListImpl extends ASTWrapperPsiElement implements PsiACPParamList {

  public PsiACPParamListImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull PsiACPVisitor visitor) {
    visitor.visitParamList(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof PsiACPVisitor) accept((PsiACPVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public List<PsiACPParam> getParamList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, PsiACPParam.class);
  }

}
