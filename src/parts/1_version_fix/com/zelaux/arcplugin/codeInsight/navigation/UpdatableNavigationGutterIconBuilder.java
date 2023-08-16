// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.zelaux.arcplugin.codeInsight.navigation;

import com.intellij.codeInsight.daemon.GutterIconNavigationHandler;
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo;
import com.intellij.codeInsight.navigation.DomGotoRelatedItem;
import com.intellij.codeInsight.navigation.NavigationGutterIconRenderer;
import com.intellij.codeInspection.InspectionsBundle;
import com.intellij.ide.util.DefaultPsiElementCellRenderer;
import com.intellij.ide.util.PsiElementListCellRenderer;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.navigation.GotoRelatedItem;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.*;
import com.intellij.openapi.util.NlsContexts.PopupContent;
import com.intellij.openapi.util.NlsContexts.PopupTitle;
import com.intellij.openapi.util.NlsContexts.Tooltip;
import com.intellij.psi.PsiElement;
import com.intellij.psi.SmartPointerManager;
import com.intellij.psi.SmartPsiElementPointer;
import com.intellij.util.ConstantFunction;
import com.intellij.util.NotNullFunction;
import com.intellij.util.NullableFunction;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.ElementPresentationManager;
import com.zelaux.arcplugin.util.NotNullLazyRecalculatableValue;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.text.MessageFormat;
import java.util.*;

/**
 * DOM-specific builder for {@link GutterIconRenderer}
 * and {@link com.intellij.codeInsight.daemon.LineMarkerInfo}.
 */
@SuppressWarnings({"UnstableApiUsage", "unused"})
public class UpdatableNavigationGutterIconBuilder<T> {
    public static final NotNullFunction<DomElement, Collection<? extends PsiElement>> DEFAULT_DOM_CONVERTOR =
            o -> ContainerUtil.createMaybeSingletonList(o.getXmlElement());
    public static final NotNullFunction<DomElement, Collection<? extends GotoRelatedItem>> DOM_GOTO_RELATED_ITEM_PROVIDER = dom -> {
        if (dom.getXmlElement() != null) {
            return List.of(new DomGotoRelatedItem(dom));
        }
        return Collections.emptyList();
    };
    protected static final NotNullFunction<PsiElement, Collection<? extends PsiElement>> DEFAULT_PSI_CONVERTOR =
            ContainerUtil::createMaybeSingletonList;
    protected static final NotNullFunction<PsiElement, Collection<? extends GotoRelatedItem>> PSI_GOTO_RELATED_ITEM_PROVIDER =
            dom -> List.of(new GotoRelatedItem(dom, InspectionsBundle.message("xml.goto.group")));
    @NonNls
    private static final String PATTERN = "&nbsp;&nbsp;&nbsp;&nbsp;{0}";
    protected final Icon myIcon;
    private final NotNullFunction<? super T, ? extends Collection<? extends PsiElement>> myConverter;
    private final NotNullFunction<? super T, ? extends Collection<? extends GotoRelatedItem>> myGotoRelatedItemProvider;
    protected NotNullFactory<Collection<? extends T>> myTargets;
    protected @Tooltip String myTooltipText;
    protected @PopupTitle String myPopupTitle;
    protected @PopupContent String myEmptyText;
    protected GutterIconRenderer.Alignment myAlignment = GutterIconRenderer.Alignment.CENTER;
    private boolean myLazy;
    @PopupTitle
    private String myTooltipTitle;
    private Computable<PsiElementListCellRenderer<?>> myCellRenderer;
    private @NotNull NullableFunction<? super T, String> myNamer = ElementPresentationManager.namer();

    protected UpdatableNavigationGutterIconBuilder(@NotNull final Icon icon, @NotNull NotNullFunction<? super T, ? extends Collection<? extends PsiElement>> converter) {
        this(icon, converter, null);
    }

    protected UpdatableNavigationGutterIconBuilder(@NotNull final Icon icon,
                                                   @NotNull NotNullFunction<? super T, ? extends Collection<? extends PsiElement>> converter,
                                                   @Nullable final NotNullFunction<? super T, ? extends Collection<? extends GotoRelatedItem>> gotoRelatedItemProvider) {
        myIcon = icon;
        myConverter = converter;
        myGotoRelatedItemProvider = gotoRelatedItemProvider;
    }

    @NotNull
    public static UpdatableNavigationGutterIconBuilder<PsiElement> create(@NotNull Icon icon) {
        return create(icon, DEFAULT_PSI_CONVERTOR, PSI_GOTO_RELATED_ITEM_PROVIDER);
    }

