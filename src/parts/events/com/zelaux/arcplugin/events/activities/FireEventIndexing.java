package com.zelaux.arcplugin.events.activities;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiMethod;
import com.zelaux.arcplugin.events.EventType;
import com.zelaux.arcplugin.events.FireEventPoint;
import org.jetbrains.uast.UExpression;

public class FireEventIndexing implements EventIndexing<FireEventPoint> {

    PsiMethod[] methods = PsiMethod.EMPTY_ARRAY;

    public void setProject(Project project) {
        ;
    }

    @Override
    public PsiMethod[] methods() {
        return methods = EventIndexingManager.getArcEventsClass().findMethodsByName("fire", false);
    }

    @Override
    public FireEventPoint construct(UExpression expression, EventType eventType) {
        return new FireEventPoint(expression,eventType);
    }
}
