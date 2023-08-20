package com.zelaux.arcplugin.langinfo;

import com.intellij.lang.Language;
import com.intellij.lang.LanguageExtension;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.psi.JVMElementFactories;
import com.intellij.psi.JVMElementFactory;
import org.jetbrains.annotations.Nullable;

public interface LanguageInfo {
    LanguageExtension<LanguageInfo> LANG_EP = new LanguageExtension<>("com.zelaux.arcplugin.languageInfo");
    Key<Key<MyElementFactory>> KEY_TO_KEY_TO_FACTORY = new Key<>("Key<Key<MyElementFactory>>");

    @Nullable
    static MyElementFactory elementFactoryFor(Language language, Project project) {
        Key<MyElementFactory> key = KEY_TO_KEY_TO_FACTORY.get(language);
        if (key == null) {
            KEY_TO_KEY_TO_FACTORY.set(language, key = Key.create("Key<Key<MyElementFactory>>_" + language.getID()));
        }
        MyElementFactory factory = key.get(project);
        if (factory == null) {
            LanguageInfo info = LANG_EP.forLanguage(language);
            if (info == null) {
                JVMElementFactory elementFactory = JVMElementFactories.getFactory(language, project);
                if (elementFactory == null) return null;
                key.set(project, factory = new JvmFactrory2MyElementFactory(elementFactory));
            } else {
                key.set(project, factory = info.getNewFactory(project));
            }

        }
        return factory;
    }

    MyElementFactory getNewFactory(Project project);

    /*@NotNull PsiElement parseExpression(@NotNull PsiElement context,@NotNull String initializer);

    default PsiElement createStringLiteral(@NotNull PsiElement context,@NotNull String value){
        return parseExpression(context,'"'+value+'"');
    }
    default PsiElement createIntegerLiteral(@NotNull PsiElement context,@NotNull String number){
        return parseExpression(context,number);
    }
    default PsiElement createFloatLiteral(@NotNull PsiElement context,float number){
        return parseExpression(context, String.valueOf(number));
    }*/

}