    @NotNull
    public static UpdatableNavigationGutterIconBuilder<PsiElement> create(@NotNull Icon icon, @NlsContexts.Separator String navigationGroup) {
        return create(icon, DEFAULT_PSI_CONVERTOR, element -> List.of(new GotoRelatedItem(element, navigationGroup)));
    }

    @NotNull
    public static <T> UpdatableNavigationGutterIconBuilder<T> create(@NotNull final Icon icon,
                                                                     @NotNull NotNullFunction<? super T, ? extends Collection<? extends PsiElement>> converter) {
        return create(icon, converter, null);
    }

    @NotNull
    public static <T> UpdatableNavigationGutterIconBuilder<T> create(@NotNull final Icon icon,
                                                                     @NotNull NotNullFunction<? super T, ? extends Collection<? extends PsiElement>> converter,
                                                                     @Nullable final NotNullFunction<? super T, ? extends Collection<? extends GotoRelatedItem>> gotoRelatedItemProvider) {
        return new UpdatableNavigationGutterIconBuilder<>(icon, converter, gotoRelatedItemProvider);
    }

    @NotNull
    private static <T> NotNullFactory<T> evaluateAndForget(@NotNull NotNullFactory<T> lazyValue) {
        final Ref<NotNullFactory<T>> ref = Ref.create(lazyValue);
        return new NotNullFactory<>() {
            volatile T value;

            @NotNull
            @Override
            public T create() {
                T result = value;
                if (result == null) {
                    value = result = ref.get().create();
                    ref.set(null);
                }
                return result;
            }
        };
    }

    @NotNull
    private static <T> NotNullLazyValue<List<SmartPsiElementPointer<?>>> createPointersThunk(boolean lazy,
                                                                                             final Project project,
                                                                                             final NotNullFactory<? extends Collection<? extends T>> targets,
                                                                                             final NotNullFunction<? super T, ? extends Collection<? extends PsiElement>> converter) {
        if (!lazy) {
            return NotNullLazyValue.createConstantValue(calcPsiTargets(project, targets.create(), converter));
        }

//        return NotNullLazyValue.lazy(() -> calcPsiTargets(project, targets.create(), converter));
        return NotNullLazyRecalculatableValue.create(()->
                calcPsiTargets(project,targets.create(),converter)
                );

    }

    @NotNull
    private static <T> List<SmartPsiElementPointer<?>> calcPsiTargets(@NotNull Project project,
                                                                      @NotNull Collection<? extends T> targets,
                                                                      @NotNull NotNullFunction<? super T, ? extends Collection<? extends PsiElement>> converter) {
        SmartPointerManager manager = SmartPointerManager.getInstance(project);
        Set<PsiElement> elements = new HashSet<>();
        final List<SmartPsiElementPointer<?>> list = new ArrayList<>(targets.size());
        for (final T target : targets) {
            for (final PsiElement psiElement : converter.fun(target)) {
                if (psiElement == null) {
                    throw new IllegalArgumentException(converter + " returned null element");
                }

                if (elements.add(psiElement) && psiElement.isValid()) {
                    list.add(manager.createSmartPsiElementPointer(psiElement));
                }
            }
        }
        return list;
    }

    @NotNull
    public UpdatableNavigationGutterIconBuilder<T> setTarget(@Nullable T target) {
        return setTargets(ContainerUtil.createMaybeSingletonList(target));
    }

    @SafeVarargs
    @NotNull
    public final UpdatableNavigationGutterIconBuilder<T> setTargets(T @NotNull ... targets) {
        return setTargets(Arrays.asList(targets));
    }

    @NotNull
    public UpdatableNavigationGutterIconBuilder<T> setTargets(@NotNull final NotNullFactory<Collection<? extends T>> targets) {
        myTargets = targets;
        myLazy = true;
        return this;
    }

    @NotNull
    public UpdatableNavigationGutterIconBuilder<T> setTargets(@NotNull final Collection<? extends T> targets) {
        if (ContainerUtil.containsIdentity(targets, null)) {
            throw new IllegalArgumentException("Must not pass collection with null target but got: " + targets);
        }
        myTargets = ()->targets;
        return this;
    }

    @NotNull
    public UpdatableNavigationGutterIconBuilder<T> setTooltipText(@NotNull @Tooltip String tooltipText) {
        myTooltipText = tooltipText;
        return this;
    }

    @NotNull
    public UpdatableNavigationGutterIconBuilder<T> setAlignment(@NotNull final GutterIconRenderer.Alignment alignment) {
        myAlignment = alignment;
        return this;
    }

