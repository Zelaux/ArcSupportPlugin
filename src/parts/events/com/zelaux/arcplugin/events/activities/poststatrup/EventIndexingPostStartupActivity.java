package com.zelaux.arcplugin.events.activities.poststatrup;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.ex.ActionUtil;
import com.intellij.openapi.extensions.ExtensionPoint;
import com.intellij.openapi.extensions.ExtensionPointName;
import com.intellij.openapi.extensions.ExtensionPointUtil;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.progress.util.BackgroundTaskUtil;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import com.intellij.openapi.util.Disposer;
import com.intellij.util.Alarm;
import com.intellij.util.concurrency.AppExecutorUtil;
import com.zelaux.arcplugin.events.activities.UpdateActivity;
import com.zelaux.arcplugin.events.indexing.EventIndexingManager;
import com.zelaux.arcplugin.utils.CheckedDisposable;
import com.zelaux.arcplugin.utils.cache.MyParameterizedCachedValue;
import kotlin.Unit;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

public class EventIndexingPostStartupActivity implements StartupActivity {

private static Disposable myDisposable;

    public static Disposable getMyDisposable() {
        return myDisposable;
    }

    @Override
    public void runActivity(@NotNull Project project) {
        myDisposable=getActivityDisposable(project);
        EventIndexingManager.startup(project);
        AppExecutorUtil.getAppScheduledExecutorService().scheduleWithFixedDelay(new UpdateActivity(project), 10, 250, TimeUnit.MILLISECONDS);

    }

    @NotNull
    private Disposable getActivityDisposable(@NotNull Project project) {
        //noinspection UnresolvedPluginConfigReference
        ExtensionPoint<Object> point = new ExtensionPointName<>("com.intellij.postStartupActivity").getPoint();
        //        Disposer.register(project, activityDisposable);
        return ExtensionPointUtil.createExtensionDisposable(this, point);
    }
}
