package com.zelaux.arcplugin.events.activities;

import arc.Events;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootModificationTracker;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.CachedValue;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.util.PsiModificationTracker;
import com.intellij.util.SmartList;
import com.zelaux.arcplugin.events.EventSubscription;
import com.zelaux.arcplugin.events.EventType;
import com.zelaux.arcplugin.events.FireEventPoint;
import com.zelaux.arcplugin.events.activities.poststatrup.EventIndexingPostStartupActivity;
import com.zelaux.arcplugin.utils.CheckedDisposable;
import com.zelaux.arcplugin.utils.cache.MyCacheValuesFactory;
import com.zelaux.arcplugin.utils.cache.MyCachedValue;
import com.zelaux.arcplugin.utils.cache.MyParameterizedCachedValue;

import java.util.HashMap;

public class EventIndexingManager {
    public static final FireEventIndexing fireEventIndexing = new FireEventIndexing();
    public static final ListenEventIndexing listenEventIndexing = new ListenEventIndexing();
    public static final EventIndexing<?>[] allIndexing = {fireEventIndexing, listenEventIndexing};
//    public static final Key<CachedValue<HashMap<EventType, SmartList<EventSubscription>>>> moduleSubscriptions = Key.create(Events.class + "#_1");
//    public static final Key<CachedValue<HashMap<EventType, SmartList<EventSubscription>>>> projectSubscriptions = Key.create(Events.class + "#_2");
//    public static final Key<CachedValue<HashMap<EventType, SmartList<FireEventPoint>>>> moduleFirePoints = Key.create(Events.class + "#_3");
//    public static final Key<CachedValue<HashMap<EventType, SmartList<FireEventPoint>>>> projectFirePoints = Key.create(Events.class + "#_4");
public static MyParameterizedCachedValue<HashMap<EventType, SmartList<FireEventPoint>>, CheckedDisposable> projectFirePoints;
    public static MyParameterizedCachedValue<HashMap<EventType, SmartList<FireEventPoint>>, CheckedDisposable> librariesFirePoints;
    public static MyParameterizedCachedValue<HashMap<EventType, SmartList<EventSubscription>>, CheckedDisposable> projectEventSubscription;
    public static MyParameterizedCachedValue<HashMap<EventType, SmartList<EventSubscription>>, CheckedDisposable> librariesEventSubscription;
    public static CachedValue<PsiClass> arcEventsClass;
    public final Project project;

    public EventIndexingManager(Project project) {
        this.project = project;
    }
/*
    public static void runIndexing(Project project, Disposable parent) {
        BackgroundTaskUtil.submitTask(parent, new EventIndexingManager(project));
    }*/

    public static PsiClass getArcEventsClass() {
        return arcEventsClass.getValue();
    }

    public static boolean isEnabled() {
        return arcEventsClass.getValue() != null;
    }

    public static void startup(Project project) {
        CachedValuesManager cacheManager = CachedValuesManager.getManager(project);

        arcEventsClass = cacheManager.createCachedValue(() -> {
            JavaPsiFacade javaPsiFacade = JavaPsiFacade.getInstance(project);
            GlobalSearchScope projectScope = GlobalSearchScope.allScope(project);
            return CachedValueProvider.Result.create(javaPsiFacade.findClass(Events.class.getCanonicalName(), projectScope), ProjectRootModificationTracker.getInstance(project));
        });

        for (EventIndexing<?> indexing : EventIndexingManager.allIndexing) {
            indexing.setProject(project);
        }
//        if (isEnabled()) {
//        }
        MyCacheValuesFactory factory = MyCacheValuesFactory.getInstance(project);
        projectFirePoints =
        factory.createParameterizedCachedValue(
                        new MyCacheValueProvider<>(project,
                                fireEventIndexing,
                                GlobalSearchScope::projectScope,
                                PsiModificationTracker.MODIFICATION_COUNT));

        librariesFirePoints =
        factory.createParameterizedCachedValue(
                        new MyCacheValueProvider<>(project,
                                fireEventIndexing,
                                it -> GlobalSearchScope.notScope(GlobalSearchScope.projectScope(it))));


        projectEventSubscription =
        factory.createParameterizedCachedValue(
                        new MyCacheValueProvider<>(project,
                                listenEventIndexing,
                                GlobalSearchScope::projectScope,
                                PsiModificationTracker.MODIFICATION_COUNT));

        librariesEventSubscription =
        factory.createParameterizedCachedValue(
                        new MyCacheValueProvider<>(project,
                                listenEventIndexing,
                                it -> GlobalSearchScope.notScope(GlobalSearchScope.projectScope(it))));
    }


}
