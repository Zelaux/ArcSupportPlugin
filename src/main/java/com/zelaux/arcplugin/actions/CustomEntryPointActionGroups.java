package com.zelaux.arcplugin.actions;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.actionSystem.impl.*;

public class CustomEntryPointActionGroups{

    public static DefaultActionGroup getColorExprTabsEntryPoint(){
        AnAction source = ActionManager.getInstance().getAction("ColorExprTabsEntryPoint");
        source.getTemplatePresentation().putClientProperty(ActionButton.HIDE_DROPDOWN_ICON, Boolean.TRUE);
        return new DefaultActionGroup(source);
    }
}
