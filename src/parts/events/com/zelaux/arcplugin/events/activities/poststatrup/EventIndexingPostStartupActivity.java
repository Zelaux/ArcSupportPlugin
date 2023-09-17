package com.zelaux.arcplugin.events.activities.poststatrup;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.extensions.ExtensionPoint;
import com.intellij.openapi.extensions.ExtensionPointName;
import com.intellij.openapi.extensions.ExtensionPointUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import com.intellij.util.concurrency.AppExecutorUtil;
import com.zelaux.arcplugin.events.activities.UpdateActivity;
import com.zelaux.arcplugin.events.indexing.EventIndexingManager;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.*;

public class EventIndexingPostStartupActivity implements StartupActivity{

    private static Disposable myDisposable;

    public static Disposable getMyDisposable(){
        return myDisposable;
    }

    @Override
    public void runActivity(@NotNull Project project){
        myDisposable = getActivityDisposable(project);
        EventIndexingManager.startup(project);
        boolean[] canceled = {false};
        final Runnable[] it = {() -> canceled[0] = true};
        UpdateActivity command = new UpdateActivity(project, () -> it[0].run());
        ScheduledFuture<?> future = AppExecutorUtil.getAppScheduledExecutorService().scheduleWithFixedDelay(command, 10, 250, TimeUnit.MILLISECONDS);
        it[0] = () -> future.cancel(false);
        if(canceled[0]){
            it[0].run();
        }

    }

    @NotNull
    private Disposable getActivityDisposable(@NotNull Project project){
        //noinspection UnresolvedPluginConfigReference
        ExtensionPoint<Object> point = new ExtensionPointName<>("com.intellij.postStartupActivity").getPoint();
        //        Disposer.register(project, activityDisposable);
        return ExtensionPointUtil.createExtensionDisposable(this, point);
    }
}
