package com.zelaux.arcplugin;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.util.*;
import org.jetbrains.annotations.*;

import javax.swing.*;

import static com.zelaux.arcplugin.Loader.load;

interface Loader{
    static Icon load(String path){
        return IconLoader.getIcon(path, PluginIcons.class);
    }
}

public interface PluginIcons{

    /** 16x16 */
    @NotNull Icon Graph = load("icons/graph16_16_2.svg");
    @NotNull Icon PluginIcon = load("META-INF/pluginIcon.svg");
    @NotNull Icon RECEIVER = AllIcons.Gutter.WriteAccess;
    @NotNull Icon SENDER = AllIcons.Gutter.ReadAccess;

}

