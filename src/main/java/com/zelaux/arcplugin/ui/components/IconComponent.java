package com.zelaux.arcplugin.ui.components;

import com.intellij.icons.*;
import com.intellij.icons.AllIcons.*;

import javax.swing.*;
import javax.swing.plaf.*;
import java.awt.*;

public class IconComponent extends JComponent{
    public Icon icon;

    public IconComponent(Icon icon){
        this.icon = icon;
    }

    @Override
    protected void paintComponent(Graphics g){
        icon.paintIcon(this, g, 0, 0);
    }

    @Override
    public Dimension getMinimumSize(){
        return new Dimension(icon.getIconWidth(), icon.getIconHeight());
    }

    @Override
    public Dimension getMaximumSize(){
        return getMinimumSize();
    }

    @Override
    public Dimension getSize(){
        return getMinimumSize();
    }
}
