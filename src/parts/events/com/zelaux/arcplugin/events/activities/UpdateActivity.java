package com.zelaux.arcplugin.events.activities;

import com.intellij.openapi.actionSystem.ex.ActionUtil;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.zelaux.arcplugin.events.AutoUpdatableEventCache;
import com.zelaux.arcplugin.events.indexing.EventIndexingManager;
import org.jetbrains.annotations.NotNull;

public class UpdateActivity implements Runnable{
    public final Project project;
    final AutoUpdatableEventCache<?>[] updatableEventCaches;
    @NotNull
    public final Runnable cancelCallback;
    boolean wasDump = false;

    public UpdateActivity(@NotNull Project project, @NotNull Runnable cancelCallback){
        this.project = project;
        this.cancelCallback = cancelCallback;
        updatableEventCaches = new AutoUpdatableEventCache<?>[]{
            EventIndexingManager.projectFirePoints,
            EventIndexingManager.projectEventSubscription,
            EventIndexingManager.librariesFirePoints,
            EventIndexingManager.librariesEventSubscription,
        };
    }

    @Override
    public void run(){
        if(project.isDisposed()){
            cancelCallback.run();
            return;
        }
        boolean dumbMode = ActionUtil.isDumbMode(project);
        if(dumbMode && !wasDump){
            DumbService.getInstance(project).runWhenSmart(() -> {
                for(AutoUpdatableEventCache<?> cache : updatableEventCaches){
                    if(cache.isLibraries) cache.checkCalculation();
                }
            });
        }
        wasDump = dumbMode;
        for(AutoUpdatableEventCache<?> eventCache : updatableEventCaches){
            if(!eventCache.isLibraries) eventCache.checkCalculation();
        }
    }

}
