package com.zelaux.arcplugin.activities.startup;

import arc.util.Reflect;
import arc.util.Structs;
import com.intellij.codeInsight.completion.CompletionConfidence;
import com.intellij.codeInsight.completion.CompletionConfidenceEP;
import com.intellij.codeInsight.completion.SkipAutopopupInStrings;
import com.intellij.lang.LanguageExtension;
import com.intellij.lang.java.JavaLanguage;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.KeyedExtensionCollector;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.ui.awt.RelativePoint;
import com.zelaux.arcplugin.support.events.EventLineMarkerProvider;
import org.jetbrains.annotations.NotNull;

import javax.swing.event.HyperlinkEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentMap;

public class WelcomeStartupActivity implements StartupActivity {
    @Override
    public void runActivity(@NotNull Project project) {
        var statusBar = WindowManager.getInstance().getStatusBar(project);
        ReadAction.nonBlocking(() -> {
            for (Module module : ModuleManager.getInstance(project).getModules()) {
                EventLineMarkerProvider.getEventListenersCached(module, null);
                EventLineMarkerProvider.getFirePointsCached(module, null);
            }
        });
        moveSomeStuff();

        /*EventLineMarkerProvider.*/
        JBPopupFactory.getInstance()
                .createHtmlTextBalloonBuilder(
                        "ZelauxArcPlugin(v0.31) started",
                        MessageType.INFO,
                        /*KtSimpleNameReferenceDescriptorsImpl*/
                        e -> {
                            if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
//                enableAnnotations(project)
                            }
                        })
                .createBalloon()
                .show(RelativePoint.getCenterOf(statusBar.getComponent()), Balloon.Position.atRight);
//        Messages.showInfoMessage(project,"Hello from Test activity",project.getName());
    }

    private void moveSomeStuff() {
        ApplicationManager.getApplication().invokeLater(() -> {
            LanguageExtension<CompletionConfidence> extension = Reflect.get(CompletionConfidenceEP.class, "INSTANCE");
            Key<List<CompletionConfidence>> myAllCacheKey = Reflect.get(LanguageExtension.class, extension, "myAllCacheKey");
            ConcurrentMap<String, List<CompletionConfidence>> myCache = Reflect.get(KeyedExtensionCollector.class, extension, "myCache");

            JavaLanguage language = JavaLanguage.INSTANCE;
            List<CompletionConfidence> confidences = new ArrayList<>(extension.allForLanguage(language));
            confidences.sort(Structs.comparingBool(it -> it instanceof SkipAutopopupInStrings));

            myCache.put(language.getID(), confidences);
            language.putUserData(myAllCacheKey, confidences);
        });
    }
}
