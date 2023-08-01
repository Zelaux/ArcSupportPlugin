package com.zelaux.arcplugin.utils.ui;

import com.intellij.openapi.ui.*;
import com.intellij.util.ui.*;
import org.jetbrains.annotations.*;

import javax.swing.*;
import java.awt.*;
import java.util.*;

import static java.lang.Math.ceil;

@SuppressWarnings("deprecation")
public class MultiIcon extends EmptyIcon{
    private final Icon[] myIcons;
    private final boolean myBorder;
    private final int myWidth;
    private final int myHeight;

    public MultiIcon(int width, int height, int colorWidth, int colorHeight, final boolean border, @NotNull Icon... icons){
        super(width, height);
        myWidth = colorWidth;
        myHeight = colorHeight;
        myBorder = border;
        myIcons = icons;
    }

    public MultiIcon(int size, int colorSize, final boolean border, @NotNull Icon... icons){
        this(size, size, colorSize, colorSize, border, icons);
    }

    public MultiIcon(int size, final boolean border, @NotNull Icon... icons){
        this(size, size, border, icons);
    }

    public MultiIcon(int size, @NotNull Icon... icons){
        this(size, false, icons);
    }

    protected MultiIcon(MultiIcon icon){
        super(icon);
        myIcons = icon.myIcons;
        myBorder = icon.myBorder;
        myWidth = icon.myWidth;
        myHeight = icon.myHeight;
    }

    @NotNull
    @Override
    public MultiIcon copy(){
        return new MultiIcon(this);
    }

    @Override
    public void paintIcon(final Component component, Graphics g, int x, int y){
        Graphics2D g2d = (Graphics2D)g.create();
        final GraphicsConfig config = GraphicsUtil.setupAAPainting(g2d);
        try{
            final int w = getIconWidth();
            final int h = getIconHeight();
            for(int i = 0; i < myIcons.length; i++){
                int drawX = i % 2 == 0 ? x : x + w / 2 + 1;
                int drawY = i < 2 ? y : y + h / 2 + 1;
                int drawWidth = w / 2 - 1;
                int drawHeight = h / 2 - 1;
                Icon icon = myIcons[i];
//                g2d.scale(drawWidth / (float)icon.getIconWidth(), drawHeight / (double)icon.getIconHeight());
                icon.paintIcon(component, g2d, drawX, drawY);
                /*RectanglePainter.FILL.paint(g2d,
                drawX,
                drawY,
                drawWidth,
                drawHeight, null);*/
                if(i == 3) break;
            }
        }catch(Exception e){
            g2d.dispose();
        }finally{
            config.restore();
        }
    }

    private int getColorWidth(){
        return (int)ceil(scaleVal(myWidth));
    }

    private int getColorHeight(){
        return (int)ceil(scaleVal(myHeight));
    }

    @Override
    public boolean equals(Object o){
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        if(!super.equals(o)) return false;

        MultiIcon icon = (MultiIcon)o;

        if(myBorder != icon.myBorder) return false;
        if(myWidth != icon.myWidth) return false;
        if(myHeight != icon.myHeight) return false;
        if(!Arrays.equals(myIcons, icon.myIcons)) return false;
        return true;
    }

    @Override
    public int hashCode(){
        int result = super.hashCode();
        result = 31 * result + Arrays.hashCode(myIcons);
        return result;
    }
}
