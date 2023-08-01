package com.zelaux.arcplugin.marker;

import com.intellij.psi.*;
import com.zelaux.arcplugin.*;
import com.zelaux.arcplugin.colorViewer.*;
import com.zelaux.arcplugin.marker.color.*;
import com.zelaux.arcplugin.marker.result.*;
import com.zelaux.arcplugin.utils.*;
import org.jetbrains.annotations.*;
import org.jetbrains.uast.*;

import java.awt.*;

public class ArcLongExpressionColorProvider implements ElementColorViewerProvider{
    @Override
    public @Nullable CustomColorViewer getColorViewerFrom(@NotNull PsiElement element){

        if(element.getFirstChild() != null) return null;
        if(element instanceof PsiWhiteSpace) return null;
        PsiElement parent = element.getParent();
        UCallExpression expression = org.jetbrains.uast.UastUtils.findContaining(parent, UCallExpression.class);
        ColorResult color = getColorFromExpression(expression);

        if(expression != null && color.isNotNull()){
            UReferenceExpression uRef = expression.getClassReference();
            String resolvedName = uRef == null ? null : uRef.getResolvedName();
            if(resolvedName != null && element.textMatches(resolvedName) || UExpressionUtils.isStaticMethod(expression, MetaData.Color.PATH, element.getText())){
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
    private ColorResult getColorFromExpression(UCallExpression expression){
        ColorResult result = getNullColorFromExpression(expression);
        return result == null ? NullColorResult.INSTANCE : result;
    }

    @Nullable
    private ColorResult getNullColorFromExpression(UCallExpression expression){
        if(!UExpressionUtils.isStaticMethod(expression, MetaData.Color.PATH, "set") && !UExpressionUtils.isStaticMethod(expression, MetaData.Color.PATH, "cpy")){
            return null;
        }

//        expression.getTypeArgumentCount()
//        expression
        throw null;
    }

    private void setColorTo(PsiElement psiElement, Color color){

    }

    @Override
    public @Nullable Color getColor(UCallExpression expression){
        return null;
    }
}
