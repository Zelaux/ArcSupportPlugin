package com.zelaux.arcplugin.utils.resolve;

import com.intellij.psi.PsiExpression;
import com.intellij.psi.PsiField;
import org.jetbrains.uast.UExpression;
import org.jetbrains.uast.UField;
import org.jetbrains.uast.UastUtils;

public record FieldData(
        String name,
        PsiField self,
        PsiExpression initializer
) {
    public UExpression uastInitializer() {
        return UastUtils.findContaining(initializer,UExpression.class);
    }

    public UField uastField() {
        return UastUtils.findContaining(self,UField.class);
    }
}
