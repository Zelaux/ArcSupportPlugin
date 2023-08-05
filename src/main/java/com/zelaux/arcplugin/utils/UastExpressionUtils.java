package com.zelaux.arcplugin.utils;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.zelaux.arcplugin.langinfo.LanguageInfo;
import com.zelaux.arcplugin.langinfo.MyElementFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.uast.UExpression;
import org.jetbrains.uast.ULiteralExpression;

import java.util.Objects;

public class UastExpressionUtils {
    @Nullable
    public static Float getFloat(@NotNull UExpression expr) {
        Object object = getObject(expr);
        if (object instanceof Number) return ((Number) object).floatValue();
        return null;
    }

    @Nullable
    public static Integer getInt(@NotNull UExpression expr) {
        Object object = getObject(expr);
        if (object instanceof Integer) return ((Integer) object);
        return null;
    }

    @Nullable
    public static Object getObject(UExpression expr) {
        return expr.evaluate();
    }

    @Nullable
    public static String getString(UExpression expr) {
        Object object = getObject(expr);
        if (object instanceof String) return (String) object;

        if (expr instanceof ULiteralExpression) {
            ULiteralExpression literalExpression = (ULiteralExpression) expr;
            Object value = literalExpression.getValue();
            if (value instanceof String) return (String) value;
        }
        return null;
    }

    public static void replaceInt(UExpression expr, int newValue) {
        replaceInt(expr, newValue, false);
    }

    public static void replaceInt(UExpression expr, int newValue, boolean hex) {
        replaceInt(expr, newValue, hex, true);
    }

    public static int replaceInt(UExpression expr, int newValue, boolean hex, boolean alpha) {
        Integer currentInt = getInt(expr);
        if (Objects.equals(currentInt, newValue)) return newValue;
        String text;
        if (hex) {
            StringBuilder builder = new StringBuilder(Integer.toHexString(newValue));
            int len = alpha ? 8 : 6;
            while (builder.length() < len) {
                builder.insert(0, "0");
            }
            text = "0x" + StringUtil.toUpperCase(builder.toString());
        } else {
            text = String.valueOf(newValue);
        }
        PsiElement sourcePsi = expr.getSourcePsi();
        if (!sourcePsi.isValid()) return 0;

        return replaceExpr(sourcePsi, text);
    }

    private static int replaceExpr(PsiElement sourcePsi, String text) {

        MyElementFactory factory = LanguageInfo.elementFactoryFor(sourcePsi.getLanguage(), sourcePsi.getProject());
        if (factory == null) return 0;
        PsiElement expression = factory
                .createExpressionFromText(text, sourcePsi);
        sourcePsi.replace(expression);
        return 0;
    }

    public static int replaceFloat(UExpression expr, float newValue) {
        PsiElement sourcePsi = expr.getSourcePsi();
        if (!sourcePsi.isValid()) return 0;
        String replacementText = newValue + "f";
        return replaceExpr(sourcePsi, replacementText);
    }

    public static int replaceString(UExpression expr, String newValue) {
        PsiElement sourcePsi = expr.getSourcePsi();
        if (!sourcePsi.isValid()) return 0;
        return replaceExpr(sourcePsi, '"' + newValue + '"');
    }
}
