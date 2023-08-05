package com.zelaux.arcplugin.interp.expressions.resolve;

import arc.math.Interp;
import arc.util.Reflect;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiField;
import com.zelaux.arcplugin.interp.expressions.render.InterpExpressionRenderer;
import com.zelaux.arcplugin.interp.expressions.render.InterpFieldExpressionRenderer;
import com.zelaux.arcplugin.langinfo.LanguageInfo;
import com.zelaux.arcplugin.langinfo.MyElementFactory;
import com.zelaux.arcplugin.utils.CustomUastTreeUtil;
import com.zelaux.arcplugin.utils.LazyValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.uast.UElement;
import org.jetbrains.uast.UExpression;
import org.jetbrains.uast.UField;

import java.util.Objects;

public class InterpFieldExpression extends InterpExpression {
    public LazyValue<@Nullable Interp> myInterpolation = LazyValue.create(() -> {
        UElement uElement = getUElement();
        UField currentField = CustomUastTreeUtil.resolveRecursiveField(uElement);
        if (currentField == null || currentField.getSourcePsi() == null) {
            return null;
        }
        PsiClass containingClass = ((PsiField) currentField.getSourcePsi()).getContainingClass();
        if (Objects.equals(containingClass.getQualifiedName(), Interp.class.getCanonicalName())) {
            return Reflect.get(Interp.class, currentField.getName());
        }
        return null;
    });

    public void replaceTarget(String newText) {
        executeAlarm(() -> {
            PsiElement sourcePsi = getUElement().getSourcePsi();
            if (sourcePsi == null) return;
            @Nullable MyElementFactory factory = LanguageInfo.elementFactoryFor(sourcePsi.getLanguage(), sourcePsi.getProject());
            if (factory == null) return;
            sourcePsi.replace(factory.createExpressionFromText(newText, sourcePsi));
            invalidateUElement();
        });
    }

    public InterpFieldExpression(@NotNull UExpression element) {
        super(element);
    }

    @Override
    public Interp getInterpolation() {
        return myInterpolation.get();
    }

    static void writeInterpAction(Project project, Runnable block) {
        WriteAction.run(() -> {
            CommandProcessor.getInstance().executeCommand(project, block, "Change Interp", null);
        });
    }

    @Override
    public InterpExpressionRenderer createRenderer() {
        return new InterpFieldExpressionRenderer(this);
    }
}
