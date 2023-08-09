package com.zelaux.arcplugin.ui.colorparser;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.ActionCallback;
import com.intellij.openapi.util.registry.Registry;
import com.intellij.ui.ComponentWithMnemonics;
import com.intellij.ui.ExperimentalUI;
import com.intellij.ui.paint.LinePainter2D;
import com.intellij.ui.tabs.*;
import com.intellij.ui.tabs.impl.*;
import com.intellij.ui.tabs.impl.themes.TabTheme;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;
import com.zelaux.arcplugin.actions.CustomEntryPointActionGroups;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

public class ColorExpressionParserTabs extends SingleHeightTabs implements ComponentWithMnemonics {


    private boolean active;

    public ColorExpressionParserTabs(Project project, @NotNull Disposable parentDisposable, @NotNull TabInfo... tabs) {
        super(project, parentDisposable);

        for (TabInfo tab : tabs) {
            addTab(tab);
        }
//        UIUtil.addAwtListener(e -> updateActive(), AWTEvent.FOCUS_EVENT_MASK, parentDisposable);
        setUiDecorator(() -> new UiDecorator.UiDecoration(null, JBUI.CurrentTheme.EditorTabs.tabInsets()));

        remove(myMoreToolbar.getComponent());
        if (myEntryPointToolbar != null) {
//            remove(myEntryPointToolbar.getComponent());

        }
        addListener(new TabsListener() {
            @Override
            public void selectionChanged(TabInfo oldSelection, TabInfo newSelection) {
                TabsListener.super.selectionChanged(oldSelection, newSelection);
                doLayout();

            }
        });
//        project.getMessageBus().connect(parentDisposable);
    }

    @Override
    public Dimension getPreferredSize() {
//        return super.getPreferredSize();
        TabInfo info = getSelectedInfo();
        return info.getComponent().getPreferredSize();
    }

    @Override
    public int getWidth() {
        return super.getWidth();
    }

    @Override
    protected void addImpl(Component comp, Object constraints, int index) {
        super.addImpl(comp, constraints, index);
    }

    @Override
    protected boolean supportsTableLayoutAsSingleRow() {
        return true;
    }

    @Override
    protected void paintChildren(Graphics g) {
        if (!isHideTabs()) {
            TabLabel label = getSelectedLabel();
            if (label != null) {
                int h = label.getHeight();
                Color color = myTabPainter.getTabTheme().getBorderColor();
                g.setColor(color);
                LinePainter2D.paint(((Graphics2D) g), 0, h, getWidth(), h);
            }
        }
        super.paintChildren(g);
        drawBorder(g);
    }

    @Override
    protected DefaultActionGroup getEntryPointActionGroup() {
        return CustomEntryPointActionGroups.getColorExprTabsEntryPoint();
    }

    @NotNull
    @Override
    protected TabLabel createTabLabel(@NotNull TabInfo info) {
        return new SingleHeightLabel(this, info) {
            @Override
            protected int getPreferredHeight() {
                Insets insets = getInsets();
                Insets layoutInsets = getLayoutInsets();

                insets.top += layoutInsets.top;
                insets.bottom += layoutInsets.bottom;
/*
                if (ExperimentalUI.isNewEditorTabs()) {
                    insets.top -= 7;
                }*/
                return super.getPreferredHeight() - insets.top - insets.bottom;
            }

            @Override
            public void paint(Graphics g) {
                /*if (ExperimentalUI.isNewEditorTabs() && getSelectedInfo() != info && !isHoveredTab(this)) {
                    GraphicsConfig config = GraphicsUtil.paintWithAlpha(g, JBUI.getFloat("EditorTabs.hoverAlpha", 0.75f));
                    super.paint(g);
                    config.restore();
                } else {
                }*/
                super.paint(g);
            }
        };
    }

    @Override
    protected TabPainterAdapter createTabPainterAdapter() {
        return new MyEditorTabPainterAdapter();
    }

    @Override
    protected JBTabsBorder createTabBorder() {
        return new JBEditorTabsBorder(this);
    }

    @NotNull
    @Override
    public ActionCallback select(@NotNull TabInfo info, boolean requestFocus) {
        active = true;
        return super.select(info, requestFocus);
    }

    private void updateActive() {
        checkActive();
        SwingUtilities.invokeLater(() -> {
            checkActive();
        });
    }

