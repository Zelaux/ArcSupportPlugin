package com.zelaux.arcplugin.properties.completion;

import com.intellij.openapi.util.TextRange;
import com.intellij.patterns.PsiJavaElementPattern;
import com.intellij.patterns.StandardPatterns;
import com.intellij.psi.*;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.intellij.patterns.PsiJavaPatterns.psiLiteral;

public class PropReferenceContributor extends PsiReferenceContributor {
    @Override
    public void registerReferenceProviders(@NotNull PsiReferenceRegistrar registrar) {
        registrar.registerReferenceProvider(psiLanguageInjectionHost(), new PsiReferenceProvider() {
            @Override
            public PsiReference @NotNull [] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext context) {

                return createPlaceholderPropertiesReferences( element);
            }
        });
    }


    private PsiReference[] createPlaceholderPropertiesReferences(@Nullable PsiElement valueElement) {
        if (valueElement == null ) return PsiReference.EMPTY_ARRAY;

        String text = valueElement.getText();
        if (!text.startsWith("@") && !(text.startsWith("\"@") && text.endsWith("\""))) return PsiReference.EMPTY_ARRAY;

        TextRange textRange;
        if(text.startsWith("@")){
            textRange = new TextRange(1,text.length());
        } else{
            textRange = new TextRange(2,text.length()-1);
        }

        return new PsiReference[]{new MyPsiReference(valueElement, textRange)};
    }


    private PsiJavaElementPattern.Capture<PsiLiteral> psiLanguageInjectionHost() {
        return /*psiLiteral().with(new PatternCondition<>("injection host") {
            @Override
            public boolean accepts(@NotNull PsiLiteral psiLiteral, ProcessingContext context) {
                return psiLiteral instanceof PsiLanguageInjectionHost;
            }
        });
    }*/psiLiteral(StandardPatterns.string().startsWith("@"));
    }
}
