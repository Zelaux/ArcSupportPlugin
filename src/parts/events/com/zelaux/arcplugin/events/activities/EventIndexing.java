package com.zelaux.arcplugin.events.activities;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiMethod;
import com.zelaux.arcplugin.events.EventType;
import org.jetbrains.uast.UExpression;

public interface EventIndexing<T> {
    void setProject(Project project);
    PsiMethod[] methods();

    T construct(UExpression expression, EventType eventType);
}
