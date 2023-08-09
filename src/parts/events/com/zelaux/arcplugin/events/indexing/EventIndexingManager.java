package com.zelaux.arcplugin.events.indexing;

import arc.Events;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.application.ex.ApplicationManagerEx;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootModificationTracker;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.CachedValue;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.util.PsiModificationTracker;
import com.zelaux.arcplugin.events.AutoUpdatableEventCache;
import com.zelaux.arcplugin.events.EventSubscription;
import com.zelaux.arcplugin.events.FireEventPoint;
import com.zelaux.arcplugin.events.activities.EventsCacheValueProvider;
import com.zelaux.arcplugin.events.activities.poststatrup.EventIndexingPostStartupActivity;
import com.zelaux.arcplugin.utils.CheckedDisposable;
import com.zelaux.arcplugin.utils.cache.MyCacheValuesFactory;
import org.jetbrains.annotations.Nullable;

public class EventIndexingManager {
    public static final FireEventIndexing fireEventIndexing = new FireEventIndexing();
    public static final ListenEventIndexing listenEventIndexing = new ListenEventIndexing();
    public static final EventIndexing<?>[] allIndexing = {fireEventIndexing, listenEventIndexing};

    //    public static final Key<CachedValue<HashMap<EventType, SmartList<EventSubscription>>>> moduleSubscriptions = Key.create(Events.class + "#_1");
//    public static final Key<CachedValue<HashMap<EventType, SmartList<EventSubscription>>>> projectSubscriptions = Key.create(Events.class + "#_2");
//    public static final Key<CachedValue<HashMap<EventType, SmartList<FireEventPoint>>>> moduleFirePoints = Key.create(Events.class + "#_3");
//    public static final Key<CachedValue<HashMap<EventType, SmartList<FireEventPoint>>>> projectFirePoints = Key.create(Events.class + "#_4");
    public static AutoUpdatableEventCache<FireEventPoint> projectFirePoints;
    public static AutoUpdatableEventCache<FireEventPoint> librariesFirePoints;
    public static AutoUpdatableEventCache<EventSubscription> projectEventSubscription;
    public static AutoUpdatableEventCache<EventSubscription> librariesEventSubscription;
    public static CachedValue<PsiClass> arcEventsClass;
    public final Project project;
/*
    public static void runIndexing(Project project, Disposable parent) {
        BackgroundTaskUtil.submitTask(parent, new EventIndexingManager(project));
    }*/

    public EventIndexingManager(Project project) {
        this.project = project;
    }

    public static PsiClass getArcEventsClass() {
        return arcEventsClass.getValue();
    }

    public static boolean isEnabled() {
        return arcEventsClass.getValue() != null;
    }

    public static void startup(Project project) {
        CachedValuesManager cacheManager = CachedValuesManager.getManager(project);

        arcEventsClass = cacheManager.createCachedValue(new CachedValueProvider<PsiClass>() {
            @Override
            public @Nullable Result<PsiClass> compute() {
                if(ApplicationManagerEx.getApplicationEx().holdsReadLock()){
                    return getClass_();
                }else{
                    return ReadAction.compute(this::getClass_);
                }
            }
            Result<PsiClass> getClass_(){
                JavaPsiFacade javaPsiFacade = JavaPsiFacade.getInstance(project);
                GlobalSearchScope projectScope = GlobalSearchScope.allScope(project);
                return CachedValueProvider.Result.create(javaPsiFacade.findClass(Events.class.getCanonicalName(), projectScope), ProjectRootModificationTracker.getInstance(project));
            }
        });

        for (EventIndexing<?> indexing : EventIndexingManager.allIndexing) {
            indexing.setProject(project);
        }
//        if (isEnabled()) {
//        }

        projectFirePoints = createCache("project fire points", project, fireEventIndexing, false,
                PsiModificationTracker.MODIFICATION_COUNT
        );
        projectEventSubscription = createCache("project listen points", project, listenEventIndexing, false,
                PsiModificationTracker.MODIFICATION_COUNT
        );
        librariesFirePoints = createCache("libraries fire points", project, fireEventIndexing, true);
        librariesEventSubscription = createCache("libraries listen points", project, listenEventIndexing, true);
    }

    private static <T> AutoUpdatableEventCache<T> createCache(String name, Project project,
                                                              EventIndexing<T> indexing,
                                                              boolean isLibraries,
                                                              Object... dependencies
    ) {
//        MyParameterizedCachedValue<HashMap<EventType, SmartList<T>>, CheckedDisposable> cachedValue = factory.createParameterizedCachedValue(

        MyCacheValuesFactory factory = MyCacheValuesFactory.getInstance(project);
        var cachedValue = factory.createParameterizedCachedValue(
                new EventsCacheValueProvider<>(project,
                        indexing,
                        isLibraries ? (it -> GlobalSearchScope.notScope(GlobalSearchScope.projectScope(it))) : GlobalSearchScope::projectScope,
                        dependencies));
        Disposable disposable = EventIndexingPostStartupActivity.getMyDisposable();

        return new AutoUpdatableEventCache<>(name, project, isLibraries, new CheckedDisposable(disposable), cachedValue);
    }


}
