package com.zelaux.arcplugin.events;


import arc.Events;
import com.intellij.codeInsight.JavaLibraryModificationTracker;
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo;
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider;
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder;
import com.intellij.openapi.editor.markup.GutterIconRenderer.Alignment;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.NotNullLazyValue;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.searches.MethodReferencesSearch;
import com.intellij.psi.util.CachedValue;
import com.intellij.psi.util.CachedValueProvider.Result;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.util.InheritanceUtil;
import com.intellij.psi.util.PsiModificationTracker;
import com.intellij.util.Query;
import com.intellij.util.SmartList;
import com.zelaux.arcplugin.PluginIcons;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.uast.*;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class EventLineMarkerProvider extends RelatedItemLineMarkerProvider {
    public static final Key<CachedValue<HashMap<EventType, SmartList<EventSubscription>>>> moduleSubscriptions = Key.create(Events.class + "#_1");
    public static final Key<CachedValue<HashMap<EventType, SmartList<EventSubscription>>>> projectSubscriptions = Key.create(Events.class + "#_2");
    public static final Key<CachedValue<HashMap<EventType, SmartList<FireEventPoint>>>> moduleFirePoints = Key.create(Events.class + "#_3");
    public static final Key<CachedValue<HashMap<EventType, SmartList<FireEventPoint>>>> projectFirePoints = Key.create(Events.class + "#_4");
    private static Module lastModule;

    @NotNull
    private static Project cacheDataHolder(Module module) {
        return module.getProject();
    }

    @Nullable
    private static PsiClass findEventsClass(Project project, @Nullable GlobalSearchScope scope) {
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

    @Nullable
    private static Module getModule(PsiElement element) {
        Module module = ModuleUtilCore.findModuleForPsiElement(element);
        if (module == null) {
            if (lastModule == null || true) {
//                element.getContainingFile().getVirtualFile().getPath().split("!")[0]
                while (!(element instanceof PsiClass) && element != null) {
                    element = element.getParent();
                }
                if (element == null) return null;
                String qualifiedName = ((PsiClass) element).getQualifiedName();
                if (qualifiedName == null) return null;

                Project project = element.getProject();
                for (Module sortedModule : ModuleManager.getInstance(project).getSortedModules()) {

                    GlobalSearchScope scope = sortedModule.getModuleWithDependenciesAndLibrariesScope(true);
                    if (JavaPsiFacade.getInstance(project).findClass(qualifiedName, scope) != null) {
                        lastModule = module = sortedModule;
                        break;
                    }
                }
            }

//            element.getContainingFile().getVirtualFile().getFileSystem()
//            return ModuleManager.getInstance(element.getProject()).;
        } else {
            lastModule = module;
        }
        return module;
    }

    public static Stream<FireEventPoint> getFirePointsCached(final Module module, EventType eventType) {
        CachedValuesManager cacheManager = CachedValuesManager.getManager(module.getProject());
        SmartList<FireEventPoint> dynamic = cacheManager.getCachedValue(cacheDataHolder(module), projectFirePoints, () -> {
            GlobalSearchScope scope = GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(module);
            return Result.create(
                    getFirePoints(module.getProject(), scope),
                    PsiModificationTracker.MODIFICATION_COUNT,
                    ProjectRootManager.getInstance(module.getProject())
            );
        }, false).get(eventType);
        SmartList<FireEventPoint> dependencies = cacheManager.getCachedValue(cacheDataHolder(module), moduleFirePoints, () -> {
//            GlobalSearchScope scope = GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(module);
            //noinspection UnstableApiUsage
            return Result.create(
                    getFirePoints(module.getProject(), null),
                    JavaLibraryModificationTracker.getInstance(module.getProject())
            );
        }, false).get(eventType);
        return getStream(dynamic, dependencies);
    }

    private static HashMap<EventType, SmartList<FireEventPoint>> getFirePoints(Project project, @Nullable GlobalSearchScope scope) {
        HashMap<EventType, SmartList<FireEventPoint>> points = new HashMap<>();
        PsiClass eventsClass = findEventsClass(project, scope);
        if (eventsClass == null) return points;

        for (PsiMethod fireMethod : eventsClass.findMethodsByName("fire", false)) {
            Query<PsiReference> entries;
            if (scope != null) {
                entries = MethodReferencesSearch.search(fireMethod, scope, true);
            } else {
                entries = MethodReferencesSearch.search(fireMethod, true);
            }
            for (PsiReference reference : entries) {
                if ((ModuleUtilCore.findModuleForPsiElement(reference.getElement()) == null) != (scope == null))
                    continue;
                UCallExpression expression = UastContextKt.getUastParentOfType(reference.getElement(), UCallExpression.class, false);
                if (expression != null) {
                    EventType eventType = extractType(expression);
                    SmartList<FireEventPoint> found = points.get(eventType);
                    if (found == null) {
                        points.put(eventType, found = new SmartList<>());
                    }
                    found.add(new FireEventPoint(expression.getValueArguments().get(0), eventType));
                }
            }
        }
        return points;
    }

    public static Stream<EventSubscription> getEventListenersCached(final Module module, EventType type) {
        CachedValuesManager cacheManager = CachedValuesManager.getManager(module.getProject());
        SmartList<EventSubscription> dynamic = cacheManager.getCachedValue(cacheDataHolder(module), projectSubscriptions, () -> {
            GlobalSearchScope scope = GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(module);
            return Result.create(
                    getEventListeners(module.getProject(), scope),
                    PsiModificationTracker.MODIFICATION_COUNT,
                    ProjectRootManager.getInstance(module.getProject())
            );
        }, false).get(type);
        SmartList<EventSubscription> dependencies = cacheManager.getCachedValue(cacheDataHolder(module), moduleSubscriptions, () -> {
//            GlobalSearchScope scope = GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(module);
            //noinspection UnstableApiUsage
            return Result.create(
                    getEventListeners(module.getProject(), null),
                    JavaLibraryModificationTracker.getInstance(module.getProject())
            );
        }, false).get(type);
        return getStream(dynamic, dependencies);
    }

    private static <T> Stream<T> getStream(SmartList<T> dynamic, SmartList<T> dependencies) {
        if (dynamic == null && dependencies == null) return Stream.of();
        if (dynamic != null && dependencies == null) return dynamic.stream();
        if (dynamic == null) return dependencies.stream();
        return Stream.concat(dynamic.stream(), dependencies.stream());
    }

    private static HashMap<EventType, SmartList<EventSubscription>> getEventListeners(Project project, @Nullable GlobalSearchScope scope) {
        HashMap<EventType, SmartList<EventSubscription>> collector = new HashMap<>();

        PsiClass eventsClass = findEventsClass(project, scope);
        if (eventsClass == null) return collector;
        for (String name : new String[]{"on", "run"}) {
            for (PsiMethod publishMethod : eventsClass.findMethodsByName(name, false)) {
                Query<PsiReference> entries;
                if (scope != null) {
                    entries = MethodReferencesSearch.search(publishMethod, scope, true);
                } else {
                    entries = MethodReferencesSearch.search(publishMethod, true);
                }
                entries.forEach(reference -> {
                    if ((ModuleUtilCore.findModuleForPsiElement(reference.getElement()) == null) != (scope == null))
                        return;
                    UCallExpression expression = UastContextKt.getUastParentOfType(reference.getElement(), UCallExpression.class, false);
                    if (expression != null) {
                        EventType eventType = extractType(expression);
                        SmartList<EventSubscription> found = collector.get(eventType);
                        if (found == null) {
                            collector.put(eventType, found = new SmartList<>());
                        }
                        found.add(new EventSubscription(expression, eventType));
                    }
                });
            }
        }
        return collector;
    }

    private static EventType extractType(UCallExpression expression) {
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

    @Override
    protected void collectNavigationMarkers(@NotNull PsiElement element, @NotNull Collection<? super RelatedItemLineMarkerInfo<?>> result) {
        Intrinsics.checkNotNullParameter(element, "element");
        Intrinsics.checkNotNullParameter(result, "result");
        UElement uParent = UastUtils.getUParentForIdentifier(element);
        UCallExpression uCallExpression = UastContextKt.toUElement(element, UCallExpression.class);
        Module module = getModule(element);

        if (uCallExpression != null && Intrinsics.areEqual(uCallExpression.getKind(), UastCallKind.METHOD_CALL))
            if (this.isPublishEventExpression(uCallExpression)) {
                final PsiElement sourcePsi = uCallExpression.getSourcePsi();
                PsiElement identifier = UElementKt.getSourcePsiElement(uCallExpression.getMethodIdentifier());
                if (identifier != null && sourcePsi != null) {
                    NavigationGutterIconBuilder<PsiElement> builder = NavigationGutterIconBuilder
                            .create(PluginIcons.SENDER)
                            .setAlignment(Alignment.LEFT)
                            .setTargets(NotNullLazyValue.createValue(() -> findEventListeners(sourcePsi)))
                            .setTooltipText("Navigate to event listeners");
                    ;

                    result.add(builder.createLineMarkerInfo(identifier));
                }
            } else if (this.isEventListenerExpression(uCallExpression)) {
                final PsiElement sourcePsi = uCallExpression.getSourcePsi();
                PsiElement identifier = UElementKt.getSourcePsiElement(uCallExpression.getMethodIdentifier());
                if (identifier != null && sourcePsi != null) {
                    NavigationGutterIconBuilder<PsiElement> builder = NavigationGutterIconBuilder
                            .create(PluginIcons.RECEIVER)
                            .setAlignment(Alignment.LEFT)
                            .setTargets(NotNullLazyValue.createValue(() -> getPublisherTargets(uCallExpression)))
                            .setTooltipText("Navigate to event publisher");
                    ;

                    result.add(builder.createLineMarkerInfo(identifier));
                }
            }

    }

    private List<PsiMethodCallExpression> getPublisherTargets(UCallExpression element) {
        if (element.getValueArgumentCount() != 2) return Collections.emptyList();

        EventType eventType = extractType(element);
        Module module = getModule(element.getSourcePsi());
        if (module == null) return Collections.emptyList();

        return getFirePoints(module, eventType)
                .filter(it -> it.element instanceof PsiMethodCallExpression)
                .map(it -> (PsiMethodCallExpression) (it.element)).collect(Collectors.toList());
    }

    private final Collection<PsiElement> findEventListeners(PsiElement psiElement) {
//        com.intellij.openapi.fileEditor.FileEditorManager.getInstance(psiElement.getProject())

        Module module = getModule(psiElement);
        if (module == null) return Collections.emptyList();

        UCallExpression uCallExpression = UastContextKt.toUElement(psiElement, UCallExpression.class);
        if (uCallExpression == null) return Collections.emptyList();

        if (uCallExpression.getValueArgumentCount() != 1) {
            return Collections.emptyList();
        }
        EventType eventType = extractType(uCallExpression);
        if (eventType == null) {
            return Collections.emptyList();
        }
        return this.getEventListeners(module, eventType)
                .filter(it -> it.element != null)
                .map(it -> it.element.getNavigationElement())
                .collect(Collectors.toList());
    }

    private Stream<FireEventPoint> getFirePoints(Module module, EventType eventType) {
        return getFirePointsCached(module, eventType);
    }

    private Stream<EventSubscription> getEventListeners(Module module, EventType eventType) {
        return getEventListenersCached(module, eventType);
    }

    private final boolean isPublishEventExpression(UCallExpression uMethodCall) {
        if (Intrinsics.areEqual(uMethodCall.getMethodName(), "fire")) {
            PsiMethod psiMethod = uMethodCall.resolve();
            if (psiMethod != null) {
                PsiClass targetClass = psiMethod.getContainingClass();
                return InheritanceUtil.isInheritor(targetClass, Events.class.getCanonicalName());
            }
        }

        return false;
    }

    private final boolean isEventListenerExpression(UCallExpression uMethodCall) {
        if (Intrinsics.areEqual(uMethodCall.getMethodName(), "on") || Intrinsics.areEqual(uMethodCall.getMethodName(), "run")) {
            PsiMethod psiMethod = uMethodCall.resolve();
            if (psiMethod != null) {
                PsiClass targetClass = psiMethod.getContainingClass();
                return InheritanceUtil.isInheritor(targetClass, Events.class.getCanonicalName());
            }
        }

        return false;
    }
}
