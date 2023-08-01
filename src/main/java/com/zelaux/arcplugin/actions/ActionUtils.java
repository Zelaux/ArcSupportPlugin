package com.zelaux.arcplugin.actions;

import com.intellij.openapi.actionSystem.*;
import com.intellij.util.ui.*;
import org.jetbrains.annotations.*;

import javax.swing.*;

public class ActionUtils{
    public static ActionToolbar createTabsToolbar(DefaultActionGroup group,@Nullable JComponent target) {
        final ActionToolbar toolbar = ActionManager.getInstance().createActionToolbar(ActionPlaces.TABS_MORE_TOOLBAR, group, true);
        if (target!=null){
            toolbar.setTargetComponent(target);
        }
        toolbar.getComponent().setBorder(JBUI.Borders.empty());
        toolbar.getComponent().setOpaque(false);
        toolbar.setLayoutPolicy(ActionToolbar.NOWRAP_LAYOUT_POLICY);
        return toolbar;
    }
}
