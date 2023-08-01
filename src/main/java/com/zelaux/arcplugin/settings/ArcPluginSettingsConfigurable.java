package com.zelaux.arcplugin.settings;

import com.intellij.openapi.options.*;
import com.zelaux.arcplugin.debugger.settings.*;
import org.jetbrains.annotations.*;

import javax.swing.*;

public class ArcPluginSettingsConfigurable implements Configurable{

    private ArcPluginSettingsComponent mySettingsComponent;

    // A default constructor with no arguments is required because this implementation
    // is registered as an applicationConfigurable EP

    @Nls(capitalization = Nls.Capitalization.Title)
    @Override
    public String getDisplayName(){
        return "ArcPlugin Settings";
    }

    @Override
    public JComponent getPreferredFocusedComponent(){
        return mySettingsComponent.getPreferredFocusedComponent();
    }

    @Nullable
    @Override
    public JComponent createComponent(){
        mySettingsComponent = new ArcPluginSettingsComponent();
        return mySettingsComponent.getPanel();
    }

    @Override
    public boolean isModified(){
        ArcPluginSettingsState settings = ArcPluginSettingsState.getInstance();
        boolean modified = false;
        modified |= mySettingsComponent.getViewColorExprSeqAsList() != settings.viewColorExprSeqAsList;
        modified |= mySettingsComponent.getEnabledDebugViewForObjectMap() != settings.enabledDebugViewForObjectMap;
        modified |= mySettingsComponent.getEnabledDebugViewForObjectMapEntry() != settings.enabledDebugViewForObjectMapEntry;
        modified |= mySettingsComponent.getEnabledDebugViewForSeq() != settings.enabledDebugViewForSeq;
        modified |= mySettingsComponent.getEnabledDebugViewForFi() != settings.enabledDebugViewForFi;
        modified |= mySettingsComponent.getEnabledDebugViewForColor() != settings.enabledDebugViewForColor;
        return modified;
    }

    @Override
    public void apply(){
        ArcPluginSettingsState settings = ArcPluginSettingsState.getInstance();
        settings.viewColorExprSeqAsList = mySettingsComponent.getViewColorExprSeqAsList();

        settings.enabledDebugViewForObjectMap = mySettingsComponent.getEnabledDebugViewForObjectMap();
        settings.enabledDebugViewForObjectMapEntry = mySettingsComponent.getEnabledDebugViewForObjectMapEntry();
        settings.enabledDebugViewForSeq = mySettingsComponent.getEnabledDebugViewForSeq();
        settings.enabledDebugViewForFi = mySettingsComponent.getEnabledDebugViewForFi();
        settings.enabledDebugViewForColor = mySettingsComponent.getEnabledDebugViewForColor();

        CustomNodeRendererSettings.updateState();

    }

    @Override
    public void reset(){
        ArcPluginSettingsState settings = ArcPluginSettingsState.getInstance();
        mySettingsComponent.setViewColorExprSeqAsList(settings.viewColorExprSeqAsList);

        mySettingsComponent.setEnabledDebugViewForObjectMap(settings.enabledDebugViewForObjectMap);
        mySettingsComponent.setEnabledDebugViewForObjectMapEntry(settings.enabledDebugViewForObjectMapEntry);
        mySettingsComponent.setEnabledDebugViewForSeq(settings.enabledDebugViewForSeq);
        mySettingsComponent.setEnabledDebugViewForFi(settings.enabledDebugViewForFi);
        mySettingsComponent.setEnabledDebugViewForColor(settings.enabledDebugViewForColor);
    }

    @Override
    public void disposeUIResources(){
        mySettingsComponent = null;
    }
}
