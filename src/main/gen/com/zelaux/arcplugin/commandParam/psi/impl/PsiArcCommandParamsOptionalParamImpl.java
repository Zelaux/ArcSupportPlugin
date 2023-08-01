// This is a generated file. Not intended for manual editing.
package com.zelaux.arcplugin.commandParam.psi.impl;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static com.zelaux.arcplugin.commandParam.psi.CPTypes.*;
import com.zelaux.arcplugin.commandParam.psi.*;

public class PsiArcCommandParamsOptionalParamImpl extends PsiArcCommandParamsParamImpl implements PsiArcCommandParamsOptionalParam {

  public PsiArcCommandParamsOptionalParamImpl(@NotNull ASTNode node) {
    super(node);
  }

  @Override
  public void accept(@NotNull PsiArcCommandParamsVisitor visitor) {
    visitor.visitOptionalParam(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof PsiArcCommandParamsVisitor) accept((PsiArcCommandParamsVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public PsiArcCommandParamsId getId() {
    return findNotNullChildByClass(PsiArcCommandParamsId.class);
  }

  @Override
  @Nullable
  public PsiArcCommandParamsVariadic getVariadic() {
    return findChildByClass(PsiArcCommandParamsVariadic.class);
  }

}
