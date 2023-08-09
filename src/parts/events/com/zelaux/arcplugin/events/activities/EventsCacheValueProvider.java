package com.zelaux.arcplugin.events.activities;

import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.application.ex.ApplicationManagerEx;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.PsiSearchHelper;
import com.intellij.psi.search.SearchRequestCollector;
import com.intellij.psi.search.SearchSession;
import com.intellij.psi.search.searches.MethodReferencesSearch;
import com.intellij.psi.util.CachedValue;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.util.ArrayUtil;
import com.intellij.util.Processor;
import com.intellij.util.SmartList;
import com.zelaux.arcplugin.events.EventType;
import com.zelaux.arcplugin.events.indexing.EventIndexing;
import com.zelaux.arcplugin.events.indexing.EventIndexingManager;
import com.zelaux.arcplugin.utils.CheckedDisposable;
import com.zelaux.arcplugin.utils.cache.MyParameterizedCachedValueProvider;
import kotlin.collections.CollectionsKt;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.uast.UCallExpression;
import org.jetbrains.uast.UastContextKt;

import java.util.HashMap;
import java.util.List;

import static com.zelaux.arcplugin.events.EventsUtils.extractType;


public class EventsCacheValueProvider<T> extends MyParameterizedCachedValueProvider<HashMap<EventType, SmartList<T>>, CheckedDisposable> {
    public final Project project;


    public final EventIndexing<T> indexing;
    public final ScopeProvider provider;
    public final CachedValue<NaNObject<PsiMethod[]>> targetMethods;
    private final List<Object> dependencies;

    public EventsCacheValueProvider(Project project, EventIndexing<T> indexing, ScopeProvider provider, Object... dependencies) {
        this.project = project;
        this.indexing = indexing;
        this.provider = provider;

        //noinspection unchecked
        this.dependencies = CollectionsKt.mutableListOf(EventIndexingManager.arcEventsClass);
        CollectionsKt.addAll(this.dependencies, dependencies);
        targetMethods = CachedValuesManager.getManager(project).createCachedValue(() -> {
            PsiMethod[] methods = PsiMethod.EMPTY_ARRAY;
            if (EventIndexingManager.isEnabled()) {
                if (ApplicationManagerEx.getApplicationEx().holdsReadLock()) {
                    methods = indexing.methods();
                } else {
                    methods = ReadAction.compute(indexing::methods);
                }
            }
            return CachedValueProvider.Result.create(new NaNObject<>(methods), EventIndexingManager.arcEventsClass);
        });
    }

    private HashMap<EventType, SmartList<T>> collect(@Nullable CheckedDisposable disposable, GlobalSearchScope scope) {
        HashMap<EventType, SmartList<T>> points = new HashMap<>();
        long start = System.nanoTime();
        PsiMethod[] methods = targetMethods.getValue().value;
        if (methods.length > 0) {
            SearchRequestCollector collector = new SearchRequestCollector(new SearchSession(methods));
            Processor<PsiReference> processor = reference -> {
                if (disposable != null && disposable.isDisposed()) return false;
                ProgressManager.getInstance().executeNonCancelableSection(() -> {
                    UCallExpression expression = UastContextKt.getUastParentOfType(reference.getElement(), UCallExpression.class, false);
                    if (expression != null) {
                        EventType eventType = extractType(expression);
                        SmartList<T> found = points.get(eventType);
                        if (found == null) {
                            points.put(eventType, found = new SmartList<>());
                        }
                        found.add(indexing.construct(expression.getValueArguments().get(0), eventType));
                    }
                });
                return disposable == null || !disposable.isDisposed();
            };
            for (PsiMethod method : methods) {
                MethodReferencesSearch.searchOptimized(method, scope, true, collector, processor);
                if (disposable != null && disposable.isDisposed()) break;
            }
            ProgressManager.checkCanceled();
            if (disposable != null && disposable.isDisposed()) throw new ProcessCanceledException();
            PsiSearchHelper.getInstance(project).processRequests(collector, it -> true);

        }
        long end = System.nanoTime();
        long delta = end - start;
        delta /= 1000L;//MICRO
        delta /= 1000L;//MILLI
        System.out.println("Time to index: " + (delta / 1000f) + "  (" + indexing + ", " + scope + ")");

        return points;
    }

    @Override
    public @Nullable HashMap<EventType, SmartList<T>> computeValue(CheckedDisposable disposable) {
        if (ApplicationManagerEx.getApplicationEx().holdsReadLock() || true) {
            return collect(disposable, provider.getScope(project));
        } else {
            return ReadAction.compute(() -> collect(disposable, provider.getScope(project)));
        }
    }

    @Override
    public Object[] dependencies() {
        return ArrayUtil.toObjectArray(dependencies);
    }

    public interface ScopeProvider {
        @NotNull GlobalSearchScope getScope(Project project);
    }

    private static class NaNObject<T> {
        public final T value;

        private NaNObject(T value) {
            this.value = value;
        }

    }
}