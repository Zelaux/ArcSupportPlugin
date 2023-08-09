package com.zelaux.arcplugin.events;


import arc.Events;
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo;
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider;
import com.zelaux.arcplugin.codeInsight.navigation.UpdatableNavigationGutterIconBuilder;
import com.intellij.ide.util.DefaultPsiElementCellRenderer;
import com.intellij.navigation.GotoRelatedItem;
import com.intellij.openapi.editor.markup.GutterIconRenderer.Alignment;
import com.intellij.openapi.util.NotNullLazyValue;
import com.intellij.psi.*;
import com.intellij.psi.presentation.java.SymbolPresentationUtil;
import com.intellij.psi.util.InheritanceUtil;
import com.intellij.util.NotNullFunction;
import com.intellij.util.SmartList;
import com.zelaux.arcplugin.PluginIcons;
import com.zelaux.arcplugin.events.indexing.EventIndexingManager;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.uast.*;

import javax.swing.*;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.zelaux.arcplugin.events.EventsUtils.extractType;


public class EventLineMarkerProvider extends RelatedItemLineMarkerProvider {


    public static Stream<FireEventPoint> getFirePoints(EventType eventType) {
        SmartList<FireEventPoint> dynamic = EventIndexingManager.projectFirePoints.getValue().get(eventType);
        SmartList<FireEventPoint> dependencies = EventIndexingManager.librariesFirePoints.getValue().get(eventType);
        return combineStream(dynamic, dependencies);
    }

    public static Stream<EventSubscription> getEventListeners(EventType type) {
        SmartList<EventSubscription> dynamic = EventIndexingManager.projectEventSubscription.getValue().get(type);
        SmartList<EventSubscription> dependencies = EventIndexingManager.librariesEventSubscription.getValue().get(type);
        return combineStream(dynamic, dependencies);
    }

    private static <T> Stream<T> combineStream(SmartList<T> dynamic, SmartList<T> dependencies) {
        if (dynamic == null && dependencies == null) return Stream.of();
        if (dynamic != null && dependencies == null) return dynamic.stream();
        if (dynamic == null) return dependencies.stream();
        return Stream.concat(dynamic.stream(), dependencies.stream());
    }

    @NotNull
    private static DefaultPsiElementCellRenderer getRenderer() {
        return new DefaultPsiElementCellRenderer() {
            @Override
            public String getElementText(PsiElement element) {
                String text = super.getElementText(element);
                int index = text.indexOf("\n");
                if (index != -1) return text.substring(0, index);
                return text;
            }

            @Override
            public String getContainerText(PsiElement element, String name) {
                PsiFile file = element.getContainingFile();
                if (file instanceof PsiFileSystemItem) {
                    return SymbolPresentationUtil.getFilePathPresentation((PsiFileSystemItem) file);
                }
                return super.getContainerText(element, name);
            }
        };
    }

    @Override
    protected void collectNavigationMarkers(@NotNull PsiElement element, @NotNull Collection<? super RelatedItemLineMarkerInfo<?>> result) {
        Intrinsics.checkNotNullParameter(element, "element");
        Intrinsics.checkNotNullParameter(result, "result");
        UElement uParent = UastUtils.getUParentForIdentifier(element);
        UCallExpression uCallExpression = UastContextKt.toUElement(element, UCallExpression.class);

        if (uCallExpression != null && Intrinsics.areEqual(uCallExpression.getKind(), UastCallKind.METHOD_CALL))
            if (this.isPublishEventExpression(uCallExpression)) {
                final PsiElement sourcePsi = uCallExpression.getSourcePsi();
                PsiElement identifier = UElementKt.getSourcePsiElement(uCallExpression.getMethodIdentifier());
                if (identifier != null && sourcePsi != null) {
                    UpdatableNavigationGutterIconBuilder<PsiElement> builder = MyNavigationGutterIconBuilder
                            .create(PluginIcons.SENDER)
                            .setAlignment(Alignment.LEFT)
                            .setEmptyPopupText("Subscribers not found")
                            .setCellRenderer(EventLineMarkerProvider::getRenderer)
                            .setTargets((() -> findEventListeners(sourcePsi)))
                            .setTooltipText("Navigate to event listeners");

                    result.add(builder.createLineMarkerInfo(identifier));
                }
            } else if (this.isEventListenerExpression(uCallExpression)) {
                final PsiElement sourcePsi = uCallExpression.getSourcePsi();
                PsiElement identifier = UElementKt.getSourcePsiElement(uCallExpression.getMethodIdentifier());
                if (identifier != null && sourcePsi != null) {
                    UpdatableNavigationGutterIconBuilder<PsiElement> builder = MyNavigationGutterIconBuilder
                            .create(PluginIcons.RECEIVER)
                            .setAlignment(Alignment.LEFT)
                            .setEmptyPopupText("Publishers not found")
                            .setCellRenderer(EventLineMarkerProvider::getRenderer)
                            .setTargets((() -> getPublisherTargets(uCallExpression)))
                            .setTooltipText("Navigate to event publisher");

                    result.add(builder.createLineMarkerInfo(identifier));
                }
            }

    }

