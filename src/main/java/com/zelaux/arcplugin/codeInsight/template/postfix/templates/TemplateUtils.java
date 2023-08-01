package com.zelaux.arcplugin.codeInsight.template.postfix.templates;

import arc.struct.EnumSet;
import arc.util.Structs;
import com.intellij.psi.PsiType;
import com.intellij.psi.util.PsiClassUtil;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiUtil;
import com.zelaux.arcplugin.utils.PsiUtils;

public class TemplateUtils {

    static boolean isArcSetType(PsiType type) {
        while (type != null) {
            if (validSetType(type)) return true;
            PsiType[] superTypes = type.getSuperTypes();
            if (superTypes.length == 0) break;
            type = superTypes[0];

            for (int i = 1; i < superTypes.length; i++) {
                if (isArcSetType(superTypes[i])) return true;
            }
        }
        return false;
    }

    static boolean isInstance(PsiType target, String parent) {
        while (target != null) {
            if (PsiUtils.isType(parent, target)) return true;
            PsiType[] superTypes = target.getSuperTypes();
            if (superTypes.length == 0) break;
            target = superTypes[0];

            for (int i = 1; i < superTypes.length; i++) {
                if (isInstance(superTypes[i], parent)) return true;
            }
        }
        return false;


    }

    private static boolean validSetType(PsiType psiType) {
        String text = psiType.getCanonicalText();
        if (text.equals(EnumSet.class.getCanonicalName())) {
            return false;
        }
        return text.contains("arc.struct") && !text.contains("ObjectSet") && text.contains("Set");
    }
}
