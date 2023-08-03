package com.zelaux.arcplugin.events.activities;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiMethod;
import com.zelaux.arcplugin.events.EventSubscription;
import com.zelaux.arcplugin.events.EventType;
import org.jetbrains.uast.UExpression;

public class ListenEventIndexing implements EventIndexing<EventSubscription> {

    PsiMethod[] methods = PsiMethod.EMPTY_ARRAY;
    Project project;

    public void setProject(Project project) {
        this.project = project;

    }

    @Override
    public PsiMethod[] methods() {
        PsiMethod[] a = EventIndexingManager.getArcEventsClass().findMethodsByName("on", false);
        PsiMethod[] b = EventIndexingManager.getArcEventsClass().findMethodsByName("run", false);

        if (methods == null || methods.length != a.length + b.length) methods = new PsiMethod[a.length + b.length];
        System.arraycopy(a, 0, methods, 0, a.length);
        System.arraycopy(b, 0, methods, a.length, b.length);
        return methods;
    }

    @Override
    public EventSubscription construct(UExpression expression, EventType eventType) {
        return new EventSubscription(expression, eventType);
    }
}
