package com.zelaux.arcplugin.commandParam.inspection;

import arc.util.Structs;
import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.codeInspection.util.IntentionFamilyName;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElementVisitor;
import com.zelaux.arcplugin.commandParam.psi.*;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

public class VariadicParamPositionInspection extends LocalInspectionTool {
    @Override
    public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
//        return super.buildVisitor(holder, isOnTheFly);
        return new PsiACPVisitor(){

            @Override
            public void visitParam(@NotNull PsiACPParam o) {

                if (o.isVariadic()) {
                    PsiACPParamList list = (PsiACPParamList) o.getParent();
                    List<PsiACPParam> paramList = list.getParamList();
                    int i = paramList.indexOf(o);
                    if(i!=paramList.size()-1){
                        if (!o.isOptional() && paramList.get(paramList.size()-1).isOptional()) {
                            holder.registerProblem(o, "Variadic parameter must be the last[No Fix]" );
                        }else {
                            holder.registerProblem(o, "Variadic parameter must be the last", new RearrangeFix());
                        }
                    }
                    int counter=0;
                    for (PsiACPParam param : paramList) {
                        if (param.isVariadic()) counter++;
                    }
                    if(counter>1){
                        holder.registerProblem(o,"Cannot be more than 1 Variadic parameter");
                    }
                }
            }
        };
    }
    static class RearrangeFix implements LocalQuickFix {

        @Override
        public @IntentionFamilyName @NotNull String getFamilyName() {
            return "Rearrange params";
        }

        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            PsiACPParam psiElement = (PsiACPParam) descriptor.getPsiElement();
            PsiACPParamList parent = (PsiACPParamList) psiElement.getParent();
            List<PsiACPParam> list = new ArrayList<>(parent.getParamList());
            list.sort(Structs.comps(
                    Structs.comparing(PsiACPParamI::isOptional),
                    Structs.comparing(PsiACPParamI::isVariadic)
            ));
            StringJoiner joiner = new StringJoiner(" ");
            for (PsiACPParam param : list) {
                joiner.add(param.getText());
            }

            WriteAction.run(()->{
                parent.replace(PsiACPFactory.createList(project,joiner.toString()));
            });
        }
    }
}
