package com.zelaux.arcplugin.settings;

import com.intellij.openapi.application.*;
import com.intellij.openapi.components.*;
import com.intellij.util.xmlb.*;
import com.zelaux.arcplugin.settings.processor.Separator;
import com.zelaux.arcplugin.settings.processor.SettingField;
import org.jetbrains.annotations.*;
@State(
name = "com.zelaux.arcplugin.settings.ArcPluginSettingsState",
storages = @Storage("ArcPluginSettings.xml")
)
public class MySettingsState implements PersistentStateComponent<MySettingsState>{

    @SettingField(title = "Special debug view for arc.struct.ObjectMap? ", component = "checkBox()")

    public boolean enabledDebugViewForObjectMap = true;
    @SettingField(title = "Special debug view for arc.struct.ObjectMap.Entry? ", component = "checkBox()")
    public boolean enabledDebugViewForObjectMapEntry = true;
    @SettingField(title = "Special debug view for arc.struct.Seq? ", component = "checkBox()")
    public boolean enabledDebugViewForSeq = true;
    @SettingField(title = "Special debug view for arc.files.Fi? ", component = "checkBox()")

    public boolean enabledDebugViewForFi = true;
    @SettingField(title = "Special debug view for arc.graphics.Color? ", component = "checkBox()")
    public boolean enabledDebugViewForColor = true;

    @SettingField(title = "View color expression sequence as list? ", component = "checkBox()")
    @Separator
    public boolean viewColorExprSeqAsList = false;
    @SettingField(title = "Do indexing for arc.Events in background? ", component = "checkBox()")
    @Separator
    public boolean backgroundEventIndexing = true;
    public static MySettingsState getInstance() {
        return ApplicationManager.getApplication().getService(MySettingsState.class);
    }

    @Nullable
    @Override
    public MySettingsState getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull MySettingsState state) {
        XmlSerializerUtil.copyBean(state, this);
    }

}
