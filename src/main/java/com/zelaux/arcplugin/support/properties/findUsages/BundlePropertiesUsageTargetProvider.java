package com.zelaux.arcplugin.support.properties.findUsages;

import com.intellij.find.FindManager;
import com.intellij.find.findUsages.PsiElement2UsageTargetAdapter;
import com.intellij.navigation.NavigationItem;
import com.intellij.psi.PsiElement;
import com.intellij.usages.UsageTarget;
import com.intellij.usages.UsageTargetProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BundlePropertiesUsageTargetProvider implements UsageTargetProvider {
    @Override
    public UsageTarget[] getTargets(@NotNull PsiElement psiElement) {
        if (psiElement instanceof NavigationItem) {
            if (FindManager.getInstance(psiElement.getProject()).canFindUsages(psiElement)) {
                return new UsageTarget[]{new MyUsageTarget(psiElement)};
            }
        }
        return null;
    }
    static class MyUsageTarget extends PsiElement2UsageTargetAdapter{

        public MyUsageTarget(@NotNull PsiElement element) {
            super(element, true);
        }

        @Override
        public String getPresentableText() {
            return "@"+super.getPresentableText();
        }
    }
}
