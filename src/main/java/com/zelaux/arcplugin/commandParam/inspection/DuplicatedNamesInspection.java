package com.zelaux.arcplugin.commandParam.inspection;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import com.zelaux.arcplugin.commandParam.psi.PsiArcCommandParamsParam;
import com.zelaux.arcplugin.commandParam.psi.PsiArcCommandParamsParamList;
import com.zelaux.arcplugin.commandParam.psi.PsiArcCommandParamsVisitor;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class DuplicatedNamesInspection extends LocalInspectionTool {
    @Override
    public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new PsiArcCommandParamsVisitor(){
            @Override
            public void visitParam(@NotNull PsiArcCommandParamsParam o) {
                PsiArcCommandParamsParamList list = (PsiArcCommandParamsParamList) o.getParent();
                List<PsiArcCommandParamsParam> paramList = list.getParamList();
                String myName = o.getParameterName();
                int counter=0;
                //noinspection ForLoopReplaceableByForEach
                for (int i = 0; i < paramList.size(); i++) {
                    PsiArcCommandParamsParam param = paramList.get(i);
                    if (param.getParameterName().equals(myName)) {
                        counter++;
                        if(counter>1)break;
                    }
                }
                if(counter>1){
                    holder.registerProblem(o,"Duplicated param");
                }
            }
        };
    }
}
