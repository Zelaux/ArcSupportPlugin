package com.zelaux.arcplugin.expressions.render;

import javax.swing.*;
import java.awt.*;

public class RenderUtils {
    public static void replaceComponent(JComponent from, JComponent to) {
        Container parent = from.getParent();
        var i = 0;
        while (parent.getComponent(i) != from) {
            i++;
        }
        parent.remove(i);
        parent.add(to, i);

        parent.repaint();
    }
}
