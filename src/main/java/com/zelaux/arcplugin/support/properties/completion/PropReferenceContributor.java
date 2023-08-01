package com.zelaux.arcplugin.support.properties.completion;

import com.intellij.psi.*;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class PropReferenceContributor extends PsiReferenceContributor {
    @Override
    public void registerReferenceProviders(@NotNull PsiReferenceRegistrar registrar) {
        registrar.registerReferenceProvider(PropCompletionContributor.SELECTOR, new PsiReferenceProvider() {
            @Override
            public PsiReference @NotNull [] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext context) {
                return new PsiReference[]{new PsiReferenceBase<PsiElement>(element) {
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
                                    it.text = "@" + it.text;
                                    return it.toLookup();
                                })
                                .toArray();
                    }
                }};
            }
        });
    }
}
