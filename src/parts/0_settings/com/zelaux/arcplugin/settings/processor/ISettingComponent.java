package com.zelaux.arcplugin.settings.processor;

import javax.swing.*;

public interface ISettingComponent {
    void applyOn(Object object);
    void reset(Object object);

    JComponent getPanel();

    boolean isModified(Object object);
}
