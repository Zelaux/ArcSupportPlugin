package com.zelaux.arcplugin.marker.color;


import com.intellij.java.*;
import com.intellij.openapi.command.*;
import com.intellij.openapi.editor.*;
import com.intellij.psi.*;
import com.intellij.psi.impl.*;
import com.zelaux.arcplugin.*;
import com.zelaux.arcplugin.colorViewer.*;
import com.zelaux.arcplugin.marker.result.*;
import com.zelaux.arcplugin.psi.PrimitiveType;
import com.zelaux.arcplugin.utils.*;
import kotlin.jvm.functions.*;
import org.jetbrains.annotations.*;
import org.jetbrains.uast.*;
import org.jetbrains.uast.evaluation.*;
import org.jetbrains.uast.values.*;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.*;

import static com.zelaux.arcplugin.ColorOp.isColorType;
import static com.zelaux.arcplugin.utils.PsiExpressionUtilsKt.*;

@SuppressWarnings("UseJBColor")
public class ArcColorProvider implements ElementColorViewerProvider{


    @Override
    public @Nullable CustomColorViewer getColorViewerFrom(@NotNull PsiElement element){
        if(element.getFirstChild() != null) return null;
        if(element instanceof PsiWhiteSpace) return null;
        PsiElement parent = element.getParent();
//        LanguageUtil.)
        UCallExpression newExpression = org.jetbrains.uast.UastUtils.findContaining(parent, UCallExpression.class);
        ColorResult color = getJavaColorFromExpression(newExpression);
        if(color.isNull()){
            parent = parent == null ? null : parent.getParent();
            newExpression = org.jetbrains.uast.UastUtils.findContaining(parent, UCallExpression.class);
            color = getJavaColorFromExpression(newExpression);
        }

        if(newExpression != null && color.isNotNull()){
            UReferenceExpression uRef = newExpression.getClassReference();
            String resolvedName = uRef == null ? null : uRef.getResolvedName();
            if(resolvedName != null && element.textMatches(resolvedName) || UExpressionUtils.isStaticMethod(newExpression, MetaData.Color.PATH, element.getText())){
                return color.toColorViewer(this::setColorTo);
            }
        }
/*
        if(isIntLiteralInsideNewJBColorExpression(parent)){
            return color;
        }*/
        return null;
    }

    @NotNull
    public ColorResult getJavaColorFromExpression(@Nullable UCallExpression expression){
        if(expression == null) return NullColorResult.INSTANCE;
        if(expression.getKind() == UastCallKind.CONSTRUCTOR_CALL && isColorType(expression.getReturnType())){
            return getColorInner(expression);
        }
        String[] availableMethods = {MetaData.Color.valueOf, MetaData.Color.grays, MetaData.Color.HSVtoRGB, MetaData.Color.NONSTATIC.set};
        for(String availableMethod : availableMethods){
            if(UExpressionUtils.isStaticMethod(expression, MetaData.Color.PATH, availableMethod)){
                return getColorInner(expression);
            }
        }
      /*  if(ArcColorUtils.isDrawColor(expression)){
//            return getColor(expression);
        }*/
        return NullColorResult.INSTANCE;
    }

    /*TODO
    set
set(float,float,float)
set(float,float,float,float)
set(int)
rgba8888(int)
abgr8888(float)
rgb565(int)
rgba4444(int)
rgb888(int)
argb8888(int)
fromHue(float,float,float)
fromHue(float[])

rgb(int,int,int)
HSVtoRGB(float,float,float)
HSVtoRGB(float,float,float,arc.graphics.Color)
HSVtoRGB(float,float,float,float)
grays(float)
valueOf(Color,String)
valueOf(String)
    *
    * */

    @Override
    public @Nullable Color getColor(UCallExpression expression){
        return getColorInner(expression).getColor();
    }

