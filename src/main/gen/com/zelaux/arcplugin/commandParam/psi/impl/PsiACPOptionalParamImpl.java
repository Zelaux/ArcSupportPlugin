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

public class PsiACPOptionalParamImpl extends PsiACPParamImpl implements PsiACPOptionalParam {

  public PsiACPOptionalParamImpl(@NotNull ASTNode node) {
    super(node);
  }

  @Override
  public void accept(@NotNull PsiACPVisitor visitor) {
    visitor.visitOptionalParam(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof PsiACPVisitor) accept((PsiACPVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public PsiACPId getId() {
    return findNotNullChildByClass(PsiACPId.class);
  }

  @Override
  @Nullable
  public PsiACPVariadic getVariadic() {
    return findChildByClass(PsiACPVariadic.class);
  }

}
