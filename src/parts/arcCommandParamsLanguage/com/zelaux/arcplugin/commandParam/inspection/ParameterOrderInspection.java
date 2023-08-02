package com.zelaux.arcplugin.commandParam.inspection;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.codeInspection.util.IntentionFamilyName;
import com.intellij.codeInspection.util.IntentionName;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.zelaux.arcplugin.commandParam.psi.*;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ParameterOrderInspection extends LocalInspectionTool {
    @Override
    public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new PsiACPVisitor(){
            @Override
            public void visitRequiredParam(@NotNull PsiACPRequiredParam o) {
                if (isMyCase(o)) {
                    holder.registerProblem(o,"Required parameters cannot be after optional",
                            new SwitchParameterTypeInspection.ToOptional(),
                            new VariadicParamPositionInspection.RearrangeFix(),
                            new ChangeAllBeforeToRequiredFix()
                            );
                }

            }
        };
    }

    public static boolean isMyCase(PsiACPRequiredParam o) {
        PsiElement parent = o.getParent();
        if(!(parent instanceof PsiACPParamList))return false;
        List<PsiACPParam> paramList = ((PsiACPParamList) parent).getParamList();

        for (int i = 0; i < paramList.size(); i++) {
            PsiACPParam param = paramList.get(i);
            if(param==o)break;
            if (param.isOptional()) return true;
        }
        return false;
    }

    static class ChangeAllBeforeToRequiredFix implements LocalQuickFix {
        @Override
        public @IntentionName @NotNull String getName() {
            return "Transform all optional parameters before this parameter to required.";
        }

        @Override
        public @IntentionFamilyName @NotNull String getFamilyName() {
            return "Arc";
        }

        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            List<PsiACPParam> paramList = ((PsiACPParamList) descriptor.getPsiElement().getParent()).getParamList();
            PsiElement obj = descriptor.getPsiElement();
            WriteAction.run(()->{
                for (PsiACPParam param : paramList) {
                    if(obj==param)break;
                    if(param.isOptional()){
                        SwitchParameterTypeInspection.ToRequired.changeToRequired(project, (PsiACPOptionalParam) param);
                    }
                }
            });
        }
    }
}