    @NotNull
    public ColorResult getColorInner(UCallExpression newExpression){
        List<UExpression> args = newExpression.getValueArguments();
        List<PsiType> typeArgs = args.stream().map(UExpression::getExpressionType).collect(Collectors.toList());
        try{
            ColorConstructors type = args.isEmpty() ? null : getConstructorType(newExpression.getMethodName(), args.size(), typeArgs);
            if(type != null){
                switch(type){
                   /* case setColor:{
                        Color color = ArcColorUtils.resolveColor(args.get(0));
                        if(color == null) return NullColorResult.INSTANCE;
                        return RGBAColorResult.notWritable(color);
//                        break;
                    }*/
                    case constructorInt:{
//                        args.get(0).toString()
                        UExpression argExpression = args.get(0);
//                        String text = argExpression.getSourcePsi().getText();
//                        System.out.println("text: " + text);
                        int rgba = getInt(argExpression);

                        float r = ((rgba & 0xff000000) >>> 24) / 255f;
                        float g = ((rgba & 0x00ff0000) >>> 16) / 255f;
                        float b = ((rgba & 0x0000ff00) >>> 8) / 255f;
                        float a = ((rgba & 0x000000ff)) / 255f;
                        return RGBAColorResult.writable(new Color(r, g, b, a), true);
                    }
                    case constructorFloat3:
                    case constructorFloat4:
                        return RGBAColorResult.writable(
                        new Color(getClampFloat(args.get(0)), getClampFloat(args.get(1)), getClampFloat(args.get(2)),
                        type == ColorConstructors.constructorFloat4 ? getClampFloat(args.get(3)) : 1f
                        ), type == ColorConstructors.constructorFloat4);
                    case valueOf_Color_Str:
                    case valueOf_Str:
                        try{
                            return RGBAColorResult.writable(ColorOp.valueOf(String.valueOf(getObject(args.get(type == ColorConstructors.valueOf_Str ? 0 : 1)))), true);
                        }catch(Exception e){
                            e.printStackTrace();
//                            throw new RuntimeException(e);
                        }
                        break;
                    case grays:
                        return new GrayColorResult(getClampFloat(args.get(0)), 1f, true, true);
                    case hsvToRgb_flt3:
                    case hsvToRgb_flt4:
                    case hsvToRgb_flt3_Color:{
                        @SuppressWarnings("PointlessArithmeticExpression")
                        Color color = Color.getHSBColor(
                        clamp(getFloat(args.get(0)) / 360f) * 1f,
                        clamp(getFloat(args.get(1)) / 100f) * 1f,
                        clamp(getFloat(args.get(2)) / 100f) * 1f
                        );
                        if(type == ColorConstructors.hsvToRgb_flt4){
                            color = new Color(color.getRed(), color.getGreen(), color.getBlue(), (int)(getClampFloat(args.get(args.size() - 1)) * 255f));
//                            return new Color(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, clamp(getFloat(args.get(args.size() - 1))));
                        }
                        return RGBAColorResult.writable(color, type == ColorConstructors.hsvToRgb_flt4);
                    }
                }
            }
        }catch(Exception ignore){
            ignore.printStackTrace();
        }
        return NullColorResult.INSTANCE;
    }

    private static float getClampFloat(UExpression expression){
        return clamp(getFloat(expression));
    }

    private static float clamp(float aFloat){
        return Math.max(0, Math.min(1, aFloat));
    }

    private static ColorConstructors getConstructorType(@Nullable String methodName, int paramCount, List<PsiType> paramType){
        if(paramType == null) return null;
        switch(paramCount){
            case 1:
                if(MetaData.Color.grays.equals(methodName)) return ColorConstructors.grays;
                String canonicalText = paramType.get(0).getCanonicalText();
//                System.out.println(canonicalText);
                if("set".equals(methodName) && canonicalText.equals(MetaData.Color.PATH)) return ColorConstructors.setColor;
                if(canonicalText.equals("java.lang.String")) return ColorConstructors.valueOf_Str;
                if (PrimitiveType.INT.isEqual(paramType.get(0))) return ColorConstructors.constructorInt;
            return null;
            case 2:
                return ColorConstructors.valueOf_Color_Str;
            case 3:
                if(MetaData.Color.HSVtoRGB.equals(methodName)) return ColorConstructors.hsvToRgb_flt3;
                return ColorConstructors.constructorFloat3;
            case 4:
                if(MetaData.Color.HSVtoRGB.equals(methodName))

                    return PrimitiveType.FLOAT.isEqual(paramType.get(paramType.size() - 1)) ? ColorConstructors.hsvToRgb_flt4 : ColorConstructors.hsvToRgb_flt3_Color;
                return ColorConstructors.constructorFloat4;
        }

        return null;
    }

    public static int getInt(UExpression expr){
        Object object = getObject(expr);
        if(object == null) throw new NullPointerException();
        return ((Integer)object);
    }

    public static float getFloat(UExpression expr){
        Object object = getObject(expr);
        if(object == null) throw new NullPointerException();
        return ((Number)object).floatValue();
    }


    public static int getInt(PsiExpression expr){
        Object object = getObject(expr);
        if(object == null) throw new NullPointerException();
        return ((Integer)object);
    }

    public static float getFloat(PsiExpression expr){
        return ((Number)getObject(expr)).floatValue();
    }

    private static Object getObject(PsiExpression expr){
        return JavaConstantExpressionEvaluator.computeConstantExpression(expr, true);
    }

