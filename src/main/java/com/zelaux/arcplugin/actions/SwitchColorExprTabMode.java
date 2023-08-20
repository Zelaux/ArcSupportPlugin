/*
package com.zelaux.arcplugin.actions;

import com.intellij.icons.*;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.project.*;
import com.zelaux.arcplugin.settings.*;
import org.jetbrains.annotations.*;

public class SwitchColorExprTabMode extends DumbAwareAction{


    @Override
    public void actionPerformed(@NotNull AnActionEvent e){
        MySettingsState handler = MySettingsState.getInstance();
        if(handler == null) return;
        */
/*boolean selected = !handler.viewColorExprSeqAsList;
        handler.viewColorExprSeqAsList = selected;*//*

        Toggleable.setSelected(e.getPresentation(), selected);

        if(!e.isFromActionToolbar()) return;
        ActionToolbar toolbar = ActionToolbar.findToolbarBy(e.getInputEvent().getComponent());
        int i=0;
    }

    @Override
    public void update(@NotNull AnActionEvent e){
        MySettingsState handler = MySettingsState.getInstance();
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
*/
