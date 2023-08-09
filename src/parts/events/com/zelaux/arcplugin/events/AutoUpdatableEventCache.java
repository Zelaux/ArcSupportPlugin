package com.zelaux.arcplugin.events;

import com.intellij.openapi.actionSystem.ex.ActionUtil;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.progress.util.BackgroundTaskUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.util.Alarm;
import com.intellij.util.SmartList;
import com.zelaux.arcplugin.settings.MySettingsState;
import com.zelaux.arcplugin.utils.CheckedDisposable;
import com.zelaux.arcplugin.utils.cache.MyParameterizedCachedValue;
import kotlin.Unit;

import java.util.HashMap;

public class AutoUpdatableEventCache<TYPE> implements Runnable {
    public final String name;
    public final Project project;
    public final boolean isLibraries;
    public final CheckedDisposable disposable;
    public final MyParameterizedCachedValue<HashMap<EventType, SmartList<TYPE>>, CheckedDisposable> cachedValue;
    final Alarm myAlarm = new Alarm();
    final Object myCalculationLock = new Object();
    long[] dependencies = null;
    private HashMap<EventType, SmartList<TYPE>> myOwnCache;

    public AutoUpdatableEventCache(String name,
                                   Project project,
                                   boolean isLibraries,
                                   CheckedDisposable disposable,
                                   MyParameterizedCachedValue<HashMap<EventType, SmartList<TYPE>>, CheckedDisposable> cachedValue) {
        this.name = name;
        this.project = project;
        this.isLibraries = isLibraries;
        this.disposable = disposable;
        this.cachedValue = cachedValue;
    }

    public HashMap<EventType, SmartList<TYPE>> getValue() {
        synchronized (myCalculationLock) {
            if (myOwnCache == null) {
                myOwnCache = cachedValue.getValue(null);
            }
            return myOwnCache;
        }
    }

    public void checkCalculation() {
        if (!MySettingsState.getInstance().backgroundEventIndexing) {
            return;
        }
        if (dependencies != null && cachedValue.isUpToDate(dependencies)) return;
        if (!disposable.isDisposed()) {
            Disposer.dispose(disposable);
        }
        dependencies = cachedValue.currentValidTimeStamps();
        postAlarm();
    }

    void postAlarm() {
        myAlarm.cancelAllRequests();
        myAlarm.addRequest(this, 666);
    }

    @Override
    public void run() {
        if (!disposable.isDisposed()) {
            Disposer.dispose(disposable);
        }
        if (ActionUtil.isDumbMode(project)) {
            postAlarm();
            return;
        }
        disposable.register();
        postBackgroundAction();
        /**/
    }

    private void postBackgroundAction() {
        if (isLibraries) {
            com.intellij.openapi.progress.ProgressKt.runBackgroundableTask(name, project, true, task -> {
                task.setFraction(0);
                synchronized (myCalculationLock) {
                    myOwnCache = cachedValue.getValue(disposable);
                }
                task.setFraction(1);
                return Unit.INSTANCE;
            });
        } else {
            BackgroundTaskUtil.submitTask(disposable, () -> {
                try {
                    synchronized (myCalculationLock) {
                        myOwnCache = cachedValue.getValue(disposable);
                    }
                } catch (ProcessCanceledException e) {
                }
            });
        }
    }
}
