package com.zelaux.arcplugin.events.activities;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.ex.ActionUtil;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.zelaux.arcplugin.events.AutoUpdatableEventCache;
import com.zelaux.arcplugin.events.indexing.EventIndexingManager;
import com.zelaux.arcplugin.utils.CheckedDisposable;
import com.zelaux.arcplugin.utils.cache.MyParameterizedCachedValue;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

public class UpdateActivity implements Runnable {
    @SuppressWarnings("unchecked")
    public final Project project;
    final AutoUpdatableEventCache<?>[] updatableEventCaches;

    public UpdateActivity(@NotNull Project project) {
        this.project = project;
        updatableEventCaches = new AutoUpdatableEventCache<?>[]{
                EventIndexingManager.projectFirePoints,
                EventIndexingManager.projectEventSubscription,
                EventIndexingManager.librariesFirePoints,
                EventIndexingManager.librariesEventSubscription,
        };
    }

    @Override
    public void run() {
        if (ActionUtil.isDumbMode(project)) return;
        for (AutoUpdatableEventCache<?> eventCache : updatableEventCaches) {
            eventCache.checkCalculation();
        }
    }

}
