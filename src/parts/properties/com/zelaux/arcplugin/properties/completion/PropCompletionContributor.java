package com.zelaux.arcplugin.properties.completion;

import com.intellij.codeInsight.completion.*;
import com.intellij.patterns.PsiJavaElementPattern;
import com.intellij.patterns.StandardPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiLiteral;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.intellij.patterns.PsiJavaPatterns.psiLiteral;

public class PropCompletionContributor extends CompletionContributor {

    static final PsiJavaElementPattern.Capture<PsiLiteral> SELECTOR =
            psiLiteral(StandardPatterns.string().startsWith("@"));


    public PropCompletionContributor() {
        extend(CompletionType.BASIC, SELECTOR, MyKeywordsCompletionProvider.INSTANCE);

    }


    private static class MyKeywordsCompletionProvider extends CompletionProvider<CompletionParameters> {
        private static final MyKeywordsCompletionProvider INSTANCE = new MyKeywordsCompletionProvider();

        @Override
        protected void addCompletions(@NotNull CompletionParameters parameters,
                                      @NotNull ProcessingContext context,
                                      @NotNull CompletionResultSet result) {
            PsiElement element = parameters.getPosition();
            final String prefix = element.getText().substring(1);

            Stream<PropertyUtils.PropertyInfo> stream = PropertyUtils.getPropertiesKeys(element.getProject());
            if (stream == null) return;
            for (PropertyUtils.PropertyInfo propertiesKey : stream.collect(Collectors.toList())) {
                propertiesKey.text = "@" + propertiesKey.text;
                result.addElement(propertiesKey.toLookup());
            }
            /*SearchUtil.findKeys()*/
        }

    }
}
