package com.zelaux.arcplugin.langinfo.impl.kotlin;

import com.intellij.openapi.project.Project;
import com.intellij.pom.java.LanguageLevel;
import com.intellij.psi.*;
import com.intellij.psi.javadoc.PsiDocComment;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.IncorrectOperationException;
import com.zelaux.arcplugin.langinfo.MyElementFactory;
import kotlin.jvm.JvmStatic;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.kotlin.psi.*;

import java.util.Map;
import java.util.StringJoiner;

public class KotlinElementFactory implements MyElementFactory {
    final KtPsiFactory ktPsiFactory;
    final Project project;

    public KotlinElementFactory(Project project) {
        this.project=project;
        this.ktPsiFactory = new KtPsiFactory(project);
    }
KtPsiFactory ktPsiFactory(PsiElement context){
        if(context==null)return ktPsiFactory;
        return new KtPsiFactory(project);
//        return KtPsiFactory.contextual(context);
//        new KtPsiFactory()
}
    @Override
    public @NotNull PsiElement createClass(@NotNull String name) throws IncorrectOperationException {
        return ktPsiFactory(null).createClass("class "+name);
    }

    @Override
    public @NotNull PsiElement createInterface(@NotNull String name) throws IncorrectOperationException {
        return ktPsiFactory(null).createClass("interface "+name);
    }

    @Override
    public @NotNull PsiElement createEnum(@NotNull String name) throws IncorrectOperationException {
        return ktPsiFactory(null).createClass("enum class " +name);
    }

    @Override
    public @NotNull PsiElement createField(@NotNull String name, @NotNull PsiType type) throws IncorrectOperationException {
        KtProperty property = ktPsiFactory(null).createProperty(name, type.getCanonicalText(), true);
        property.addAnnotationEntry(ktPsiFactory(null).createAnnotationEntry("@"+ JvmStatic.class.getCanonicalName()));
        return property;
    }

    @Override
    public @NotNull PsiElement createMethod(@NotNull String name, PsiType returnType) throws IncorrectOperationException {
        return ktPsiFactory(null).createFunction("fun "+name+"(): " + returnType+"{}");
    }

    @Override
    public @NotNull PsiElement createMethod(@NotNull String name, PsiType returnType, @Nullable PsiElement context) throws IncorrectOperationException {
        return ktPsiFactory(null).createFunction("fun "+name+"(): " + returnType+"{}");
    }

    @Override
    public @NotNull PsiElement createConstructor() {
        return ktPsiFactory(null).createSecondaryConstructor("constructor(){}");
    }

    @Override
    public @NotNull PsiElement createClassInitializer() {
        return ktPsiFactory(null).createClass("class F{{}}").getDeclarations().get(0);
    }

    @Override
    public @NotNull PsiElement createParameter(@NotNull String name, PsiType type) throws IncorrectOperationException {
        return ktPsiFactory(null).createParameter(name+':'+ type.getCanonicalText());
    }

    @Override
    public PsiElement createParameter(@NotNull String name, PsiType type, @Nullable PsiElement context) throws IncorrectOperationException {
        return applyContext(ktPsiFactory(context).createParameter(name+':'+ type.getCanonicalText()),context);
    }

    private PsiElement applyContext(PsiElement element, PsiElement context) {
        KtPsiFactoryKt.setAnalysisContext((KtFile)element.getContainingFile(),context);
        return element;
    }

    @Override
    public @NotNull PsiElement createParameterList(String @NotNull [] names, PsiType @NotNull [] types) throws IncorrectOperationException {
        StringJoiner builder = new StringJoiner(",");
        for (int i = 0; i < names.length; i++) {
            builder.add(names[i]+": "+types[i]);
        }
        return ktPsiFactory(null).createParameterList(builder.toString());
    }

    @Override
    public @NotNull PsiElement createMethodFromText(String text, @Nullable PsiElement context) {
        return applyContext(ktPsiFactory(context).createFunction(text),context);
    }

    @Override
    public @NotNull PsiElement createAnnotationFromText(@NotNull String annotationText, @Nullable PsiElement context) throws IncorrectOperationException {
        return applyContext(ktPsiFactory(context).createAnnotationEntry(annotationText),context);
    }

