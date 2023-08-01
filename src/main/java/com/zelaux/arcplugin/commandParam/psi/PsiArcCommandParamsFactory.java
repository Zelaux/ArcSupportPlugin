package com.zelaux.arcplugin.commandParam.psi;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFileFactory;
import com.zelaux.arcplugin.commandParam.ArcCommandParamFileType;

public class PsiArcCommandParamsFactory {
    public static PsiArcCommandParamsParamList createList(Project project, String text) {
        final ArcCommandParamsFile file = createFile(project, text);
        return (PsiArcCommandParamsParamList) file.getFirstChild();
    }

    public static ArcCommandParamsFile createFile(Project project, String text) {
        String name = "dummy.simple";
        return (ArcCommandParamsFile) PsiFileFactory.getInstance(project).createFileFromText(name, ArcCommandParamFileType.INSTANCE, text);
    }


}
