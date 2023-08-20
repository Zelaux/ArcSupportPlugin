package com.zelaux.arcplugin.utils;

import com.intellij.codeInsight.TargetElementUtil;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.zelaux.arcplugin.utils.resolve.SourceResolver;
import com.zelaux.arcplugin.utils.resolve.StaticFieldResolver;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.uast.*;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

public class CustomUastTreeUtil {

    public static UElement restore(PsiFile file, int startOffset, int endOffset, int deltaLen) {
        PsiElement a = file.findElementAt(startOffset);
        int newEndIndex = endOffset + deltaLen - startOffset;
        PsiElement b = newEndIndex > 1 ? file.findElementAt(newEndIndex - 1) : a;
        return UastUtils.findContaining(PsiTreeUtil.findCommonParent(a, b), UElement.class);
    }

    public static UElement restore(PsiFile file, TextRange range, int deltaLen) {
        return restore(file, range.getStartOffset(), range.getEndOffset(), deltaLen);
    }

    @Nullable
    public static UField resolveRecursiveField(UElement element) {
        return tryResolve(element);
    }

    @Nullable
    private static UField tryResolve(UElement element) {
        UElement resolve = resolveElement(element);
        if (!(resolve instanceof UField resolveField)) {
            if (resolve instanceof ULocalVariable) {
                return tryResolve(((ULocalVariable) resolve).getUastInitializer());
            }
            return null;
        }

        PsiExpression initializer = StaticFieldResolver.resolveStaticInitializer((PsiField) resolveField.getSourcePsi());
        if(initializer!=null)return resolveField;
        /*
        List<PsiField> collect = Arrays.stream(sourceMirrorClass.getAllFields())
                .filter(it -> it.getName().equals(resolveField.getName()))
                .toList();
        if (collect.size() == 1 && collect.get(0).hasInitializer())
            return UastUtils.findContaining(collect.get(0), UField.class);*/
        return null;
    }

    @Nullable
    public static UElement resolveElement(UElement element) {
        PsiElement sourcePsi = element.getSourcePsi();

        if (sourcePsi == null) return null;
        Project project = sourcePsi.getProject();
        Document currentDocument = PsiDocumentManager.getInstance(project).getDocument(sourcePsi.getContainingFile());
        if (currentDocument == null) return null;
        Editor[] editors = EditorFactory.getInstance().getEditors(currentDocument, project);
        if (editors.length == 0) return null;
        Editor editor = editors[0];
//
        PsiReference reference = TargetElementUtil.findReference(editor, sourcePsi.getTextOffset());
        return reference == null ? null : UastUtils.findContaining(reference.resolve(), UElement.class);
//        return sourcePsi.getReference();
    }

    @Nullable
    public static String getFullName(UField field) {
        PsiElement sourcePsi = field.getSourcePsi();
        if (!(sourcePsi instanceof PsiField)) return null;
        UClass containingClass = getContainingClass(field);
        if (containingClass == null) return null;
        return containingClass.getQualifiedName() + "." + field.getName();
    }


    public static UClass getContainingClass(@NotNull UField currentField) {
        return UastUtils.getParentOfType(currentField, UClass.class);
    }

    public static UClass getContainingClass(@NotNull UMethod currentField) {
        return UastUtils.getParentOfType(currentField, UClass.class);
    }


    public static class ChangeAppliedListener<T> implements Runnable {
        public final T newValue;
        public final Supplier<T> currentData;
        public boolean useObjectsEquals=true;
        public final Runnable callback;

        public ChangeAppliedListener(T newValue, Supplier<T> currentData, Runnable callback) {
            this.newValue = newValue;
            this.currentData = currentData;
            this.callback = callback;
            run();
        }

        private boolean isEq(T currentValue, T newValue) {
            return !useObjectsEquals && currentValue == newValue || useObjectsEquals && Objects.equals(currentValue, newValue);
        }
        @Override
        public void run() {
            if (isEq(currentData.get(), (T) newValue)) {
                callback.run();
            } else {
                ApplicationManager.getApplication().invokeLater(this);
            }
        }
    }
}
