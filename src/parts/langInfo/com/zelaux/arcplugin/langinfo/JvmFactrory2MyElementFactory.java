package com.zelaux.arcplugin.langinfo;

import com.intellij.pom.java.LanguageLevel;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;


public class JvmFactrory2MyElementFactory implements MyElementFactory{
    public final JVMElementFactory delegate;

    public JvmFactrory2MyElementFactory(JVMElementFactory delegate) {
        this.delegate = delegate;
    }

    @Override
    public @NotNull PsiElement createClass(@NotNull String name) throws IncorrectOperationException {
        return delegate.createClass(name);
    }

    @Override
    public @NotNull PsiElement createInterface(@NotNull String name) throws IncorrectOperationException {
        return delegate.createInterface(name);
    }

    @Override
    public @NotNull PsiElement createEnum(@NotNull String name) throws IncorrectOperationException {
        return delegate.createEnum(name);
    }

    @Override
    public @NotNull PsiElement createField(@NotNull String name, @NotNull PsiType type) throws IncorrectOperationException {
        return delegate.createField(name, type);
    }

    @Override
    public @NotNull PsiElement createMethod(@NotNull String name, PsiType returnType) throws IncorrectOperationException {
        return delegate.createMethod(name, returnType);
    }

    @Override
    public @NotNull PsiElement createMethod(@NotNull String name, PsiType returnType, @Nullable PsiElement context) throws IncorrectOperationException {
        return delegate.createMethod(name, returnType, context);
    }

    @Override
    public @NotNull PsiElement createConstructor() {
        return delegate.createConstructor();
    }

    @Override
    public @NotNull PsiElement createClassInitializer() {
        return delegate.createClassInitializer();
    }

    @Override
    public @NotNull PsiElement createParameter(@NotNull String name, PsiType type) throws IncorrectOperationException {
        return delegate.createParameter(name, type);
    }

    @Override
    public PsiElement createParameter(@NotNull String name, PsiType type, @Nullable PsiElement context) throws IncorrectOperationException {
        return delegate.createParameter(name, type, context);
    }

    @Override
    public @NotNull PsiElement createParameterList(String @NotNull [] names, PsiType @NotNull [] types) throws IncorrectOperationException {
        return delegate.createParameterList(names, types);
    }

    @Override
    public @NotNull PsiElement createMethodFromText(String text, @Nullable PsiElement context) {
        return delegate.createMethodFromText(text, context);
    }

    @Override
    public @NotNull PsiElement createAnnotationFromText(@NotNull String annotationText, @Nullable PsiElement context) throws IncorrectOperationException {
        return delegate.createAnnotationFromText(annotationText, context);
    }

    @Override
    public @NotNull PsiElement createExpressionFromText(@NotNull String text, @Nullable PsiElement context) throws IncorrectOperationException {
        return delegate.createExpressionFromText(text, context);
    }

    @Override
    public @NotNull PsiElement createReferenceElementByType(PsiClassType type) {
        return delegate.createReferenceElementByType(type);
    }

    @Override
    public @NotNull PsiElement createTypeParameterList() {
        return delegate.createTypeParameterList();
    }

    @Override
    public @NotNull PsiElement createTypeParameter(@NotNull String name, PsiClassType @NotNull [] superTypes) {
        return delegate.createTypeParameter(name, superTypes);
    }
/*
    @Override
    public @NotNull PsiElement createType(@NotNull PsiClass aClass) {
        return delegate.createType(aClass);
    }*/

    @Override
    public @NotNull PsiElement createAnnotationType(@NotNull String name) throws IncorrectOperationException {
        return delegate.createAnnotationType(name);
    }

    @Override
    public @NotNull PsiElement createConstructor(@NotNull String name) {
        return delegate.createConstructor(name);
    }

    @Override
    public @NotNull PsiElement createConstructor(@NotNull String name, @Nullable PsiElement context) {
        return delegate.createConstructor(name, context);
    }
/*

    @Override
    public @NotNull PsiElement createType(@NotNull PsiClass resolve, @NotNull PsiSubstitutor substitutor) {
        return delegate.createType(resolve, substitutor);
    }

    @Override
    public @NotNull PsiElement createType(@NotNull PsiClass resolve, @NotNull PsiSubstitutor substitutor, @Nullable LanguageLevel languageLevel) {
        return delegate.createType(resolve, substitutor, languageLevel);
    }

    @Override
    public @NotNull PsiElement createType(@NotNull PsiClass aClass, PsiType parameters) {
        return delegate.createType(aClass, parameters);
    }

    @Override
    public @NotNull PsiElement createType(@NotNull PsiClass aClass, PsiType... parameters) {
        return delegate.createType(aClass, parameters);
    }

    @Override
    public @NotNull PsiElement createRawSubstitutor(@NotNull PsiTypeParameterListOwner owner) {
        return delegate.createRawSubstitutor(owner);
    }

    @Override
    public @NotNull PsiElement createSubstitutor(@NotNull Map<PsiTypeParameter, PsiType> map) {
        return delegate.createSubstitutor(map);
    }

    @Override
    public @Nullable PsiElement createPrimitiveType(@NotNull String text) {
        return delegate.createPrimitiveType(text);
    }

    @Override
    public @NotNull PsiElement createTypeByFQClassName(@NotNull String qName) {
        return delegate.createTypeByFQClassName(qName);
    }

    @Override
    public @NotNull PsiElement createTypeByFQClassName(@NotNull String qName, @NotNull GlobalSearchScope resolveScope) {
        return delegate.createTypeByFQClassName(qName, resolveScope);
    }
*/

    @Override
    public @NotNull PsiElement createDocCommentFromText(@NotNull String text) {
        return delegate.createDocCommentFromText(text);
    }
}
