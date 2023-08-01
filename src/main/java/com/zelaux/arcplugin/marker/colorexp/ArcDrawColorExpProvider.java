package com.zelaux.arcplugin.marker.colorexp;

import com.intellij.java.*;
import com.intellij.openapi.command.*;
import com.intellij.openapi.editor.*;
import com.intellij.psi.*;
import com.zelaux.arcplugin.*;
import com.zelaux.arcplugin.MetaData.*;
import com.zelaux.arcplugin.awt.*;
import com.zelaux.arcplugin.marker.*;
import com.zelaux.arcplugin.parsers.colorexpr.*;
import com.zelaux.arcplugin.parsers.colorexpr.ComponentSetter.Companion.*;
import com.zelaux.arcplugin.utils.*;
import org.jetbrains.annotations.*;
import org.jetbrains.uast.*;

import java.awt.Color;
import java.util.*;
import java.util.List;
import java.util.stream.*;

import static com.zelaux.arcplugin.utils.PsiExpressionUtilsKt.*;

public class ArcDrawColorExpProvider implements ExpParserSeqProvider{

    private static ColorExpParserSequence getColorExpressionSequence(@Nullable UCallExpression expression, @NotNull PsiCallExpression element){
        if(expression == null) return null;
        String[] availableMethods = {MetaData.Draw.color, MetaData.Draw.colorl, MetaData.Draw.mixcol};
        for(String availableMethod : availableMethods){
            if(UExpressionUtils.isStaticMethod(expression, MetaData.Draw.PATH, availableMethod)){
                return getColorInternal(expression, element);
            }
        }
        return null;
    }

    private static ColorExpParserSequence getColorInternal(UCallExpression newExpression, @NotNull PsiCallExpression callExpression){
        java.util.List<UExpression> args = newExpression.getValueArguments();
        java.util.List<PsiType> typeArgs = args.stream().map(UExpression::getExpressionType).collect(Collectors.toList());
        try{
            DrawFunctionType type = drawFunctionType(newExpression.getMethodName(), args.size(), typeArgs);
            if(type != null){
                switch(type){
                    case color_EMPTY:
                        return new StaticColorParser(arc.graphics.Color.white, callExpression).wrap("color()").asSequence();
                    case color_Color_float:{

                        ColorExpParserSequence sequence = new ColorExpParserSequence();
                        sequence.getList().add(new StaticSetColorParser(callExpression).wrap("color(Color,_)"));
                        sequence.getList().add(new ComponentSetter(callExpression, ComponentType.a).offset(1).wrap("color(_,float)"));
                        return sequence;
                    }
                    case color_Color_Color_float:
//                        Color colorA = ArcColorUtils.resolveColor(args.get(0));
//                        if(colorA == null) return NullColorResult.INSTANCE;
//                        Color colorB = ArcColorUtils.resolveColor(args.get(1));
//                        if(colorB == null) return NullColorResult.INSTANCE;
                        return new GradientColorParser("color(Color,Color,float)", callExpression).offset(1).asSequence();
//                        return GradientColorResult.writable(colorA, colorB, getFloat(args.get(2)), false);
                    case color_Color:
                        return new StaticSetColorParser(callExpression).wrap("color(Color)").asSequence();
                    case colorl:{

                        ColorExpParserSequence sequence = new ColorExpParserSequence();
                        sequence.getList().add(new GraysColor(callExpression).wrap("colorl(float)"));
                        return sequence;
                    }
                    case colorlAlpha:{
                        ColorExpParserSequence sequence = new ColorExpParserSequence();
                        sequence.getList().add(new GraysColor(callExpression).wrap("colorl(float,_)"));
                        sequence.getList().add(new ComponentSetter(callExpression, ComponentType.a).offset(1).wrap("colorl(_,float)"));
                        return sequence;
                    }
                    case colorInt:
                        return new SetIntColor(callExpression,true).wrap("color(int)").asSequence();
                    case colorFloat3:
                        return new SetFloat3Color(callExpression).wrap("color(float,float,float)").asSequence();
                    case colorFloat4:
                        return new SetFloat4Color(callExpression).wrap("color(float,float,float,float)").asSequence();
                }
            }
        }catch(Exception ignore){
//            ignore.printStackTrace();
        }
        return null;
    }


    private static DrawFunctionType drawFunctionType(String methodName, int paramCount, java.util.List<PsiType> paramType){
        if(paramType == null) return null;
        boolean isColorMethod = Draw.color.equals(methodName);
        boolean isColorlMethod = Draw.colorl.equals(methodName);
        boolean isMixcolMethod = Draw.mixcol.equals(methodName);
        switch(paramCount){
            case 0:
                if(isColorMethod) return DrawFunctionType.color_EMPTY;
                return null;
            case 1:
                if(isColorlMethod) return DrawFunctionType.colorl;
                if(Objects.equals(paramType.get(0), PsiType.INT)) return DrawFunctionType.colorInt;
                return DrawFunctionType.color_Color;
            case 2:{
                if(isColorlMethod) return DrawFunctionType.colorlAlpha;
                if(isColorMethod || isMixcolMethod) return DrawFunctionType.color_Color_float;
                break;
            }
            case 3:{
                if(isColorMethod || isMixcolMethod){
                    if(paramType.get(0).getCanonicalText().equals(MetaData.Color.PATH) &&
                    paramType.get(1).getCanonicalText().equals(MetaData.Color.PATH)
                    ) return DrawFunctionType.color_Color_Color_float;
                    if(isColorMethod) return DrawFunctionType.colorFloat3;
                }
                break;
            }
            case 4:
                if(isColorMethod) return DrawFunctionType.colorFloat4;
                break;
        }

        return null;
    }

