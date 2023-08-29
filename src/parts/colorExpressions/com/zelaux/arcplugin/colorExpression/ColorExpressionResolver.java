package com.zelaux.arcplugin.colorExpression;

import arc.util.pooling.Pool;
import arc.util.pooling.Pools;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiField;
import com.zelaux.arcplugin.MetaData;
import com.zelaux.arcplugin.expressions.resolve.ArcColorExpression;
import com.zelaux.arcplugin.expressions.resolve.ArcColorExpressionSequence;
import com.zelaux.arcplugin.expressions.resolve.Expression;
import com.zelaux.arcplugin.expressions.resolve.methods.StaticSetColorExpression;
import com.zelaux.arcplugin.utils.*;
import com.zelaux.arcplugin.utils.resolve.StaticFieldResolver;
import kotlin.collections.CollectionsKt;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.uast.*;

import java.awt.*;
import java.util.Objects;

public class ColorExpressionResolver {
    private static final Pool<arc.graphics.Color> colorPool = ColorUtils.colorPool;

    @Nullable
    public static Color resolveColor(@Nullable UExpression expression) {
        if (expression == null) return null;
        ArcColorExpressionSequence sequence = resolveColorSequence(expression);
        if (sequence == null) return null;
        return sequence.calculateResultAwtColor();
    }

    @Nullable
    public static ArcColorExpressionSequence resolveColorSequence(@NotNull UExpression expression) {
        PsiElement sourcePsi = expression.getSourcePsi();
        assert sourcePsi != null;
        if (expression instanceof UCallExpression) {
            return resolve((UCallExpression) expression);
        } else {
            UCallExpression element = UastContextKt.toUElement(expression.getSourcePsi(), UCallExpression.class);
            if (element != null) return resolveColorSequence(element);
        }
        UField resolved = CustomUastTreeUtil.resolveRecursiveField(expression);
        if (resolved == null) return null;
        UExpression initializer = UastUtils.findContaining(StaticFieldResolver.resolveStaticInitializer((PsiField) resolved.getSourcePsi()),UExpression.class);
        if (initializer == null) return null;
        return resolveColorSequence(initializer);
    }

    public static ArcColorExpressionSequence resolve(UExpression method) {
        if (method instanceof UCallExpression) {
            return resolve((UCallExpression) method);
        } else {
            return null;
        }
    }

    public static ArcColorExpressionSequence resolve(UCallExpression method) {

        ArcColorExpressionSequence sequence = new ArcColorExpressionSequence();
        UCallExpression lastMethod = null;
        while (method != null) {
            ColorExpressionParserConstructor resolved = resolveLocalMethod(method);

            if (resolved != null) {
                sequence.add(0, resolved.invoke(method));
                UExpression receiver = method.getReceiver();
                lastMethod = method;
                method = null;
                if (receiver instanceof UQualifiedReferenceExpression) {
                    UExpression selector = ((UQualifiedReferenceExpression) receiver).getSelector();
                    if (selector instanceof UCallExpression) {
                        method = (UCallExpression) selector;
                    }
                }
            } else {
                break;
            }
        }
        if (sequence.isEmpty()) return null;
        int entryPoint = CollectionsKt.indexOfFirst(sequence.list(), Expression::isEntryPoint);
        arc.graphics.Color color = colorPool.obtain();
        try {
            if (entryPoint == -1) {
                ArcColorExpression expression = resolveLocal(lastMethod.getReceiver());
                if (expression == null || !expression.isEntryPoint()) return null;
                if (!expression.apply(color)) return null;
                sequence.add(0, expression);
            } else {
                if (!sequence.list().get(entryPoint).apply(color)) return null;
            }
        } finally {
            colorPool.free(color);
        }
        return sequence;
    }

    @Nullable
    private static ArcColorExpression resolveLocal(UExpression expression) {
        if (expression instanceof UCallExpression) {
            UCallExpression method = (UCallExpression) expression;
            ColorExpressionParserConstructor constructor = resolveLocalMethod(method);
            return constructor != null ? constructor.invoke(method) : null;
        }
        UField foundField = CustomUastTreeUtil.resolveRecursiveField(expression);
        if (foundField != null) {
            return new StaticSetColorExpression(expression, UExpression.class, "<receiver>");
        }
        return null;
    }

    private static ColorExpressionParserConstructor resolveLocalMethod(UCallExpression method) {
        UMethod containing = UastUtils.findContaining(method.resolve(), UMethod.class);
        if (containing == null) return null;
        UClass clazz = CustomUastTreeUtil.getContainingClass(containing);
        if (clazz == null) {
            return null;
        }
        String name = method.getMethodName();
        if (name == null) name = "";
        return ColorExpressionParserMap.INSTANCE.get(name, method);
    }
}
