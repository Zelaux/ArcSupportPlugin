package com.zelaux.arcplugin.interp;

import arc.math.Interp;
import com.zelaux.arcplugin.interp.expressions.resolve.InterpExpression;
import com.zelaux.arcplugin.interp.expressions.resolve.InterpFieldExpression;
import com.zelaux.arcplugin.utils.CustomUastTreeUtil;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.uast.*;

import java.util.Objects;

public class InterpResolver {
    @Nullable
    public static InterpExpression resolve(UExpression expression){
        if (expression instanceof UCallExpression) {
//            return null
        }
        UElement element = CustomUastTreeUtil.resolveElement(expression);
        if(element instanceof UField){
            UClass containingClass = CustomUastTreeUtil.getContainingClass((UField) element);

            if(isInterpClass(containingClass)) return new InterpFieldExpression(expression);
        }
        return null;
    }
    public static boolean isInterpClass(UClass containingClass) {
        if (containingClass == null) return false;
        return Objects.equals(containingClass.getQualifiedName(), Interp.class.getCanonicalName());
    }

}
