package com.zelaux.arcplugin;

import com.intellij.psi.*;
import com.zelaux.arcplugin.utils.*;
import org.jetbrains.annotations.*;
import org.jetbrains.uast.*;

import java.awt.*;

public class ColorOp{
    /** Returns the color encoded as hex string with the format RRGGBBAA. */
    public static String toString(Color color){
        StringBuilder value = new StringBuilder();
        toString(color, value);
        return value.toString();
    }

    static void toString(Color color, StringBuilder builder){
        builder.append(Integer.toHexString(((color.getRed()) << 24) | ((color.getGreen()) << 16) | ((color.getBlue()) << 8) | ((color.getAlpha()))));
        while(builder.length() < 8)
            builder.insert(0, "0");
    }

    public static float[] toHsv(Color color, float[] hsv){
        float r = color.getRed() / 255f;
        float g = color.getGreen() / 255f;
        float b = color.getBlue() / 255f;

        float max = Math.max(Math.max(r, g), b);
        float min = Math.min(Math.min(r, g), b);
        float range = max - min;
        if(range == 0){
            hsv[0] = 0;
        }else if(max == r){
            hsv[0] = (60 * (g - b) / range + 360) % 360;
        }else if(max == g){
            hsv[0] = 60 * (b - r) / range + 120;
        }else{
            hsv[0] = 60 * (r - g) / range + 240;
        }

        if(max > 0){
            hsv[1] = 1 - min / max;
        }else{
            hsv[1] = 0;
        }

        hsv[2] = max;

        return hsv;
    }

    @SuppressWarnings("UseJBColor")
    @Nullable
    public static Color valueOf(@NotNull String hex){
        if(!hex.matches("#?([\\da-fA-F]{6}|[\\da-fA-F]{8})")) return null;
        int offset = hex.charAt(0) == '#' ? 1 : 0;

        int r = parseHex(hex, offset, offset + 2);
        int g = parseHex(hex, offset + 2, offset + 4);
        int b = parseHex(hex, offset + 4, offset + 6);
        int a = hex.length() - offset != 8 ? 255 : parseHex(hex, offset + 6, offset + 8);
        return new Color(r, g, b, a);
    }

    private static int parseHex(String string, int from, int to){
        int total = 0;
        for(int i = from; i < to; i++){
            char c = string.charAt(i);
            total += Character.digit(c, 16) * (i == from ? 16 : 1);
        }
        return total;
    }


    public static boolean isColorType(@Nullable PsiType type){
        return PsiUtils.isType(MetaData.Color.PATH,type);
    }
    public static boolean isColorType(@Nullable UExpression type){
        return PsiUtils.isType(MetaData.Color.PATH,type);
    }

}
