package com.zelaux.arcplugin.codeInsight.preview;

import com.intellij.codeInsight.preview.*;
import com.intellij.openapi.util.text.*;
import com.intellij.patterns.*;
import com.intellij.psi.*;
import com.intellij.psi.util.*;
import com.intellij.util.*;
import com.intellij.xml.util.*;
import com.zelaux.arcplugin.*;
import org.jetbrains.annotations.*;

import javax.swing.*;
import java.awt.*;

public class ArcColorPreviewHintProvider implements PreviewHintProvider{

    private static final PsiMethodPattern DECODE_METHOD = PsiJavaPatterns.psiMethod()
    .definedInClass(Color.class.getName())
    .withName("decode");
    private static final PsiExpressionPattern.Capture<PsiExpression> DECODE_METHOD_CALL_PARAMETER =
    PsiJavaPatterns.psiExpression().methodCallParameter(0, DECODE_METHOD);
    private static final PsiMethodPattern GET_COLOR_METHOD = PsiJavaPatterns.psiMethod()
    .definedInClass(Color.class.getName())
    .withName("getColor");
    private static final PsiExpressionPattern.Capture<PsiExpression> GET_METHOD_CALL_PARAMETER =
    PsiJavaPatterns.psiExpression().methodCallParameter(0, GET_COLOR_METHOD);

    private static boolean isInsideDecodeOrGetColorMethod(PsiElement element) {
        if (PsiUtil.isJavaToken(element, JavaTokenType.STRING_LITERAL)) {
            element = element.getParent();
        }

        return DECODE_METHOD_CALL_PARAMETER.accepts(element) ||
        GET_METHOD_CALL_PARAMETER.accepts(element);
    }

    @Override
    public boolean isSupportedFile(PsiFile file) {
        return file instanceof PsiJavaFile;
    }

    @SuppressWarnings("UseJBColor")
    @Override
    public JComponent getPreviewComponent(@NotNull PsiElement element) {
        final PsiCall psiNewExpression = PsiTreeUtil.getParentOfType(element, PsiCallExpression.class);

        if (psiNewExpression != null) {
            final PsiJavaCodeReferenceElement referenceElement = PsiTreeUtil.getChildOfType(psiNewExpression, PsiJavaCodeReferenceElement.class);

            if (referenceElement != null) {
                final PsiReference reference = referenceElement.getReference();

                if (reference != null) {
                    final PsiElement psiElement = reference.resolve();

                    if (psiElement instanceof PsiClass && MetaData.Color.PATH.equals(((PsiClass)psiElement).getQualifiedName())) {
                        final PsiExpressionList argumentList = psiNewExpression.getArgumentList();

                        if (argumentList != null) {
                            final PsiExpression[] expressions = argumentList.getExpressions();
                            int[] values = ArrayUtil.newIntArray(expressions.length);
                            float[] values2 = new float[expressions.length];
                            int i = 0;
                            int j = 0;

                            final PsiConstantEvaluationHelper helper = JavaPsiFacade.getInstance(element.getProject()).getConstantEvaluationHelper();
                            for (final PsiExpression each : expressions) {
                                final Object o = helper.computeConstantExpression(each);
                                if (o instanceof Integer) {
                                    values[i] = ((Integer)o).intValue();
                                    if (expressions.length != 1) {
                                        values[i] = values[i] > 255 ? 255 : Math.max(values[i], 0);
                                    }

                                    i++;
                                }
                                else if (o instanceof Float) {
                                    values2[j] = ((Float)o).floatValue();
                                    values2[j] = values2[j] > 1 ? 1 : values2[j] < 0 ? 0 : values2[j];
                                    j++;
                                }
                            }


                            Color c = null;
                            if (i == expressions.length) {
                                if (i == 1 && values[0] > 255) {
                                    c = new Color(values[0]);
                                } else {
                                    switch (values.length) {
                                        case 1:
                                            c = new Color(values[0]);
                                            break;
                                        case 3:
                                            c = new Color(values[0], values[1], values[2]);
                                            break;
                                        case 4:
                                            c = new Color(values[0], values[1], values[2], values[3]);
                                            break;
                                        default:
                                            break;
                                    }
                                }
                            }
                            else if (j == expressions.length) {
                                switch (values2.length) {
                                    case 3:
                                        c = new Color(values2[0], values2[1], values2[2]);
                                        break;
                                    case 4:
                                        c = new Color(values2[0], values2[1], values2[2], values2[3]);
                                        break;
                                    default:
                                        break;
                                }
                            }

                            if (c != null) {
                                return new ColorPreviewComponent(c);
                            }
                        }
                    }
                }
            }
        }

        if (isInsideDecodeOrGetColorMethod(element)) {
            final String color = StringUtil.unquoteString(element.getText());
            try {
                return new ColorPreviewComponent(Color.decode(color));
            } catch (NumberFormatException ignore) {}
        }

        if (PlatformPatterns.psiElement(PsiIdentifier.class).withParent(PlatformPatterns.psiElement(PsiReferenceExpression.class))
        .accepts(element)) {
            final PsiReference reference = element.getParent().getReference();

            if (reference != null) {
                final PsiElement psiElement = reference.resolve();

                if (psiElement instanceof PsiField) {
                    if (MetaData.Color.PATH.equals(((PsiField)psiElement).getContainingClass().getQualifiedName())) {
                        final String colorName = StringUtil.toLowerCase(((PsiField)psiElement).getName()).replace("_", "");
                        final String hex = ColorMap.getHexCodeForColorName(colorName);
                        if (hex != null) {
                            return new ColorPreviewComponent(Color.decode("0x" + hex.substring(1)));
                        }
                    }
                }
            }
        }

        if (PlatformPatterns.psiElement().withParent(PlatformPatterns.psiElement(PsiLiteralExpression.class)).accepts(element)) {
            final PsiLiteralExpression psiLiteralExpression = (PsiLiteralExpression) element.getParent();
            if (psiLiteralExpression != null) {
                return ImagePreviewComponent.getPreviewComponent(psiLiteralExpression);
            }
        }

        return null;
    }
}