    @NotNull
    public UpdatableNavigationGutterIconBuilder<T> setPopupTitle(@NotNull @PopupTitle String popupTitle) {
        myPopupTitle = popupTitle;
        return this;
    }

    @NotNull
    public UpdatableNavigationGutterIconBuilder<T> setEmptyPopupText(@NotNull @PopupContent String emptyText) {
        myEmptyText = emptyText;
        return this;
    }

    @NotNull
    public UpdatableNavigationGutterIconBuilder<T> setTooltipTitle(final @NotNull @PopupTitle String tooltipTitle) {
        myTooltipTitle = tooltipTitle;
        return this;
    }

    @NotNull
    public UpdatableNavigationGutterIconBuilder<T> setNamer(@NotNull NullableFunction<? super T, String> namer) {
        myNamer = namer;
        return this;
    }

    /**
     * This method may lead to a deadlock when used from pooled thread, e.g., from
     * {@link com.intellij.codeInsight.daemon.LineMarkerProvider#collectSlowLineMarkers(List, Collection)}.
     * {@link PsiElementListCellRenderer} is a UI component that acquires Swing tree lock on init.
     *
     * @deprecated Use {@link #setCellRenderer(Computable)} instead, then renderer will be instantiated lazily and from EDT
     */
    @Deprecated
    @NotNull
    public UpdatableNavigationGutterIconBuilder<T> setCellRenderer(@NotNull final PsiElementListCellRenderer<?> cellRenderer) {
        myCellRenderer = new Computable.PredefinedValueComputable<>(cellRenderer);
        return this;
    }

    /**
     * @param cellRendererProvider list cell renderer for navigation popup
     */
    public @NotNull UpdatableNavigationGutterIconBuilder<T> setCellRenderer(@NotNull Computable<PsiElementListCellRenderer<?>> cellRendererProvider) {
        myCellRenderer = cellRendererProvider;
        return this;
    }

    /*public void createGutterIcon(@NotNull AnnotationHolder holder, @Nullable PsiElement element) {
        if (!myLazy && myTargets.getValue().isEmpty() || element == null) return;

        NavigationGutterIconRenderer renderer = createGutterIconRenderer(element.getProject());

        holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                .range(element)
                .gutterIconRenderer(renderer)
                .needsUpdateOnTyping(false)
                .create();
    }*/

    @NotNull
    public RelatedItemLineMarkerInfo<PsiElement> createLineMarkerInfo(@NotNull PsiElement element) {
        NavigationGutterIconRenderer renderer = createGutterIconRenderer(element.getProject());
        return createLineMarkerInfo(element, renderer.isNavigateAction() ? renderer : null);
    }

    @NotNull
    public RelatedItemLineMarkerInfo<PsiElement> createLineMarkerInfo(@NotNull PsiElement element,
                                                                      @Nullable GutterIconNavigationHandler<PsiElement> navigationHandler) {
        NavigationGutterIconRenderer renderer = createGutterIconRenderer(element.getProject());
        String tooltip = renderer.getTooltipText();
        return new RelatedItemLineMarkerInfo<>(
                element,
                element.getTextRange(),
                renderer.getIcon(),
                tooltip == null ? null : new ConstantFunction<>(tooltip),
                navigationHandler,
                renderer.getAlignment(),
                this::computeGotoTargets) {
            @Override
            public @NotNull Collection<? extends GotoRelatedItem> createGotoRelatedItems() {
                return computeGotoTargets();
            }
        };
    }

    @NotNull
    protected Collection<GotoRelatedItem> computeGotoTargets() {
        if (myTargets == null || myGotoRelatedItemProvider == null) return Collections.emptyList();
        NotNullFactory<Collection<? extends T>> factory = evaluateAndForget(myTargets);
        return ContainerUtil.concat(factory.create(), myGotoRelatedItemProvider);
    }

    private void checkBuilt() {
        assert myTargets != null : "Must have called .setTargets() before calling create()";
    }

    @NotNull
    protected NavigationGutterIconRenderer createGutterIconRenderer(@NotNull Project project) {
        checkBuilt();

        NotNullFactory<Collection<? extends T>> factory = evaluateAndForget(myTargets);
        NotNullLazyValue<List<SmartPsiElementPointer<?>>> pointers = createPointersThunk(myLazy, project, factory, myConverter);

        final boolean empty = isEmpty();

        if (myTooltipText == null && !myLazy) {
            final SortedSet<String> names = new TreeSet<>();
            for (T t : myTargets.create()) {
                final String text = myNamer.fun(t);
                if (text != null) {
                    names.add(MessageFormat.format(PATTERN, text));
                }
            }
            @Nls StringBuilder sb = new StringBuilder("<html><body>");
            if (myTooltipTitle != null) {
                sb.append(myTooltipTitle).append("<br>");
            }
            for (String name : names) {
                sb.append(name).append("<br>");
            }
            sb.append("</body></html>");
            myTooltipText = sb.toString();
        }

        Computable<PsiElementListCellRenderer<?>> renderer =
                myCellRenderer == null ? DefaultPsiElementCellRenderer::new : myCellRenderer;
        //        gutterIconRenderer.setProject(project);
//        gutterIconRenderer.setTargetRenderer(myTargetRenderer);
        return createGutterIconRenderer(pointers, renderer, empty);
    }

