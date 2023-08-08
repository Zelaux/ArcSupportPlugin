package com.zelaux.arcplugin.debugger.ui.tree.render;

import arc.files.*;
import com.intellij.debugger.settings.*;
import com.intellij.debugger.ui.tree.render.*;
import com.zelaux.arcplugin.settings.*;

public class FiObjectRenderer extends CompoundRendererProvider{   {
    System.out.println(getClass().getName());
}
    @Override
    protected String getName(){
        return "Fi";
    }
    @Override
    protected String getClassName(){
        return Fi.class.getName();
    }

    @Override
    protected ChildrenRenderer getChildrenRenderer(){
        return NodeRendererSettings.createExpressionChildrenRenderer("list()", null);
    }


    @Override
    protected boolean isEnabled(){
        return MySettingsState.getInstance().enabledDebugViewForFi;
    }
}
