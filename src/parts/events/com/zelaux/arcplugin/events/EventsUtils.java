package com.zelaux.arcplugin.events;

import arc.Events;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.InheritanceUtil;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.uast.UCallExpression;
import org.jetbrains.uast.UExpression;

import java.util.List;
import java.util.Objects;

public class EventsUtils {
    public static EventType extractType(UCallExpression expression) {
        List<UExpression> arguments = expression.getValueArguments();
        UExpression typeExpression = arguments.get(0);
        //noinspection IfStatementWithIdenticalBranches
        if ("fire".equals(expression.getMethodName())) {
            typeExpression = arguments.get(0);
        } else {
            typeExpression = arguments.get(0);
        }
        if (InheritanceUtil.isInheritor(typeExpression.getExpressionType(), Enum.class.getCanonicalName())) {
            return EventType.EnumType.tryMakeEnumType(typeExpression);
        } else {
            return EventType.SimpleType.create(expression, typeExpression);
        }
    }
    @Nullable
    public static PsiClass findEventsClass(Project project, @Nullable GlobalSearchScope scope) {
        JavaPsiFacade javaPsiFacade = JavaPsiFacade.getInstance(Objects.requireNonNull(project));
        if (scope != null) {
            return javaPsiFacade.findClass(Events.class.getCanonicalName(), scope);
        } else {
            String qualifiedName = Events.class.getCanonicalName();

            for (Module sortedModule : ModuleManager.getInstance(project).getSortedModules()) {
                GlobalSearchScope scope_ = sortedModule.getModuleWithDependenciesAndLibrariesScope(true);
                PsiClass found = javaPsiFacade.findClass(qualifiedName, scope_);
                if (found != null) return found;
            }
        }
        return null;
    }
}
