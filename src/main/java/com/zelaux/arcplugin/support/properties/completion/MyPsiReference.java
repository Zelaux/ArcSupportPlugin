package com.zelaux.arcplugin.support.properties.completion;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiLiteralExpression;
import com.intellij.psi.PsiReferenceBase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

class MyPsiReference extends PsiReferenceBase<PsiElement> {
    public MyPsiReference(@NotNull PsiElement element, TextRange rangeInElement) {
        super(element,rangeInElement);
    }

    @Override
    public @Nullable PsiElement resolve() {
        final   String text;
        if (myElement instanceof PsiLiteralExpression) {
            text = String.valueOf(((PsiLiteralExpression) myElement).getValue()).substring(1);
        } else{
            text = this.myElement.getText().substring(1);
        }
        PropertyUtils.PropertyInfo found = PropertyUtils.findKey(myElement.getProject(), it -> Objects.equals(it.getKey(), text));
        if (found == null) return null;
        //noinspection DataFlowIssue
        return found.property.getPsiElement();
    }

    @Override
    public Object @NotNull [] getVariants() {
        return Objects.requireNonNull(PropertyUtils.getPropertiesKeys(myElement.getProject()))
                .map(it -> {
                    it.text = it.text;
                    return it.property;
                })
                .toArray();
    }
}
