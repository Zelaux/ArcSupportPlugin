package com.zelaux.arcplugin.marker.color;

import com.intellij.openapi.extensions.*;
import com.intellij.psi.*;
import com.zelaux.arcplugin.colorViewer.*;
import org.jetbrains.annotations.*;
import org.jetbrains.uast.*;

import java.awt.*;

public interface ElementColorViewerProvider{
    ExtensionPointName<ElementColorViewerProvider> EP_NAME = ExtensionPointName.create("com.zelaux.arcplugin.customColorProvider");
//com.intellij.codeInsight.daemon.impl              java/java-impl/src/com/intellij/codeInsight/daemon/impl/JavaColorProvider.java
//com.intellij.codeInsight.intention.impl           java/java-impl/src/com/intellij/codeInsight/intention/impl/BaseColorIntentionAction.java
//com.intellij.codeInsight.javadoc                  java/java-impl/src/com/intellij/codeInsight/javadoc/JavaDocColorUtil.java
//com.intellij.codeInsight.preview                  java/java-impl/src/com/intellij/codeInsight/preview/JavaPreviewHintProvider.java
//com.intellij.debugger.ui.tree.render              java/debugger/impl/src/com/intellij/debugger/ui/tree/render/ColorObjectRenderer.java
//com.intellij.java.psi                             java/java-tests/testSrc/com/intellij/java/psi/PsiNameHelperTest.java
//com.intellij.uiDesigner.lw                        java/compiler/forms-compiler/src/com/intellij/uiDesigner/lw/CompiledClassPropertiesProvider.java
//com.intellij.uiDesigner.lw                        java/compiler/forms-compiler/src/com/intellij/uiDesigner/lw/LwIntroColorProperty.java
//com.siyeh.ig.psiutils                             plugins/InspectionGadgets/InspectionGadgetsAnalysis/src/com/siyeh/ig/psiutils/ClassUtils.java
//org.jetbrains.idea.devkit.inspections             plugins/devkit/devkit-core/src/inspections/UseGrayInspection.java
//org.jetbrains.idea.devkit.inspections.internal    plugins/devkit/devkit-core/src/inspections/internal/UseJBColorInspection.java
//org.jetbrains.plugins.groovy.swingBuilder         plugins/groovy/groovy-psi/src/org/jetbrains/plugins/groovy/swingBuilder/SwingBuilderNonCodeMemberContributor.java
    @Nullable
    CustomColorViewer getColorViewerFrom(@NotNull PsiElement element);
    @Nullable Color getColor(UCallExpression expression);

}
