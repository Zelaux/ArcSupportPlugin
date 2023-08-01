// This is a generated file. Not intended for manual editing.
package com.zelaux.arcplugin.commandParam.psi;

import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiElement;

public class PsiArcCommandParamsVisitor extends PsiElementVisitor {

  public void visitId(@NotNull PsiArcCommandParamsId o) {
    visitPsiElement(o);
  }

  public void visitOptionalParam(@NotNull PsiArcCommandParamsOptionalParam o) {
    visitParam(o);
  }

  public void visitParam(@NotNull PsiArcCommandParamsParam o) {
    visitParamI(o);
  }

  public void visitParamList(@NotNull PsiArcCommandParamsParamList o) {
    visitPsiElement(o);
  }

  public void visitRequiredParam(@NotNull PsiArcCommandParamsRequiredParam o) {
    visitParam(o);
  }

  public void visitVariadic(@NotNull PsiArcCommandParamsVariadic o) {
    visitPsiElement(o);
  }

  public void visitParamI(@NotNull PsiArcCommandParamsParamI o) {
    visitPsiElement(o);
  }

  public void visitPsiElement(@NotNull PsiElement o) {
    visitElement(o);
  }

}