    @Override
    public @Nullable ColorExpParserSequence expressionParserSequenceFrom(@NotNull PsiElement element){
        if(!(element instanceof PsiIdentifier)) return null;
        PsiIdentifier identifier = (PsiIdentifier)element;
        PsiElement parent__ = identifier.getParent();
        if(!(parent__ instanceof PsiReference || parent__ instanceof PsiReferenceExpression)) return null;
        PsiElement referenceExpression = identifier.getParent();
        if(!(referenceExpression.getParent() instanceof PsiCallExpression)) return null;

        PsiCallExpression callExpression = (PsiCallExpression)referenceExpression.getParent();
//        LanguageUtil.)
        UCallExpression newExpression = UastUtils.findContaining(callExpression, UCallExpression.class);
        if(!UExpressionUtils.isStaticMethod(newExpression, MetaData.Draw.PATH, element.getText())) return null;

        ColorExpParserSequence color = getColorExpressionSequence(newExpression, callExpression);

        if(newExpression != null){
//            UReferenceExpression uRef = newExpression.getClassReference();
//            String resolvedName = uRef == null ? null : uRef.getResolvedName();

            return color;

        }
        return null;
    }

    public void setColorTo(@NotNull PsiElement element, @NotNull Color color){

        final Document document = PsiDocumentManager.getInstance(element.getProject()).getDocument(element.getContainingFile());


//        PsiCall psiCall = PsiTreeUtil.getParentOfType(element, PsiNewExpression.class,false);
        @SuppressWarnings("unchecked")
        PsiCall psiCall = CustomPsiTreeUtil.getParentOfType(element, false, PsiNewExpression.class, PsiMethodCallExpression.class);
//        if(psiCall == null) psiCall = PsiTreeUtil.getParentOfType(element, PsiMethodCallExpression.class,false);
//        if(psiCall == null) psiCall = PsiTreeUtil.getParentOfType(element, PsiCall.class);
//        if(psiCall == null) psiCall = PsiTreeUtil.getParentOfType(element, PsiCallExpression.class);
        if(psiCall == null) return;

        PsiExpressionList argumentList = psiCall.getArgumentList();
        assert argumentList != null;

        PsiExpression[] expr = argumentList.getExpressions();
        PsiType[] expressionTypes = argumentList.getExpressionTypes();
        DrawFunctionType type = drawFunctionType(psiCall.resolveMethod().getName(), expressionTypes.length, List.of(expressionTypes));

        assert type != null;
        Runnable command = () -> {
            switch(type){
                case color_Color_float:{
                    replaceFloat(expr[1], color.getAlpha() / 255f);
                    return;
                }
                case color_Color_Color_float:
                    if(!(color instanceof GradientColor)) return;
                    float progress = ((GradientColor)color).progress;
                    replaceFloat(expr[2], progress);
                    return;
                case colorl:
                case colorlAlpha:
                    replaceFloat(expr[0], color.getRed() / 255f);
                    if(type == DrawFunctionType.colorlAlpha){
                        replaceFloat(expr[1], color.getAlpha() / 255f);
                    }
                    return;
                case colorInt:
                    if(true){
                        int rgba = (color.getRed() << 0x18) | (color.getGreen() << 0x10) | (color.getBlue() << 0x8) | color.getAlpha();
                        replaceInt(expr[0], rgba, true);
                    }
                    return;
                case colorFloat3:
                case colorFloat4:
                    float[] rgba = color.getColorComponents(null);
                    replaceFloat(expr[0], rgba[0]);
                    replaceFloat(expr[1], rgba[1]);
                    replaceFloat(expr[2], rgba[2]);
                    if(type == DrawFunctionType.colorFloat4){
                        replaceFloat(expr[3], rgba.length == 4 ? rgba[3] : color.getAlpha() / 255f);
                    }else if(color.getAlpha() != 255){
                        PsiElementFactory factory = JavaPsiFacade.getElementFactory(argumentList.getProject());
                        String text = String.valueOf(color.getAlpha() / 255f);
                        argumentList.add(factory.createExpressionFromText(text + "f", null));
                    }
                    return;
            }
        };

        CommandProcessor.getInstance()
        .executeCommand(element.getProject(), command, JavaBundle.message("change.color.command.text"), null, document);
    }


    private enum DrawFunctionType{
        color_EMPTY,
        colorInt, colorFloat3, colorFloat4, color_Color, colorl, colorlAlpha, color_Color_Color_float, color_Color_float,
    }

}