package com.zelaux.arcplugin.events.indexing;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import com.zelaux.arcplugin.events.EventType;
import com.zelaux.arcplugin.events.FireEventPoint;
import org.jetbrains.uast.UExpression;

public class FireEventIndexing implements EventIndexing<FireEventPoint> {

    PsiMethod[] methods = PsiMethod.EMPTY_ARRAY;

    @Override
    public void setProject(Project project) {

    }

    @Override
    public PsiMethod[] methods() {
        PsiClass psiClass = EventIndexingManager.getArcEventsClass();
        if (psiClass == null) return methods = PsiMethod.EMPTY_ARRAY;
        return methods = psiClass.findMethodsByName("fire", false);
    }

    @Override
    public FireEventPoint construct(UExpression expression, EventType eventType) {
        return new FireEventPoint(expression, eventType);
    }
}
