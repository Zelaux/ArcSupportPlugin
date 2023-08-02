package com.zelaux.arcplugin.commandParam.psi;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFileFactory;
import com.zelaux.arcplugin.commandParam.ArcCommandParamFileType;

public class PsiACPFactory {
    public static PsiACPParamList createList(Project project, String text) {
        final ArcCommandParamsFile file = createFile(project, text);
        return (PsiACPParamList) file.getFirstChild();
    }

    public static ArcCommandParamsFile createFile(Project project, String text) {
        String name = "dummy.simple";
        return (ArcCommandParamsFile) PsiFileFactory.getInstance(project).createFileFromText(name, ArcCommandParamFileType.INSTANCE, text);
    }


    public static PsiACPOptionalParam createOptionalParam(Project project, String name, boolean variadic) {
        String text;
        if (variadic) {
            text = "[" + name + "...]";
        } else {
            text = "[" + name + "]";
        }
        return (PsiACPOptionalParam) createList(project, text).getFirstChild();
    }

    public static PsiACPRequiredParam createRequiredParam(Project project, String name, boolean variadic) {
        String text;
        if (variadic) {
            text = "<" + name + "...>";
        } else {
            text = "<" + name + ">";
        }
        return (PsiACPRequiredParam) createList(project, text).getFirstChild();
    }
}