    private void checkActive() {
        boolean newActive = UIUtil.isFocusAncestor(this);

        if (newActive != active) {
            active = newActive;
            revalidateAndRepaint();
        }
    }

    @Override
    protected boolean isActiveTabs(TabInfo info) {
        return active;
    }

    @Nullable
    @Override
    public TabInfo getToSelectOnRemoveOf(TabInfo info) {
//        if (myColorParserSequence.isDisposed()) return null;
       /* int index = getIndexOf(info);
        if (index != -1) {
            VirtualFile file = myColorParserSequence.getFileAt(index);
            int indexToSelect = myColorParserSequence.calcIndexToSelect(file, index);
            if (indexToSelect >= 0 && indexToSelect < getTabs().size()) {
                return getTabAt(indexToSelect);
            }
        }*/
        return super.getToSelectOnRemoveOf(info);
    }

    @Override
    public void remove(int index) {
        super.remove(index);
    }

    @Override
    public void revalidateAndRepaint(boolean layoutNow) {
        //noinspection ConstantConditions - called from super constructor
//        if (myColorParserSequence != null && myColorParserSequence.getOwner().isInsideChange()) return;
        super.revalidateAndRepaint(layoutNow);
    }

    static class MyEditorTabPainterAdapter implements TabPainterAdapter {
        private final int magicOffset = 1;
        private final JBEditorTabPainter painter = new JBEditorTabPainter();

        @NotNull
        @Override
        public JBTabPainter getTabPainter() {
            return painter;
        }

        @Override
        public void paintBackground(@NotNull TabLabel label, @NotNull Graphics g, @NotNull JBTabsImpl __tabs) {
            ColorExpressionParserTabs tabs = (ColorExpressionParserTabs) __tabs;
            var info = label.getInfo();
            var isSelected = info == tabs.getSelectedInfo();
            var isHovered = tabs.isHoveredTab(label);

            var rect = new Rectangle(0, 0, label.getWidth(), label.getHeight());

            var g2d = (Graphics2D) g;
            if (isSelected) {
                painter.paintSelectedTab(tabs.getPosition(), g2d, rect,
                        tabs.getBorderThickness(), info.getTabColor(),
                        tabs.isActiveTabs(info), isHovered);
                paintBorders(g2d, label, tabs);
            } else {
                //noinspection UnstableApiUsage
                if (ExperimentalUI.isNewUI() && isHovered) {
                    rect.height -= 1;
                }
                painter.paintTab(tabs.getPosition(), g2d, rect, tabs.getBorderThickness(), info.getTabColor(), tabs.isActiveTabs(info), isHovered);
                paintBorders(g2d, label, tabs);
            }
        }

        private void paintBorders(Graphics2D g, TabLabel label, ColorExpressionParserTabs tabs) {
            var paintStandardBorder = !tabs.isSingleRow()
                    || (!tabs.getPosition().isSide() && Registry.is("ide.new.editor.tabs.vertical.borders"));
            var lastPinned = label.isLastPinned();
            var nextToLastPinned = label.isNextToLastPinned();
            var rect = new Rectangle(0, 0, label.getWidth(), label.getHeight());
            if (paintStandardBorder || lastPinned || nextToLastPinned) {


                var bounds = label.getBounds();
                if (bounds.x > magicOffset && (paintStandardBorder || nextToLastPinned)) {
                    painter.paintLeftGap(tabs.getPosition(), g, rect, tabs.getBorderThickness());
                }

                if (bounds.x + bounds.width < tabs.getWidth() - magicOffset && (paintStandardBorder || lastPinned)) {
                    painter.paintRightGap(tabs.getPosition(), g, rect, tabs.getBorderThickness());
                }
            }

            if (tabs.getPosition().isSide() && lastPinned) {
                var bounds = label.getBounds();
                if (bounds.y + bounds.height < tabs.getHeight() - magicOffset) {
                    painter.paintBottomGap(tabs.getPosition(), g, rect, tabs.getBorderThickness());
                }
            }
            if (tabs.getPosition().isSide() && nextToLastPinned) {
                var bounds = label.getBounds();
                if (bounds.y + bounds.height < tabs.getHeight() - magicOffset) {
                    painter.paintTopGap(tabs.getPosition(), g, rect, tabs.getBorderThickness());
                }
            }
        }

        @NotNull
        @Override
        public TabTheme getTabTheme() {
            return painter.getTabTheme();
        }
    }
}
