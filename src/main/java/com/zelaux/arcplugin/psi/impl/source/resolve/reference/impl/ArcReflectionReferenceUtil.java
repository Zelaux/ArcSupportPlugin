package com.zelaux.arcplugin.psi.impl.source.resolve.reference.impl;

import com.intellij.codeInsight.completion.InsertHandler;
import com.intellij.codeInsight.completion.InsertionContext;
import com.intellij.codeInsight.completion.PrioritizedLookupElement;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.psi.*;
import com.intellij.psi.codeStyle.JavaCodeStyleManager;
import com.intellij.psi.impl.source.resolve.reference.impl.JavaReflectionReferenceUtil;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiUtilCore;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.StringJoiner;

import static com.intellij.psi.impl.source.resolve.reference.impl.JavaReflectionReferenceUtil.*;

public class ArcReflectionReferenceUtil {
    public static final String GET = "get";
    public static final String SET = "set";
    public static final String INVOKE = "invoke";
    public static final String MAKE = "make";

    @Contract("null -> false")
    static boolean isRegularMethod(@Nullable PsiMethod method) {
        return method != null && !method.isConstructor();
    }

    static boolean isPublic(@NotNull PsiMember member) {
        return member.hasModifierProperty(PsiModifier.PUBLIC);
    }

    static boolean isAtomicallyUpdateable(@NotNull PsiField field) {
        if (field.hasModifierProperty(PsiModifier.STATIC) || !field.hasModifierProperty(PsiModifier.VOLATILE)) {
            return false;
        }
        final PsiType type = field.getType();
        return !(type instanceof PsiPrimitiveType) || PsiType.INT.equals(type) || PsiType.LONG.equals(type);
    }

    @Nullable
    static String getParameterTypesText(@NotNull PsiMethod method) {
        final StringJoiner joiner = new StringJoiner(", ");
        for (PsiParameter parameter : method.getParameterList().getParameters()) {
            final String typeText = getTypeText(parameter.getType());
            joiner.add(typeText + ".class");
        }
        return joiner.toString();
    }

    static void shortenArgumentsClassReferences(@NotNull InsertionContext context) {
        final PsiElement parameter = PsiUtilCore.getElementAtOffset(context.getFile(), context.getStartOffset());
        final PsiExpressionList parameterList = PsiTreeUtil.getParentOfType(parameter, PsiExpressionList.class);
        if (parameterList != null && parameterList.getParent() instanceof PsiMethodCallExpression) {
            JavaCodeStyleManager.getInstance(context.getProject()).shortenClassReferences(parameterList);
        }
    }

    @NotNull
    static LookupElement withPriority(@NotNull LookupElement lookupElement, boolean hasPriority) {
        return hasPriority ? lookupElement : PrioritizedLookupElement.withPriority(lookupElement, -1);
    }

    @Nullable
    static LookupElement withPriority(@Nullable LookupElement lookupElement, int priority) {
        return priority == 0 || lookupElement == null ? lookupElement : PrioritizedLookupElement.withPriority(lookupElement, priority);
    }

    static int getMethodSortOrder(@NotNull PsiMethod method) {
        return isJavaLangObject(method.getContainingClass()) ? 1 : isPublic(method) ? -1 : 0;
    }


    @Contract("null -> false")
    static boolean isJavaLangObject(@Nullable PsiClass aClass) {
        return isClassWithName(aClass, CommonClassNames.JAVA_LANG_OBJECT);
    }
    @Nullable
    static String getMemberType(@Nullable PsiElement element) {
        final PsiMethodCallExpression methodCall = PsiTreeUtil.getParentOfType(element, PsiMethodCallExpression.class);
        return methodCall != null ? methodCall.getMethodExpression().getReferenceName() : null;
    }

    @Nullable
    static LookupElement lookupMethod(@NotNull PsiMethod method, @Nullable InsertHandler<LookupElement> insertHandler) {
        final JavaReflectionReferenceUtil.ReflectiveSignature signature = getMethodSignature(method);
        return signature != null
                ? LookupElementBuilder.create(signature, method.getName())
                .withIcon(signature.getIcon())
                .withTailText(signature.getShortArgumentTypes())
                .withInsertHandler(insertHandler)
                : null;
    }

    static void replaceText(@NotNull InsertionContext context, @NotNull String text) {
        final PsiElement newElement = PsiUtilCore.getElementAtOffset(context.getFile(), context.getStartOffset());
        final PsiElement params = newElement.getParent().getParent();
        final int end = params.getTextRange().getEndOffset() - 1;
        final int start = Math.min(newElement.getTextRange().getEndOffset(), end);

        context.getDocument().replaceString(start, end, text);
        context.commitDocument();
        shortenArgumentsClassReferences(context);
    }

}
