package com.zelaux.arcplugin.utils;

import arc.struct.Queue;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.ui.popup.JBPopupListener;
import com.intellij.openapi.ui.popup.LightweightWindowEvent;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.ui.awt.RelativePoint;
import org.jetbrains.annotations.NotNull;

public class BalloonQueue {

    private static final Queue<PopupDescriptor> list = new Queue<>();

    public static void showPopup(Project project, Balloon balloon) {
        StatusBar statusBar = WindowManager.getInstance().getStatusBar(project);
        RelativePoint relativePoint = RelativePoint.getCenterOf(statusBar.getComponent());
        Balloon.Position position = Balloon.Position.atRight;
        PopupDescriptor descriptor = new PopupDescriptor(balloon, relativePoint, position);
        list.addLast(descriptor);
        if (list.size == 1) {
            descriptor.run();
        }
        balloon.addListener(new JBPopupListener() {
            @Override
            public void onClosed(@NotNull LightweightWindowEvent event) {
                list.removeFirst();//Removing balloon
                if (list.isEmpty()) return;
                list.first().run();
            }
        });

    }

    static class PopupDescriptor {
        public final Balloon balloon;
        public final RelativePoint relativePoint;
        public final Balloon.Position position;

        public PopupDescriptor(Balloon balloon, RelativePoint relativePoint, Balloon.Position position) {
            this.balloon = balloon;
            this.relativePoint = relativePoint;
            this.position = position;
        }

        public void run() {
            balloon.show(relativePoint, position);

        }
    }
}