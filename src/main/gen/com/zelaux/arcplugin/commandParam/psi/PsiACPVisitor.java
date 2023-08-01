// This is a generated file. Not intended for manual editing.
package com.zelaux.arcplugin.commandParam.psi;

import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiElement;

public class PsiACPVisitor extends PsiElementVisitor {

  public void visitId(@NotNull PsiACPId o) {
    visitPsiElement(o);
  }

  public void visitOptionalParam(@NotNull PsiACPOptionalParam o) {
    visitParam(o);
  }

  public void visitParam(@NotNull PsiACPParam o) {
    visitParamI(o);
  }

  public void visitParamList(@NotNull PsiACPParamList o) {
    visitPsiElement(o);
  }

  public void visitRequiredParam(@NotNull PsiACPRequiredParam o) {
    visitParam(o);
  }

  public void visitVariadic(@NotNull PsiACPVariadic o) {
    visitPsiElement(o);
  }

  public void visitParamI(@NotNull PsiACPParamI o) {
    visitPsiElement(o);
  }

  public void visitPsiElement(@NotNull PsiElement o) {
    visitElement(o);
  }

}
