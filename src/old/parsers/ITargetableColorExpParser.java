package com.zelaux.arcplugin.parsers;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.zelaux.arcplugin.utils.CustomUastTreeUtil;
import com.zelaux.arcplugin.utils.UastExpressionUtils;
import kotlin.jvm.functions.Function2;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.uast.UElement;
import org.jetbrains.uast.UastUtils;

import java.util.Objects;

public interface ITargetableColorExpParser<T extends ITargetableColorExpParser<T>> {
    Function2<T, UElement, UElement> getTargetSelector();
@NotNull
    UElement getElement();
@NotNull
    void setElement(UElement element);

    @SuppressWarnings("DataFlowIssue")
    default void replaceTarget(UElement target, String replacement) {
        PsiElement sourcePsi = Objects.requireNonNull(target.getSourcePsi());
        TextRange range = sourcePsi.getTextRange();
        PsiFile psiFile = sourcePsi.getContainingFile();
        Document document = PsiDocumentManager.getInstance(sourcePsi.getProject()).getDocument(psiFile);
        document.replaceString(range.getStartOffset(), range.getEndOffset(), replacement);
        UElement element= CustomUastTreeUtil.restore(psiFile,range,replacement.length()-range.getLength());


//        val newTarget = target.replace(replacement);
        setElement(element);
    }
    default UElement getTargetExpression(){
        //noinspection unchecked
        return ((T)this).getTargetSelector().invoke((T)this,getElement());
    }
}
