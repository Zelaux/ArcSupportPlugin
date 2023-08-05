package com.zelaux.arcplugin.langinfo;

import com.intellij.pom.java.LanguageLevel;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public interface MyElementFactory {

    @NotNull PsiElement createClass(@NotNull String name) throws IncorrectOperationException;


    @NotNull PsiElement createInterface(@NotNull String name) throws IncorrectOperationException;


    @NotNull PsiElement createEnum(@NotNull String name) throws IncorrectOperationException;


    @NotNull PsiElement createField(@NotNull String name, @NotNull PsiType type) throws IncorrectOperationException;


    @NotNull PsiElement createMethod(@NotNull String name, PsiType returnType) throws IncorrectOperationException;


    @NotNull PsiElement createMethod(@NotNull String name, PsiType returnType, @Nullable PsiElement context) throws IncorrectOperationException;


    @NotNull PsiElement createConstructor();


    @NotNull PsiElement createClassInitializer();


    @NotNull PsiElement createParameter(@NotNull String name, PsiType type) throws IncorrectOperationException;


    PsiElement createParameter(@NotNull String name, PsiType type, @Nullable PsiElement context) throws IncorrectOperationException;


    @NotNull PsiElement createParameterList(String @NotNull [] names, PsiType @NotNull [] types) throws IncorrectOperationException;


    @NotNull PsiElement createMethodFromText(String text, @Nullable PsiElement context);


    @NotNull PsiElement createAnnotationFromText(@NotNull String annotationText, @Nullable PsiElement context) throws IncorrectOperationException;


    @NotNull PsiElement createExpressionFromText(@NotNull String text, @Nullable PsiElement context) throws IncorrectOperationException;


    @NotNull PsiElement createReferenceElementByType(PsiClassType type);


    @NotNull PsiElement createTypeParameterList();


    @NotNull PsiElement createTypeParameter(@NotNull String name, PsiClassType @NotNull [] superTypes);


//    @NotNull PsiElement createType(@NotNull PsiClass aClass);


    @NotNull PsiElement createAnnotationType(@NotNull String name) throws IncorrectOperationException;


    @NotNull PsiElement createConstructor(@NotNull String name);


    @NotNull PsiElement createConstructor(@NotNull String name, @Nullable PsiElement context);


//    @NotNull PsiElement createType(@NotNull PsiClass resolve, @NotNull PsiSubstitutor substitutor);


//    @NotNull PsiElement createType(@NotNull PsiClass resolve, @NotNull PsiSubstitutor substitutor, @Nullable LanguageLevel languageLevel);


//    @NotNull PsiElement createType(@NotNull PsiClass aClass, PsiType parameters);


//    @NotNull PsiElement createType(@NotNull PsiClass aClass, PsiType... parameters);


//    @NotNull PsiElement createRawSubstitutor(@NotNull PsiTypeParameterListOwner owner);


//    @NotNull PsiElement createSubstitutor(@NotNull Map<PsiTypeParameter, PsiType> map);


//    @Nullable PsiElement createPrimitiveType(@NotNull String text);


//    @NotNull PsiElement createTypeByFQClassName(@NotNull String qName);


//    @NotNull PsiElement createTypeByFQClassName(@NotNull String qName, @NotNull GlobalSearchScope resolveScope);


    @NotNull PsiElement createDocCommentFromText(@NotNull String text);
}
