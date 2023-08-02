package com.zelaux.arcplugin.properties.findUsages;

import com.intellij.lang.*;
import com.intellij.lang.cacheBuilder.*;
import com.intellij.lang.findUsages.*;
import com.intellij.lang.properties.*;
import com.intellij.openapi.util.text.*;
import com.intellij.psi.*;
import com.intellij.util.*;
import org.jetbrains.annotations.*;

public class BundlePropertiesFindUsagesProvider implements FindUsagesProvider {
    @Override
    public boolean canFindUsagesFor(@NotNull PsiElement psiElement) {
        return psiElement instanceof PsiNamedElement;
    }

    @Override
    public String getHelpId(@NotNull PsiElement psiElement) {
        return HelpID.FIND_OTHER_USAGES;
    }

    @Override
    @NotNull
    public String getType(@NotNull PsiElement element) {
        if (element instanceof IProperty) return PropertiesBundle.message("terms.property");
        return "";
    }

    @Override
    @NotNull
    public String getDescriptiveName(@NotNull PsiElement element) {
        String text;
        if (element instanceof PsiNamedElement) {
            text = StringUtil.notNullize(((PsiNamedElement) element).getName());
        } else {
            text = element.getText();
        }
        return "@" + text;
    }

    @Override
    @NotNull
    public String getNodeText(@NotNull PsiElement element, boolean useFullName) {
        return getDescriptiveName(element);
    }

    //private final PropertiesFindUsagesProvider findUsagesProvider=new PropertiesFindUsagesProvider();
    @Override
    public WordsScanner getWordsScanner() {
        return new MyWordsScanner();
    }

    class MyWordsScanner extends SimpleWordsScanner {


        @Override
        public void processWords(@NotNull CharSequence fileText, @NotNull Processor<? super WordOccurrence> processor) {
//            WordOccurrence occurrence = new WordOccurrence(fileText, 0, 0, null);
            super.processWords(fileText, it -> {
                if (it.getBaseText().charAt(it.getStart()) == '@') {
//                    CharSequence text = it..subSequence();
//                    occurrence.init(text);
//                    processor.process(occurrence);
                    it.init(it.getBaseText(), it.getStart() + 1, it.getEnd(), it.getKind());
                } else {
                }
               return processor.process(it);
            });
        }
    }
}
