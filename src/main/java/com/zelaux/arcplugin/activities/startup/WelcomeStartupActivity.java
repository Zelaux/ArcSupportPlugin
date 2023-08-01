package com.zelaux.arcplugin.activities.startup;

import arc.graphics.*;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.*;
import com.intellij.openapi.startup.*;
import com.intellij.openapi.ui.*;
import com.intellij.openapi.ui.popup.*;
import com.intellij.openapi.wm.*;
import com.intellij.ui.awt.*;
import com.intellij.vcs.log.ui.actions.ResumeIndexingAction;
import com.zelaux.arcplugin.support.events.EventLineMarkerProvider;
import org.jetbrains.annotations.*;

import javax.swing.event.*;

public class WelcomeStartupActivity implements StartupActivity{
    @Override
    public void runActivity(@NotNull Project project){
        var statusBar = WindowManager.getInstance().getStatusBar(project);
        ReadAction.nonBlocking(() -> {
            for (Module module : ModuleManager.getInstance(project).getModules()) {
                EventLineMarkerProvider.getEventListenersCached(module, null);
                EventLineMarkerProvider.getFirePointsCached(module, null);
            }
        });
        /*EventLineMarkerProvider.*/
        JBPopupFactory.getInstance()
        .createHtmlTextBalloonBuilder(
        "ZelauxArcPlugin(v0.31) started",
        MessageType.INFO,
        /*KtSimpleNameReferenceDescriptorsImpl*/
        e -> {
            if(e.getEventType() == HyperlinkEvent.EventType.ACTIVATED){
//                enableAnnotations(project)
            }
        })
        .createBalloon()
        .show(RelativePoint.getCenterOf(statusBar.getComponent()), Balloon.Position.atRight);
//        Messages.showInfoMessage(project,"Hello from Test activity",project.getName());
    }
}
