/*
package com.zelaux.arcplugin.marker.color;


import com.intellij.codeInsight.*;
import com.intellij.ide.util.*;
import com.intellij.java.*;
import com.intellij.navigation.*;
import com.intellij.openapi.command.*;
import com.intellij.openapi.editor.*;
import com.intellij.openapi.util.*;
import com.intellij.pom.*;
import com.intellij.psi.*;
import com.intellij.psi.impl.compiled.*;
import com.intellij.psi.util.*;
import com.zelaux.arcplugin.*;
import com.zelaux.arcplugin.MetaData.*;
import com.zelaux.arcplugin.awt.*;
import com.zelaux.arcplugin.colorViewer.*;
import com.zelaux.arcplugin.marker.result.*;
import com.zelaux.arcplugin.utils.*;
import org.jetbrains.annotations.*;
import org.jetbrains.uast.*;

import java.awt.Color;
import java.util.*;
import java.util.List;
import java.util.stream.*;

import static com.zelaux.arcplugin.utils.PsiExpressionUtilsKt.*;

@SuppressWarnings("UseJBColor")
public class ArcDrawColorProvider implements ElementColorViewerProvider{
    public static @NotNull Key<CachedValue<PsiField>> psiFieldWithInitializerKey = new Key<>("com.zelaux.arcplugin.psifield.initializer");

    @Override
    public CustomColorViewer getColorViewerFrom(@NotNull PsiElement element){
        if(element.getFirstChild() != null) return null;
        if(element instanceof PsiWhiteSpace) return null;
        if(!(element instanceof PsiIdentifier)) return null;
        PsiElement parent = element;
//        LanguageUtil.)
        UCallExpression newExpression = UastUtils.findContaining(parent, UCallExpression.class);
        if(!UExpressionUtils.isStaticMethod(newExpression, MetaData.Draw.PATH, element.getText())) return null;

        ColorResult color = getJavaColorFromExpression(newExpression);

        if(newExpression != null && color.isNotNull()){
//            UReferenceExpression uRef = newExpression.getClassReference();
//            String resolvedName = uRef == null ? null : uRef.getResolvedName();

            return color.toColorViewer(this::setColorTo);

        }
        return null;
    }


    private static ColorResult getJavaColorFromExpression(@Nullable UCallExpression initializer){
        if(initializer == null) return NullColorResult.INSTANCE;
        String[] availableMethods = {MetaData.Draw.color, MetaData.Draw.colorl,MetaData.Draw.mixcol};
        for(String availableMethod : availableMethods){
            if(UExpressionUtils.isStaticMethod(initializer, MetaData.Draw.PATH, availableMethod)){
                return getColorInternal(initializer);
            }
        }
        return NullColorResult.INSTANCE;
    }


    @Override
    public @Nullable Color getColor(UCallExpression initializer){
        return getColorInternal(initializer).getColor();
    }

    private static ColorResult getColorInternal(UCallExpression newExpression){
        List<UExpression> args = newExpression.getValueArguments();
        List<PsiType> typeArgs = args.stream().map(UExpression::getExpressionType).collect(Collectors.toList());
        try{
            DrawFunctionType type =  drawFunctionType(newExpression.getMethodName(), args.size(), typeArgs);
            if(type != null){
                switch(type){
                    case color_EMPTY:
                        return RGBAColorResult.notWritable(Color.white);
                    case color_Color_float:{
                        Color color = ArcColorUtils.resolveColor(args.get(0));
                        if(color == null) return NullColorResult.INSTANCE;
                        Color colorA = new Color(color.getRed(), color.getGreen(), color.getBlue(), 0);
                        Color colorB = new Color(color.getRed(), color.getGreen(), color.getBlue(), 255);

                        return GradientColorResult.writable(colorA, colorB, getFloat(args.get(1)), false);
                    }
                    case color_Color_Color_float:
                        Color colorA = ArcColorUtils.resolveColor(args.get(0));
                        if(colorA == null) return NullColorResult.INSTANCE;
                        Color colorB = ArcColorUtils.resolveColor(args.get(1));
                        if(colorB == null) return NullColorResult.INSTANCE;

                        return GradientColorResult.writable(colorA, colorB, getFloat(args.get(2)), false);
                    case color_Color:
                        Color color = ArcColorUtils.resolveColor(args.get(0));
                        if(color != null) return RGBAColorResult.notWritable(color);
                        return NullColorResult.INSTANCE;
                    case colorl:
                    case colorlAlpha:
                        float value = getFloat(args.get(0));
                        return new GrayColorResult(value, type == DrawFunctionType.colorl ? 1f : getFloat(args.get(1)), type != DrawFunctionType.colorl, true);
                    case colorInt:
                        int rgba = getInt(args.get(0));

                        float r = ((rgba & 0xff000000) >>> 24) / 255f;
                        float g = ((rgba & 0x00ff0000) >>> 16) / 255f;
                        float b = ((rgba & 0x0000ff00) >>> 8) / 255f;
                        float a = ((rgba & 0x000000ff)) / 255f;
                        return RGBAColorResult.writable(new Color(r, g, b, a), true);
                    case colorFloat3:
                    case colorFloat4:
                        return RGBAColorResult.writable(
                        new Color(
                        clamp(getFloat(args.get(0))),
                        clamp(getFloat(args.get(1))),
                        clamp(getFloat(args.get(2))),
                        type == DrawFunctionType.colorFloat3 ? 1f : clamp(getFloat(args.get(3)))
                        ), type == DrawFunctionType.colorFloat4);
                }
            }
        }catch(Exception ignore){
//            ignore.printStackTrace();
        }
        return NullColorResult.INSTANCE;
    }


    private static Navigatable gtdTargetNavigatable(PsiElement targetElement){
        PsiElement rawTarget = TargetElementUtil.getInstance().getGotoDeclarationTarget(targetElement, targetElement.getNavigationElement());
        if(rawTarget == null){
            return EmptyNavigatable.INSTANCE;
        }
        return psiNavigatable(rawTarget);
    }

    private static Navigatable psiNavigatable(PsiElement targetElement){
        if(targetElement instanceof Navigatable){
            return (Navigatable)targetElement;
        }
        Navigatable descriptor = EditSourceUtil.getDescriptor(targetElement);
        if(descriptor != null) return descriptor;
        return EmptyNavigatable.INSTANCE;
    }

    private static @Nullable PsiElement tryResolve(PsiElement sourcePsi){
//        CommonDataKeys.EDITOR.getData(sourcePsi.getProject().getCon)
//        UtilKt.

        Document currentDocument = PsiDocumentManager.getInstance(sourcePsi.getProject()).getDocument(sourcePsi.getContainingFile());
        if(currentDocument == null) return null;
        Editor editor = EditorFactory.getInstance().getEditors(currentDocument, sourcePsi.getProject())[0];
        final PsiReference reference = TargetElementUtil.findReference(editor, sourcePsi.getTextOffset());
        if(reference == null) return null;
        PsiElement resolve = reference.resolve();
        if(resolve instanceof ClsFieldImpl){
            ClsClassImpl parent = (ClsClassImpl)resolve.getParent();
            PsiClass sourceMirrorClass = parent.getSourceMirrorClass();

            assert sourceMirrorClass != null;
            List<PsiField> collect = Arrays.stream(sourceMirrorClass.getAllFields()).filter(it -> {
//                resolve.getNa
                return it.getName().equals(((ClsFieldImpl)resolve).getName());
            }).collect(Collectors.toList());
            if(collect.size() == 1 && collect.get(0).hasInitializer()) return collect.get(0);
        }
//        String text = requeredDocument.getText(reference.getRangeInElement());
        return null;
//        return findTargets(editor, sourcePsi.getTextOffset(), sourcePsi);
//        return PsiElement.EMPTY_ARRAY;
    }

    private static float clamp(float aFloat){
        return Math.max(0, Math.min(1, aFloat));
    }

    private static DrawFunctionType drawFunctionType(String methodName, int paramCount, List<PsiType> paramType){
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
        DrawFunctionType type =  drawFunctionType(psiCall.resolveMethod().getName(), expressionTypes.length, List.of(expressionTypes));

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

    */
/*private static class ColorResult{
        @Nullable
        private Color color;
        @Nullable
        private Color colorA;
        @Nullable
        private Color colorB;
        public static final ColorResult result = new ColorResult();
        private boolean writable;

        public static ColorResult nullResult(){
            return result.setNull();
        }

        public ColorResult setGradient(Color color, boolean canChange){
            this.color = color;
            this.writable = canChange;
//            hasAlpha=true;
            return this;
        }
        public ColorResult set(Color color, boolean canChange){
            this.color = color;
            this.writable = canChange;
//            hasAlpha=true;
            return this;
        }

        //private boolean hasAlpha;
        public ColorResult setChangeable(Color color){
            return set(color, true);
        }

        public ColorResult setUnchangeable(Color color){
            return set(color, false);
        }

        public ColorResult setNull(){
            set(null, false);
            return this;
        }

        public boolean isNotNull(){
            return color != null;
        }
    }*//*

}*/
