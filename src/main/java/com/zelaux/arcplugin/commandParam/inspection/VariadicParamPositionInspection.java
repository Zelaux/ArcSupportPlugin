package com.zelaux.arcplugin.commandParam.inspection;

import arc.util.Structs;
import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.codeInspection.util.IntentionFamilyName;
import com.intellij.designer.model.QuickFix;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import com.zelaux.arcplugin.commandParam.psi.*;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collectors;

public class VariadicParamPositionInspection extends LocalInspectionTool {
    @Override
    public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
//        return super.buildVisitor(holder, isOnTheFly);
        return new PsiArcCommandParamsVisitor(){

            @Override
            public void visitParam(@NotNull PsiArcCommandParamsParam o) {

                if (o.isVariadic()) {
                    PsiArcCommandParamsParamList list = (PsiArcCommandParamsParamList) o.getParent();
                    List<PsiArcCommandParamsParam> paramList = list.getParamList();
                    int i = paramList.indexOf(o);
                    if(i!=paramList.size()-1){
                        if (!o.isOptional() && paramList.get(paramList.size()-1).isOptional()) {
                            holder.registerProblem(o, "Variadic parameter must be the last[No Fix]" );
                        }else {
                            holder.registerProblem(o, "Variadic parameter must be the last", new MyQuickFix());
                        }
                    }
                    int counter=0;
                    for (PsiArcCommandParamsParam param : paramList) {
                        if (param.isVariadic()) counter++;
                    }
                    if(counter>1){
                        holder.registerProblem(o,"Cannot be more than 1 Variadic parameter");
                    }
                }
            }
        };
    }
    static class MyQuickFix implements LocalQuickFix {

        @Override
        public @IntentionFamilyName @NotNull String getFamilyName() {
            return "Rearrange params";
        }

        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            PsiArcCommandParamsParam psiElement = (PsiArcCommandParamsParam) descriptor.getPsiElement();
            PsiArcCommandParamsParamList parent = (PsiArcCommandParamsParamList) psiElement.getParent();
            List<PsiArcCommandParamsParam> list = new ArrayList<>(parent.getParamList());
            list.sort(Structs.comps(
                    Structs.comparing(PsiArcCommandParamsParamI::isOptional),
                    Structs.comparing(PsiArcCommandParamsParamI::isVariadic)
            ));
            StringJoiner joiner = new StringJoiner(" ");
            for (PsiArcCommandParamsParam param : list) {
                joiner.add(param.getText());
            }

            WriteAction.run(()->{
                parent.replace(PsiArcCommandParamsFactory.createList(project,joiner.toString()));
            });
        }
    }
}
