package com.zelaux.arcplugin.ui;

import org.jetbrains.annotations.*;

import java.awt.*;

public interface GrayScaleColorPickerListener{
    /**
     * Color was changed by user.
     */
    void colorChanged(float color);

    /**
     * Dialog was closed
     * @param color resulting color or {@code null} if dialog was cancelled.
     */
    void closed(float color);
}
