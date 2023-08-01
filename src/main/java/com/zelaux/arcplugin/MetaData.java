package com.zelaux.arcplugin;

import arc.util.*;
import org.jetbrains.annotations.*;

import java.util.*;

public class MetaData{


    public static class Color{
        public static final String PATH = "arc.graphics.Color";
        public static final String valueOf = "valueOf";
        public static final String HSVtoRGB = "HSVtoRGB";
        public static final String grays = "grays";

        public static class NONSTATIC{
            public static final String set = "set";
        }
    }

    public static class Draw{
        public static final String PATH = "arc.graphics.g2d.Draw";
        public static final String color = "color";
        public static final String colorl = "colorl";
        public static final String mixcol = "mixcol";
    }
}
