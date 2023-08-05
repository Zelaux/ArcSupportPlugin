/*
package com.zelaux.arcplugin.marker.colorexp;

import com.intellij.psi.*;
import com.zelaux.arcplugin.marker.*;
import com.zelaux.arcplugin.parsers.colorexpr.ColorExpParserSequence;
import org.jetbrains.annotations.*;

public class FieldAccessColorExpProvider implements ExpParserSeqProvider {
    @Override
    public @Nullable ColorExpParserSequence expressionParserSequenceFrom(@NotNull PsiElement element) {
if(true)return null;
        if (!(element instanceof PsiReferenceExpression)) return null;
        PsiElement parent = element.getParent();
        if (parent instanceof PsiReferenceExpression) return null;
        if (parent instanceof PsiExpressionList) {
            if (parent.getParent() instanceof PsiCallExpression) {
                try {
//                    ColorExpParserSequence sequence = SmartColorResolver.resolveColor((PsiExpression) parent.getParent());
//                    if (sequence != null) return null;
                } catch (Exception ignored) {
                }
            }
        }
return null;
//        StaticSetColorParser parser = new StaticSetColorParser(element, (_p, elem) -> elem);
//        if (!parser.validate()) {
//            return null;
//        }
//        return parser.wrap(element.getText()).asSequence();
    }

}
*/
