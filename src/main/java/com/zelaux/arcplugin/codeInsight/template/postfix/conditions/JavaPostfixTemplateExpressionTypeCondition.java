package com.zelaux.arcplugin.codeInsight.template.postfix.conditions;

import arc.func.*;
import com.intellij.codeInsight.template.postfix.templates.editable.*;
import com.intellij.openapi.util.*;
import com.intellij.psi.*;
import org.jdom.*;
import org.jetbrains.annotations.*;

import java.util.*;

public class JavaPostfixTemplateExpressionTypeCondition implements JavaPostfixTemplateExpressionCondition{
    public static final @NonNls String ID = "typeValid";
    public static final @NonNls String PRESENTABLE_NAME = "presentable-name";
    public final String myPresentableName;

    private final @NlsSafe Boolf<PsiType> myTypeValidator;

    public JavaPostfixTemplateExpressionTypeCondition(String presentableName, Boolf<@Nullable PsiType> typeValidator){
        this.myPresentableName = presentableName;
        this.myTypeValidator = typeValidator;
    }


    @Override
    public boolean value(@NotNull PsiExpression element) {
        PsiType type = element.getType();
        return myTypeValidator.get(type);
//        return type != null && InheritanceUtil.isInheritor(type, myFqn);
    }

    @NotNull
    @Override
    public String getId() {
        return ID;
    }

    @Override
    public @NotNull @Nls String getPresentableName() {
        return myPresentableName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JavaPostfixTemplateExpressionTypeCondition condition = (JavaPostfixTemplateExpressionTypeCondition)o;
        return Objects.equals(myTypeValidator, condition.myTypeValidator) && myPresentableName.equals(condition.myTypeValidator);
    }

    @Override
    public int hashCode() {
        return Objects.hash(myTypeValidator,myPresentableName);
    }

    @Override
    public void serializeTo(@NotNull Element element) {
        JavaPostfixTemplateExpressionCondition.super.serializeTo(element);
        element.setAttribute(PRESENTABLE_NAME, getPresentableName());
    }
}
