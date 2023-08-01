package com.zelaux.arcplugin.codeInsight.template.postfix.templates;

import arc.util.*;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.template.*;
import com.intellij.codeInsight.template.impl.*;
import com.intellij.codeInsight.template.macro.*;
import com.intellij.codeInsight.template.postfix.templates.*;
import com.intellij.codeInsight.template.postfix.templates.editable.*;
import com.intellij.pom.java.*;
import com.intellij.psi.*;
import com.intellij.psi.codeStyle.*;
import com.intellij.util.containers.*;
import com.zelaux.arcplugin.codeInsight.template.postfix.MyPostfixTemplate;
import com.zelaux.arcplugin.codeInsight.template.postfix.conditions.*;
import org.jetbrains.annotations.*;
import org.jetbrains.annotations.Nullable;

public class IteratorPostfixTemplate extends MyPostfixTemplate {
    public IteratorPostfixTemplate(@NotNull String templateName, @NotNull PostfixTemplateProvider provider) {
        super(templateName,
                "$FINAL$$ITER_TYPE$ $ITER_NAME$ = $EXPR$.iterator();\n" +
                        "while ($ITER_NAME$.hasNext) {\n" +
                        "    $FINAL$$TYPE$ $NAME$ = $ITER_NAME$.next();\n" +
                        "    $END$\n" +
                        "}",
                "Iter iter=expr.iterator();\nwhile (iter.hasNext){ iter.next()}",
                ContainerUtil.newHashSet(
                        new JavaPostfixTemplateExpressionTypeCondition("is arc set", TemplateUtils::isArcSetType)),
                provider);
    }


    @Override
    protected void setupTemplateVariables(@NotNull PsiElement element, @NotNull Template template) {
        MacroCallNode name = new MacroCallNode(new SuggestVariableNameMacro());


        MacroCallNode iterType = new MacroCallNode(new ExpressionTypeMacro());
        iterType.addParameter(new VariableNode("EXPR", new TextExpression(element.getText() + ".iterator()")));
//        iterType.addParameter(new TextExpression(element.getText()+".iterator()"));
        template.addVariable("ITER_TYPE", iterType, iterType, false);
        template.addVariable("ITER_NAME", name, name, true);

        MacroCallNode type = new MacroCallNode(new ExpressionTypeMacro());
        type.addParameter(new VariableNode("EXPR", new TextExpression(element.getText() + ".iterator().next()")));
        template.addVariable("TYPE", type, type, false);
        template.addVariable("NAME", name, name, true);

        String finalPart = JavaFileCodeStyleFacade.forContext(element.getContainingFile()).isGenerateFinalLocals() ? "final " : null;
        if (finalPart != null) {
            template.addVariable("FINAL", new TextExpression(finalPart), false);
        }
    }

    static class MyExpr extends Expression {
        public final MacroCallNode node;

        MyExpr(MacroCallNode node) {
            this.node = node;
        }

        @Override
        public @Nullable Result calculateResult(ExpressionContext context) {
            return node.calculateResult(context);
        }

        @Override
        public LookupElement @Nullable [] calculateLookupItems(ExpressionContext context) {
            return node.calculateLookupItems(context);
        }
    }
}