    private static Object getObject(UExpression expr){
        UValue value = UEvaluationContextKt.uValueOf(expr);
        if(value == null){
            return null;
        }
        UConstant constant = value.toConstant();
        if(constant == null){
            return null;
        }
        return constant.getValue();
    }

    public void setColorTo(@NotNull PsiElement element, @NotNull Color color){

        final Document document = PsiDocumentManager.getInstance(element.getProject()).getDocument(element.getContainingFile());

        @SuppressWarnings("unchecked")
        PsiCall psiCall = CustomPsiTreeUtil.getParentOfType(element, false, PsiNewExpression.class, PsiMethodCallExpression.class);
//        if(psiCall == null) psiCall = PsiTreeUtil.getParentOfType(element, PsiMethodCallExpression.class, false);
//        if(psiCall == null) psiCall = PsiTreeUtil.getParentOfType(element, PsiCall.class, false);
//        if(psiCall == null) psiCall = PsiTreeUtil.getParentOfType(element, PsiCallExpression.class, false);
        if(psiCall == null) return;

        PsiExpressionList argumentList = psiCall.getArgumentList();
        assert argumentList != null;

        PsiExpression[] expr = argumentList.getExpressions();
        PsiType[] expressionTypes = argumentList.getExpressionTypes();
        ColorConstructors type = expressionTypes.length == 0 ? null : getConstructorType(psiCall.resolveMethod().getName(), expressionTypes.length, List.of(expressionTypes));

        assert type != null;
        Runnable command = () -> {
            switch(type){
                case setColor:
                    break;
                case grays:
                    replaceFloat(expr[0], color.getRed() / 255f);
                    return;
                case constructorInt:
                    if(color.getAlpha() == 255 && false){
                        replaceInt(expr[0], color.getRGB(), true);
                    }else{
//                            PsiElementFactory factory = JavaPsiFacade.getElementFactory(argumentList.getProject());
//                            argumentList.add(factory.createExpressionFromText("true", null));

                        int rgba = (color.getRed() << 24) | (color.getGreen() << 16) | (color.getBlue() << 8) | color.getAlpha();
                        replaceInt(expr[0], rgba, true, true);
                    }
                    return;
                case constructorFloat3:
                case constructorFloat4:
                    float[] rgba = color.getColorComponents(null);
                    replaceFloat(expr[0], rgba[0]);
                    replaceFloat(expr[1], rgba[1]);
                    replaceFloat(expr[2], rgba[2]);
                    if(type == ColorConstructors.constructorFloat4){
                        replaceFloat(expr[3], rgba.length == 4 ? rgba[3] : color.getAlpha() / 255f);
                    }else if(color.getAlpha() != 255){
                        PsiElementFactory factory = JavaPsiFacade.getElementFactory(argumentList.getProject());
                        String text = String.valueOf(color.getAlpha() / 255f);
                        argumentList.add(factory.createExpressionFromText(text + "f", null));
                    }
                    return;
                case valueOf_Str:
                case valueOf_Color_Str:
                    replaceString(expr[type == ColorConstructors.valueOf_Str ? 0 : 1], ColorOp.toString(color));
                    return;
                case hsvToRgb_flt3:
                case hsvToRgb_flt4:
                case hsvToRgb_flt3_Color:
                    float[] hsv = ColorOp.toHsv(color, new float[3]);

                    replaceFloat(expr[0], hsv[0]);
                    replaceFloat(expr[1], hsv[1] * 100f);
                    replaceFloat(expr[2], hsv[2] * 100f);

                    if(type == ColorConstructors.hsvToRgb_flt4){
                        replaceFloat(expr[3], color.getAlpha() / 255f);
                    }
                    return;
//                    color.getColorComponents(ColorSpace.getInstance())
            }
        };

        CommandProcessor.getInstance()
        .executeCommand(element.getProject(), command, JavaBundle.message("change.color.command.text"), null, document);
    }


    private class ColorProv{
        ArrayList<ColorProv> provs = new ArrayList<>();

        public final String classRef;
        public final String methodName;
        public final boolean isStatic;
        public Function1<@NotNull PsiElement, @Nullable Color> colorGetter;

        public ColorProv(String classRef, String methodName, boolean isStatic){
            this.classRef = classRef;
            this.methodName = methodName;
            this.isStatic = isStatic;
            provs.add(this);
        }
    }

    private enum ColorConstructors{
        constructorInt, constructorFloat3, constructorFloat4,
        valueOf_Str, valueOf_Color_Str,
        grays,
        hsvToRgb_flt3,
        hsvToRgb_flt4,
        hsvToRgb_flt3_Color, setColor,

    }
}