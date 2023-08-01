package com.zelaux.arcplugin.ui.componentUI;

import javax.swing.*;
import javax.swing.plaf.*;
import java.awt.*;

public class FixedSizeComponentUI extends ComponentUI{
    public Dimension size;

    public FixedSizeComponentUI(Dimension size){
        this.size = size;
    }

    @Override
    public Dimension getPreferredSize(JComponent c){
        return size;
    }
}
