package com.zelaux.arcplugin.activities;

import arc.struct.*;
import com.intellij.debugger.engine.evaluation.*;
import com.intellij.debugger.settings.*;
import com.intellij.debugger.settings.NodeRendererSettings.*;
import com.intellij.debugger.ui.tree.render.*;
import com.intellij.ide.highlighter.*;
import com.intellij.openapi.project.*;
import com.intellij.openapi.startup.*;
import com.zelaux.arcplugin.debugger.settings.*;
import org.jetbrains.annotations.*;

public class CustomNodeRendererSettingsSetup implements StartupActivity{
    @Override
    public void runActivity(@NotNull Project project){
        NodeRendererSettings instance = NodeRendererSettings.getInstance();
        RendererConfiguration renderers = instance.getCustomRenderers();

        CustomNodeRendererSettings.setup(instance,renderers);
    }

}
