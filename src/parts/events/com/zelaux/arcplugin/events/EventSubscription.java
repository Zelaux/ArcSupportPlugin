package com.zelaux.arcplugin.events;

import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.uast.UCallExpression;
import org.jetbrains.uast.UExpression;
import org.jetbrains.uast.UastUtils;

public class EventSubscription {
    @Nullable
    public final EventType eventType;
    private final PsiAnchor elementRef;
    @Nullable
    public final PsiElement element;
    public EventSubscription(@NotNull UExpression expr, @Nullable EventType eventType) {
        this.eventType = eventType;
        PsiElement sourcePsi = access$findSourcePsi(expr);

        if (sourcePsi != null) {
            elementRef = PsiAnchor.create(sourcePsi);
            this.element=elementRef.retrieve();
        } else {
            elementRef = null;
            this.element=null;
        }
    }
    private static PsiElement findSourcePsi(UExpression expr) {
        UCallExpression callExpression = UastUtils.getUCallExpression(expr.getUastParent());
        if (callExpression == null) return expr.getSourcePsi();
        PsiElement sourcePsi = callExpression.getSourcePsi();
        return sourcePsi == null ? expr.getSourcePsi() : sourcePsi;
    }

    // $FF: synthetic method
    public static PsiElement access$findSourcePsi(UExpression expr) {
        return findSourcePsi(expr);
    }
}
