package com.zelaux.arcplugin.events.activities.poststatrup;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.ex.ActionUtil;
import com.intellij.openapi.extensions.ExtensionPoint;
import com.intellij.openapi.extensions.ExtensionPointName;
import com.intellij.openapi.extensions.ExtensionPointUtil;
import com.intellij.openapi.progress.util.BackgroundTaskUtil;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import com.intellij.openapi.util.Disposer;
import com.intellij.psi.util.CachedValue;
import com.intellij.util.Alarm;
import com.intellij.util.concurrency.AppExecutorUtil;
import com.zelaux.arcplugin.events.activities.EventIndexingManager;
import com.zelaux.arcplugin.utils.CheckedDisposable;
import com.zelaux.arcplugin.utils.cache.MyCachedValue;
import com.zelaux.arcplugin.utils.cache.MyParameterizedCachedValue;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

public class EventIndexingPostStartupActivity implements StartupActivity {


    @Override
    public void runActivity(@NotNull Project project) {
        EventIndexingManager.startup(project);
        DumbService.getInstance(project).runWhenSmart(() -> {
            AppExecutorUtil.getAppScheduledExecutorService().scheduleWithFixedDelay(new UpdateAction(project, getActivityDisposable(project)), 0, 500, TimeUnit.MILLISECONDS);
        });
//        ApplicationManager.getApplication().invokeLater(new TestRun(project));
        if (true) return;
    }

    @NotNull
    private Disposable getActivityDisposable(@NotNull Project project) {
        //noinspection UnresolvedPluginConfigReference
        ExtensionPoint<Object> point = new ExtensionPointName<>("com.intellij.postStartupActivity").getPoint();
        //        Disposer.register(project, activityDisposable);
        return ExtensionPointUtil.createExtensionDisposable(this, point);
    }

    private static class UpdateAction implements Runnable {
        @SuppressWarnings("unchecked")
        public final MyParameterizedCachedValue<?, CheckedDisposable>[] arr = new MyParameterizedCachedValue[]{
                EventIndexingManager.projectFirePoints,
                EventIndexingManager.projectEventSubscription,
                EventIndexingManager.librariesFirePoints,
                EventIndexingManager.librariesEventSubscription,
        };
        public final Project project;
        final Disposable parent;
        final ValueData[] disposables;

        public UpdateAction(@NotNull Project project, @NotNull Disposable parent) {
            this.parent = parent;
            this.project = project;
            disposables = new ValueData[arr.length];
            for (int i = 0; i < disposables.length; i++) {
                disposables[i] = new ValueData(project,arr[i]);
            }
        }

        @Override
        public void run() {
            if (ActionUtil.isDumbMode(project)) return;
            for (int i = 0, arrLength = arr.length; i < arrLength; i++) {
                MyParameterizedCachedValue<?, CheckedDisposable> cachedValue = arr[i];
                ValueData valueData = disposables[i];
                boolean needToUpdate=true;
                if(valueData.dependencies!=null){
                    needToUpdate= cachedValue.isUpToDate(valueData.dependencies);
                }
                if(needToUpdate){

                    CheckedDisposable disposable = valueData.disposable;
                    if (!disposable.isDisposed()) {
                        Disposer.dispose(disposable);
                    }
                    disposable.register(parent);
                    valueData.dependencies= cachedValue.currentValidTimeStamps();
                    valueData.postAlarm();
                }
            }
        }

      static   class ValueData implements Runnable {
            public final Project project;
            public final    MyParameterizedCachedValue<?, CheckedDisposable> cachedValue;

          public ValueData(Project project, MyParameterizedCachedValue<?, CheckedDisposable> cachedValue) {
              this.project = project;
              this.cachedValue = cachedValue;
          }

          long[] dependencies = null;
            Alarm myAlarm=new Alarm();
            void postAlarm(){
                myAlarm.cancelAllRequests();
                myAlarm.addRequest(this,1_000);
            }
            final CheckedDisposable disposable=new CheckedDisposable();

          @Override
          public void run() {
              DumbService instance = DumbService.getInstance(project);
              BackgroundTaskUtil.submitTask(disposable, ()->{
                  instance.runReadActionInSmartMode(()->cachedValue.getValue(disposable));
              });
          }
      }
    }
}
