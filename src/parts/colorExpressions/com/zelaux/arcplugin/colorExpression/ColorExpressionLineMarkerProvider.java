package com.zelaux.arcplugin.colorExpression;

import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.codeInsight.daemon.MergeableLineMarkerInfo;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiIdentifier;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.util.PsiEditorUtil;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.Function;
import com.intellij.util.FunctionUtil;
import com.zelaux.arcplugin.MetaData;
import com.zelaux.arcplugin.expressions.render.ExpressionSequenceRenderer;
import com.zelaux.arcplugin.expressions.resolve.ArcColorExpressionSequence;
import com.zelaux.arcplugin.expressions.resolve.ExpressionSequence;
import com.zelaux.arcplugin.marker.ExpressionSequenceProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.uast.*;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class ColorExpressionLineMarkerProvider implements  ExpressionSequenceProvider {
    private static boolean isColorMethod(UCallExpression method) {
        PsiMethod resolved = method.resolve();
        PsiClass containingClass = resolved == null ? null : resolved.getContainingClass();
        if (containingClass == null || !MetaData.Color.PATH.equals(containingClass.getQualifiedName()))
            return false;
        return true;
    }

    @Override
    public ArcColorExpressionSequence expressionParserSequenceFrom(@NotNull PsiElement element) {
        UElement thisUast = UastUtils.findContaining(element, UIdentifier.class);
        if (thisUast==null) return null;
        UCallExpression method = UastUtils.findContaining(element, UCallExpression.class);
        if (method == null) return null;
        UIdentifier identifier = method.getMethodIdentifier();
        if (identifier != null) {
            if (identifier.getSourcePsi() != element) return null;
        } else {
            UReferenceExpression classReference = method.getClassReference();
            if (classReference != null) {//reference named
                if (!PsiTreeUtil.isAncestor(classReference.getSourcePsi(),element,true)) {
                    return null;
                }
            }
        }
        check:
        {
            UElement parent1 = method.getUastParent();
           /* if (!(parent1 instanceof UQualifiedReferenceExpression)) {
                return null;
            }*/
            if(parent1!=null) {
                UElement parent2 = parent1.getUastParent();
                if (parent2 instanceof UQualifiedReferenceExpression) {
                    UExpression selector = ((UQualifiedReferenceExpression) parent2).getSelector();
                    if (selector instanceof UCallExpression) {
                        ArcColorExpressionSequence resolved = ColorExpressionResolver.resolve(selector);
                        if(resolved!=null)return null;
//                        if (isColorMethod(((UCallExpression) selector))) return null;
                    }
                }
            }
//            if (!isColorMethod(method)) return null;
        }
        return ColorExpressionResolver.resolve(method);
    }

}
