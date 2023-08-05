package com.zelaux.arcplugin.expressions.resolve;

import arc.math.Interp;
import com.intellij.psi.PsiElement;
import com.intellij.psi.SmartPointerManager;
import com.intellij.psi.SmartPsiElementPointer;
import com.zelaux.arcplugin.expressions.render.ExpressionRenderer;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.uast.UElement;
import org.jetbrains.uast.UastUtils;

public abstract class Expression<SEQUENCE_TYPE extends ExpressionSequence<?>> {
    public final Class<? extends UElement> uelementClass;
    public final String tabTitle;
    private final SmartPsiElementPointer<PsiElement> pointer;
    public boolean isPointless;
    public boolean unresolved;
    private UElement uElement;

    public Expression(UElement uElement, Class<? extends UElement> uelementClass, String tabTitle) {
        Intrinsics.checkNotNullParameter(uElement, "uElement");
        Intrinsics.checkNotNullParameter(uelementClass, "uelementClass");
        Intrinsics.checkNotNullParameter(tabTitle, "tabTitle");
        this.tabTitle = tabTitle;
        this.uelementClass = uelementClass;
        this.uElement = uElement;
        PsiElement sourcePsi = uElement.getSourcePsi();
        Intrinsics.checkNotNull(sourcePsi, "uElement.getSourcePsi()");
        //noinspection DataFlowIssue
        pointer = SmartPointerManager.createPointer(sourcePsi);
    }

    public String calculateTabTitle() {
        return tabTitle;
    }

    public abstract SEQUENCE_TYPE asSequence();

    public abstract ExpressionRenderer<SEQUENCE_TYPE, ?> createRenderer();

    public void invalidateUElement() {
        uElement = null;
    }

    protected UElement calculateUElement(PsiElement psiElement) {
        return UastUtils.findContaining(psiElement, uelementClass);
    }

    public final UElement getUElement() {
        PsiElement element = pointer.getElement();
        if (uElement == null || uElement.getSourcePsi() != element) {
            uElement = calculateUElement(element);
        }
        return uElement;
    }

    public boolean isEntryPoint() {
        return this instanceof ExpressionEntryPoint;//TODO entry pointI
    }

    public boolean isDynamic() {
        return true;
    }

    public final <T extends UElement> T castElement() {
        return (T) getUElement();
    }

    public interface ExpressionEntryPoint {

    }
}