    @NotNull
    protected NavigationGutterIconRenderer createGutterIconRenderer(@NotNull NotNullLazyValue<? extends List<SmartPsiElementPointer<?>>> pointers,
                                                                    @NotNull Computable<? extends PsiElementListCellRenderer<?>> renderer,
                                                                    boolean empty) {
        if (myLazy) {
            return createLazyGutterIconRenderer(pointers, renderer, empty);
        }
        return new MyNavigationGutterIconRenderer(this, myAlignment, myIcon, myTooltipText, pointers, renderer, empty);
    }

    @NotNull
    private NavigationGutterIconRenderer createLazyGutterIconRenderer(@NotNull NotNullLazyValue<? extends List<SmartPsiElementPointer<?>>> pointers,
                                                                      @NotNull Computable<? extends PsiElementListCellRenderer<?>> renderer,
                                                                      boolean empty) {
        return new MyNavigationGutterIconRenderer(this, myAlignment, myIcon, myTooltipText, pointers, renderer, empty, true);
    }

    private boolean isEmpty() {
        if (myLazy) {
            return false;
        }
        Collection<? extends T> targets = myTargets.create();
        return ContainerUtil.all(targets, target -> myConverter.fun(target).isEmpty());
    }

    private static class MyNavigationGutterIconRenderer extends NavigationGutterIconRenderer {
        private final Alignment myAlignment;
        private final Icon myIcon;
        private final @Tooltip String myTooltipText;
        private final boolean myEmpty;

        MyNavigationGutterIconRenderer(@NotNull UpdatableNavigationGutterIconBuilder<?> builder,
                                       @NotNull Alignment alignment,
                                       final Icon icon,
                                       @Nullable final @Tooltip String tooltipText,
                                       @NotNull NotNullLazyValue<? extends List<SmartPsiElementPointer<?>>> pointers,
                                       @NotNull Computable<? extends PsiElementListCellRenderer<?>> cellRenderer,
                                       boolean empty) {
            super(builder.myPopupTitle, builder.myEmptyText, cellRenderer, pointers, false);
            myAlignment = alignment;
            myIcon = icon;
            myTooltipText = tooltipText;
            myEmpty = empty;
        }

        MyNavigationGutterIconRenderer(@NotNull UpdatableNavigationGutterIconBuilder<?> builder,
                                       @NotNull Alignment alignment,
                                       Icon icon,
                                       @Nullable @Tooltip String tooltipText,
                                       @NotNull NotNullLazyValue<? extends List<SmartPsiElementPointer<?>>> pointers,
                                       @NotNull Computable<? extends PsiElementListCellRenderer<?>> cellRenderer,
                                       boolean empty,
                                       boolean computeTargetsInBackground) {
            super(builder.myPopupTitle, builder.myEmptyText, cellRenderer, pointers, computeTargetsInBackground);
            myAlignment = alignment;
            myIcon = icon;
            myTooltipText = tooltipText;
            myEmpty = empty;
        }

        @Override
        public boolean isNavigateAction() {
            return !myEmpty;
        }

        @Override
        @NotNull
        public Icon getIcon() {
            return myIcon;
        }

        @Override
        @Nullable
        public String getTooltipText() {
            return myTooltipText;
        }

        @NotNull
        @Override
        public Alignment getAlignment() {
            return myAlignment;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) return true;
            if (!super.equals(o)) return false;

            final MyNavigationGutterIconRenderer that = (MyNavigationGutterIconRenderer) o;

            if (myAlignment != that.myAlignment) return false;
            if (!Objects.equals(myIcon, that.myIcon)) return false;
            return Objects.equals(myTooltipText, that.myTooltipText);
        }

        @Override
        public int hashCode() {
            int result = super.hashCode();
            result = 31 * result + (myAlignment != null ? myAlignment.hashCode() : 0);
            result = 31 * result + (myIcon != null ? myIcon.hashCode() : 0);
            result = 31 * result + (myTooltipText != null ? myTooltipText.hashCode() : 0);
            return result;
        }
    }
}