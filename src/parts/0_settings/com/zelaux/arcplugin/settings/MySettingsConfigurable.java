package com.zelaux.arcplugin.settings;

import com.intellij.openapi.options.*;
import com.zelaux.arcplugin.settings.processor.ISettingComponent;
import com.zelaux.arcplugin.settings.processor.SettingsFieldProcessor;
import org.jetbrains.annotations.*;

import javax.swing.*;

public class MySettingsConfigurable implements Configurable{

    private ISettingComponent mySettingsComponent;

    // A default constructor with no arguments is required because this implementation
    // is registered as an applicationConfigurable EP

    @Nls(capitalization = Nls.Capitalization.Title)
    @Override
    public String getDisplayName(){
        return "Arc Support";
    }

    @Override
    public JComponent getPreferredFocusedComponent(){
        return mySettingsComponent.getPanel();
    }


    public final SettingsFieldProcessor processor = new SettingsFieldProcessor(MySettingsState.class);
    @Nullable
    @Override
    public JComponent createComponent(){
        MySettingsState.getInstance();
        mySettingsComponent = processor.settingComponentProv.invoke();
        return mySettingsComponent.getPanel();
    }

    @Override
    public boolean isModified(){
        MySettingsState settings = MySettingsState.getInstance();
        return mySettingsComponent.isModified(settings);

    }

    @Override
    public void apply(){
        MySettingsState settings = MySettingsState.getInstance();
        mySettingsComponent.applyOn(settings);
    }

    @Override
    public void reset(){
        MySettingsState settings = MySettingsState.getInstance();
        mySettingsComponent.reset(settings);
    }

    @Override
    public void disposeUIResources(){
        mySettingsComponent = null;
    }
}
