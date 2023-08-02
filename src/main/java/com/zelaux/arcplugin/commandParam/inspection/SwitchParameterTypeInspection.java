package com.zelaux.arcplugin.commandParam.inspection;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.codeInspection.util.IntentionFamilyName;
import com.intellij.codeInspection.util.IntentionName;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElementVisitor;
import com.zelaux.arcplugin.commandParam.psi.PsiACPFactory;
import com.zelaux.arcplugin.commandParam.psi.PsiACPOptionalParam;
import com.zelaux.arcplugin.commandParam.psi.PsiACPRequiredParam;
import com.zelaux.arcplugin.commandParam.psi.PsiACPVisitor;
import org.jetbrains.annotations.NotNull;

public class SwitchParameterTypeInspection extends LocalInspectionTool {
    @Override
    public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new PsiACPVisitor() {
            @Override
            public void visitRequiredParam(@NotNull PsiACPRequiredParam o) {
                if(!ParameterOrderInspection.isMyCase(o))
                holder.registerProblem(o, "Transform to optional", new ToOptional());
            }

            @Override
            public void visitOptionalParam(@NotNull PsiACPOptionalParam o) {
                holder.registerProblem(o, "Transform to required", new ToRequired());
            }
        };
    }

    static class ToOptional implements LocalQuickFix {
        @Override
        public @IntentionFamilyName @NotNull String getFamilyName() {
            return "ParameterTransformation";
        }

        @Override
        public @IntentionName @NotNull String getName() {
            return "Transform to optional";
        }

        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            PsiACPRequiredParam param = (PsiACPRequiredParam) descriptor.getPsiElement();
            WriteAction.run(() -> {
                changeToOptional(project, param);
            });
        }

        public static void changeToOptional(@NotNull Project project, PsiACPRequiredParam param) {
            param.replace(PsiACPFactory.createOptionalParam(project, param.getParameterName(), param.isVariadic()));
        }
    }

    static class ToRequired implements LocalQuickFix {
        @Override
        public @IntentionFamilyName @NotNull String getFamilyName() {
            return "ParameterTransformation";
        }

        @Override
        public @IntentionName @NotNull String getName() {
            return "Transform to required";
        }
        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            PsiACPOptionalParam param = (PsiACPOptionalParam) descriptor.getPsiElement();
            WriteAction.run(() -> {
                changeToRequired(project, param);
            });
        }

        public static void changeToRequired(@NotNull Project project, PsiACPOptionalParam param) {
            param.replace(PsiACPFactory.createRequiredParam(project, param.getParameterName(), param.isVariadic()));
        }
    }
}
