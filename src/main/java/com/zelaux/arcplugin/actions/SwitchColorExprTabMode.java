package com.zelaux.arcplugin.actions;

import com.intellij.execution.ui.layout.*;
import com.intellij.icons.*;
import com.intellij.ide.*;
import com.intellij.ide.actions.*;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.fileEditor.impl.*;
import com.intellij.openapi.project.*;
import com.intellij.openapi.vfs.*;
import com.intellij.openapi.wm.*;
import com.intellij.openapi.wm.impl.*;
import com.intellij.openapi.wm.impl.content.*;
import com.intellij.ui.*;
import com.intellij.ui.content.*;
import com.intellij.util.*;
import com.zelaux.arcplugin.settings.*;
import org.jetbrains.annotations.*;

import java.awt.*;
import java.util.*;

public class SwitchColorExprTabMode extends DumbAwareAction{


    @Override
    public void actionPerformed(@NotNull AnActionEvent e){
        ArcPluginSettingsState handler = ArcPluginSettingsState.getInstance();
        if(handler == null) return;
        boolean selected = !handler.viewColorExprSeqAsList;
        handler.viewColorExprSeqAsList = selected;
        Toggleable.setSelected(e.getPresentation(), selected);

        if(!e.isFromActionToolbar()) return;
        ActionToolbar toolbar = ActionToolbar.findToolbarBy(e.getInputEvent().getComponent());
        int i=0;
    }

    @Override
    public void update(@NotNull AnActionEvent e){
        ArcPluginSettingsState handler = ArcPluginSettingsState.getInstance();
        boolean enabled = handler != null;
        boolean selected = enabled && handler.viewColorExprSeqAsList;

        e.getPresentation().setIcon(e.isFromActionToolbar() ? AllIcons.General.Pin_tab : null);
        Toggleable.setSelected(e.getPresentation(), selected);
//        e.getPresentation().setText(selected ? IdeBundle.message("action.unpin.tab") : IdeBundle.message("action.pin.tab"));
        //noinspection DialogTitleCapitalization
        e.getPresentation().setText(selected ? "view as Tabs" : "view as List");
        e.getPresentation().setEnabledAndVisible(enabled);
    }


}
