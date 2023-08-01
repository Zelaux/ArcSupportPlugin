package com.zelaux.arcplugin.settings;

import com.intellij.openapi.application.*;
import com.intellij.openapi.components.*;
import com.intellij.util.xmlb.*;
import org.jetbrains.annotations.*;
@State(
name = "com.zelaux.arcplugin.settings.ArcPluginSettingsState",
storages = @Storage("ArcPluginSettings.xml")
)
public class ArcPluginSettingsState implements PersistentStateComponent<ArcPluginSettingsState>{

    public boolean viewColorExprSeqAsList = false;

    public boolean enabledDebugViewForObjectMap = true;
    public boolean enabledDebugViewForObjectMapEntry = true;
    public boolean enabledDebugViewForSeq = true;

    public boolean enabledDebugViewForFi = true;
    public boolean enabledDebugViewForColor = true;

    public static ArcPluginSettingsState getInstance() {
        return ApplicationManager.getApplication().getService(ArcPluginSettingsState.class);
    }

    @Nullable
    @Override
    public ArcPluginSettingsState getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull ArcPluginSettingsState state) {
        XmlSerializerUtil.copyBean(state, this);
    }

}
