package com.zelaux.arcplugin.ui.colorparser;

import com.intellij.openapi.*;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.actionSystem.impl.ActionButton;
import com.intellij.openapi.project.*;
import com.intellij.openapi.util.*;
import com.intellij.ui.*;
import com.intellij.ui.paint.*;
import com.intellij.ui.tabs.*;
import com.intellij.ui.tabs.impl.*;
import com.intellij.util.ui.*;
import com.zelaux.arcplugin.actions.*;
import com.zelaux.arcplugin.parsers.*;
import org.jetbrains.annotations.*;

import javax.swing.*;
import java.awt.*;

public class ColorExpressionParserTabs extends SingleHeightTabs implements ComponentWithMnemonics{


    private boolean active;

    public ColorExpressionParserTabs(Project project, @NotNull Disposable parentDisposable, @NotNull TabInfo... tabs){
        super(project, parentDisposable);

        for(TabInfo tab : tabs){
            addTab(tab);
        }
//        UIUtil.addAwtListener(e -> updateActive(), AWTEvent.FOCUS_EVENT_MASK, parentDisposable);
        setUiDecorator(() -> new UiDecorator.UiDecoration(null, JBUI.CurrentTheme.EditorTabs.tabInsets()));

        remove(myMoreToolbar.getComponent());
        if(myEntryPointToolbar != null){
//            remove(myEntryPointToolbar.getComponent());

        }
        addListener(new TabsListener(){
            @Override
            public void selectionChanged(TabInfo oldSelection, TabInfo newSelection){
                TabsListener.super.selectionChanged(oldSelection, newSelection);
                doLayout();

            }
        });
//        project.getMessageBus().connect(parentDisposable);
    }

    @Override
    public Dimension getPreferredSize(){
//        return super.getPreferredSize();
        TabInfo info = getSelectedInfo();
        return info.getComponent().getPreferredSize();
    }

    @Override
    public int getWidth(){
        return super.getWidth();
    }

    @Override
    protected void addImpl(Component comp, Object constraints, int index){
        super.addImpl(comp, constraints, index);
    }

    @Override
    protected boolean supportsTableLayoutAsSingleRow(){
        return true;
    }

    @Override
    protected void paintChildren(Graphics g){
        if(!isHideTabs()){
            TabLabel label = getSelectedLabel();
            if(label != null){
                int h = label.getHeight();
                Color color = myTabPainter.getTabTheme().getBorderColor();
                g.setColor(color);
                LinePainter2D.paint(((Graphics2D)g), 0, h, getWidth(), h);
            }
        }
        super.paintChildren(g);
        drawBorder(g);
    }

    @Override
    protected DefaultActionGroup getEntryPointActionGroup(){
        return CustomEntryPointActionGroups.getColorExprTabsEntryPoint();
    }

    @NotNull
    @Override
    protected TabLabel createTabLabel(@NotNull TabInfo info){
        return new SingleHeightLabel(this, info){
            @Override
            protected int getPreferredHeight(){
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
            public void paint(Graphics g){
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
    protected TabPainterAdapter createTabPainterAdapter(){
        return new EditorTabPainterAdapter();
    }

    @Override
    protected JBTabsBorder createTabBorder(){
        return new JBEditorTabsBorder(this);
    }

    @NotNull
    @Override
    public ActionCallback select(@NotNull TabInfo info, boolean requestFocus){
        active = true;
        return super.select(info, requestFocus);
    }

    private void updateActive(){
        checkActive();
        SwingUtilities.invokeLater(() -> {
            checkActive();
        });
    }

    private void checkActive(){
        boolean newActive = UIUtil.isFocusAncestor(this);

        if(newActive != active){
            active = newActive;
            revalidateAndRepaint();
        }
    }

    @Override
    protected boolean isActiveTabs(TabInfo info){
        return active;
    }

    @Nullable
    @Override
    public TabInfo getToSelectOnRemoveOf(TabInfo info){
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
    public void remove(int index){
        super.remove(index);
    }

    @Override
    public void revalidateAndRepaint(boolean layoutNow){
        //noinspection ConstantConditions - called from super constructor
//        if (myColorParserSequence != null && myColorParserSequence.getOwner().isInsideChange()) return;
        super.revalidateAndRepaint(layoutNow);
    }
}