    private List<PsiMethodCallExpression> getPublisherTargets(UCallExpression element) {
        if (element.getValueArgumentCount() != 2) return Collections.emptyList();

        EventType eventType = extractType(element);
        return getFirePoints(eventType)
                .filter(it -> it.element instanceof PsiMethodCallExpression)
                .map(it -> (PsiMethodCallExpression) (it.element)).collect(Collectors.toList());
    }

    private final Collection<PsiElement> findEventListeners(PsiElement psiElement) {
//        com.intellij.openapi.fileEditor.FileEditorManager.getInstance(psiElement.getProject())

        UCallExpression uCallExpression = UastContextKt.toUElement(psiElement, UCallExpression.class);
        if (uCallExpression == null) return Collections.emptyList();

        if (uCallExpression.getValueArgumentCount() != 1) {
            return Collections.emptyList();
        }
        EventType eventType = extractType(uCallExpression);
        if (eventType == null) {
            return Collections.emptyList();
        }
        return this.getEventListeners(eventType)
                .filter(it -> it.element != null)
                .map(it -> it.element.getNavigationElement())
                .collect(Collectors.toList());
    }


    private final boolean isPublishEventExpression(UCallExpression uMethodCall) {
        if (Intrinsics.areEqual(uMethodCall.getMethodName(), "fire")) {
            PsiMethod psiMethod = uMethodCall.resolve();
            if (psiMethod != null) {
                PsiClass targetClass = psiMethod.getContainingClass();
                return InheritanceUtil.isInheritor(targetClass, Events.class.getCanonicalName());
            }
        }

        return false;
    }

    private final boolean isEventListenerExpression(UCallExpression uMethodCall) {
        if (Intrinsics.areEqual(uMethodCall.getMethodName(), "on") || Intrinsics.areEqual(uMethodCall.getMethodName(), "run")) {
            PsiMethod psiMethod = uMethodCall.resolve();
            if (psiMethod != null) {
                PsiClass targetClass = psiMethod.getContainingClass();
                return InheritanceUtil.isInheritor(targetClass, Events.class.getCanonicalName());
            }
        }

        return false;
    }

    private static class MyNavigationGutterIconBuilder<T> extends UpdatableNavigationGutterIconBuilder<T> {
        protected MyNavigationGutterIconBuilder(@NotNull Icon icon, @NotNull NotNullFunction<? super T, ? extends Collection<? extends PsiElement>> converter, @Nullable NotNullFunction<? super T, ? extends Collection<? extends GotoRelatedItem>> gotoRelatedItemProvider) {
            super(icon, converter, gotoRelatedItemProvider);
        }

        public static MyNavigationGutterIconBuilder<PsiElement> create(@NotNull Icon icon) {
            return new MyNavigationGutterIconBuilder<>(icon, DEFAULT_PSI_CONVERTOR, PSI_GOTO_RELATED_ITEM_PROVIDER);
        }




    }
}
