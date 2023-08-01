package com.zelaux.arcplugin.commandParam.inspection;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import com.zelaux.arcplugin.commandParam.psi.PsiACPParam;
import com.zelaux.arcplugin.commandParam.psi.PsiACPParamList;
import com.zelaux.arcplugin.commandParam.psi.PsiACPVisitor;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class DuplicatedNamesInspection extends LocalInspectionTool {
    @Override
    public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new PsiACPVisitor(){
            @Override
            public void visitParam(@NotNull PsiACPParam o) {
                PsiACPParamList list = (PsiACPParamList) o.getParent();
                List<PsiACPParam> paramList = list.getParamList();
                String myName = o.getParameterName();
                int counter=0;
                //noinspection ForLoopReplaceableByForEach
                for (int i = 0; i < paramList.size(); i++) {
                    PsiACPParam param = paramList.get(i);
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