    @Override
    public @NotNull PsiElement createExpressionFromText(@NotNull String text, @Nullable PsiElement context) throws IncorrectOperationException {
        return applyContext(ktPsiFactory(context).createExpression(text),context);
    }

    @Override
    public @NotNull PsiElement createReferenceElementByType(PsiClassType type) {
        throw new UnsupportedOperationException();
    }
/*

    @Override
    public @NotNull PsiElement createReferenceElementByType(PsiClassType type) {
        return ktPsiFactory(null).createReferenceElementByType(type);
    }
*/

    @Override
    public @NotNull PsiElement createTypeParameterList() {
        return ktPsiFactory(null).createTypeParameterList("");
    }

    @Override
    public @NotNull PsiElement createTypeParameter(@NotNull String name, PsiClassType @NotNull [] superTypes) {
        if(superTypes.length>1) throw new IncorrectOperationException("superTypes cannot be more than one");
        return ktPsiFactory(null).createTypeParameter(superTypes.length==0?name:name+":"+superTypes[0]);
    }

    @Override
    public @NotNull PsiElement createAnnotationType(@NotNull String name) throws IncorrectOperationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public @NotNull PsiElement createConstructor(@NotNull String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public @NotNull PsiElement createConstructor(@NotNull String name, @Nullable PsiElement context) {
        throw new UnsupportedOperationException();
    }

    @Override
    public @NotNull PsiElement createDocCommentFromText(@NotNull String text) {
        throw new UnsupportedOperationException();
    }
/*
    @Override
    public @NotNull PsiElement createType(@NotNull PsiClass aClass) {
        return ktPsiFactory(null).createType(aClass);
    }*/

   /* @Override
    public @NotNull PsiElement createAnnotationType(@NotNull String name) throws IncorrectOperationException {
        return ktPsiFactory(null).createAnnotationType(name);
    }

    @Override
    public @NotNull PsiElement createConstructor(@NotNull String name) {
        return ktPsiFactory(null).createConstructor(name);
    }

    @Override
    public @NotNull PsiElement createConstructor(@NotNull String name, @Nullable PsiElement context) {
        return applyContext(ktPsiFactory(context).createConstructor(name, context),context);
    }*/
/*

    @Override
    public @NotNull PsiElement createType(@NotNull PsiClass resolve, @NotNull PsiSubstitutor substitutor) {
        return ktPsiFactory(null).createType(resolve, substitutor);
    }

    @Override
    public @NotNull PsiElement createType(@NotNull PsiClass resolve, @NotNull PsiSubstitutor substitutor, @Nullable LanguageLevel languageLevel) {
        return ktPsiFactory(null).createType(resolve, substitutor, languageLevel);
    }

    @Override
    public @NotNull PsiElement createType(@NotNull PsiClass aClass, PsiType parameters) {
        return ktPsiFactory(null).createType(aClass, parameters);
    }

    @Override
    public @NotNull PsiElement createType(@NotNull PsiClass aClass, PsiType... parameters) {
        return ktPsiFactory(null).createType(aClass, parameters);
    }

    @Override
    public @NotNull PsiElement createRawSubstitutor(@NotNull PsiTypeParameterListOwner owner) {
        return ktPsiFactory(null).createRawSubstitutor(owner);
    }

    @Override
    public @NotNull PsiElement createSubstitutor(@NotNull Map<PsiTypeParameter, PsiType> map) {
        return ktPsiFactory(null).createSubstitutor(map);
    }

    @Override
    public @Nullable PsiElement createPrimitiveType(@NotNull String text) {
        return ktPsiFactory(null).createPrimitiveType(text);
    }

    @Override
    public @NotNull PsiElement createTypeByFQClassName(@NotNull String qName) {
        return ktPsiFactory(null).createTypeByFQClassName(qName);
    }

    @Override
    public @NotNull PsiElement createTypeByFQClassName(@NotNull String qName, @NotNull GlobalSearchScope resolveScope) {
        return ktPsiFactory(null).createTypeByFQClassName(qName, resolveScope);
    }
*/

   /* @Override
    public @NotNull PsiElement createDocCommentFromText(@NotNull String text) {
        return ktPsiFactory(null).createDocCommentFromText(text);
    }*/
}
