package com.zelaux.arcplugin.expressions.render;

import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Ref;
import com.intellij.ui.colorpicker.ColorPickerBuilderKt;
import com.intellij.ui.colorpicker.LightCalloutPopup;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.scale.JBUIScale;
import com.intellij.ui.tabs.TabInfo;
import com.intellij.ui.tabs.TabsListener;
import com.intellij.util.ui.ColorIcon;
import com.intellij.util.ui.ColorsIcon;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.JBUI;
import com.zelaux.arcplugin.actions.ActionUtils;
import com.zelaux.arcplugin.actions.CustomEntryPointActionGroups;
import com.zelaux.arcplugin.expressions.resolve.ArcColorExpression;
import com.zelaux.arcplugin.expressions.resolve.ArcColorExpressionSequence;
import com.zelaux.arcplugin.expressions.resolve.ExpressionSequence;
import com.zelaux.arcplugin.ui.JPanelBuilder;
import com.zelaux.arcplugin.ui.colorparser.ColorExpressionParserTabs;
import com.zelaux.arcplugin.ui.components.ColorComponent;
import com.zelaux.arcplugin.ui.picker.popup.SimplePopup;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class ArcColorExpressionSequenceRenderer implements ExpressionSequenceRenderer<ArcColorExpressionSequenceRenderer> {
    public final ArcColorExpressionSequence self;

    public ArcColorExpressionSequenceRenderer(ArcColorExpressionSequence self) {
        this.self = self;
    }

    private static JPanelBuilder getJPanelBuilder(Project project, Ref<LightCalloutPopup> ref, Ref<ArcColorExpressionRenderSettings> param) {
        ArcColorExpressionRenderSettings settings = param.get();
        List<ArcColorExpression> list = settings.sequence.list();
        if (list.size() == 1) {
            return list.get(0).createRenderer().getTabComponent(project, ref, param);
        }
        if (settings.isListView()) {
            return listBuilder(project, ref, param);
        }
        return tabsBuilder(project, ref, param);
    }

    private static JPanelBuilder listBuilder(Project project,
                                             Ref<LightCalloutPopup> popupRef,
                                             Ref<ArcColorExpressionRenderSettings> param) {
        return new JPanelBuilder() {{
            JBScrollPane pane = new JBScrollPane(
                    new JPanelBuilder() {{
                        List<ArcColorExpression> list = param.get().sequence.list();
                        for (int i = 0; i < list.size(); i++) {
                            ArcColorExpression expr = list.get(i);
                            JPanel panel = expr.createRenderer()
                                    .getTabComponent(project, popupRef, param)
                                    .buildJPanel();
                            panel.setMinimumSize(panel.getPreferredSize());

                            addComponent(FormBuilder.createFormBuilder()
                                    .addLabeledComponent(expr.calculateTabTitle() + ": ", new JPanel())
                                    .getPanel()
                            );
                            addComponent(panel);
                            if (i < list.size()) {
                                addSeparator();
                            }
                        }
                    }}.buildJPanel()
            );
            pane.setMaximumSize(maximumSizeForRootComponent());
            param.get().registerListener(null, pane::repaint);

            ActionToolbar actionToolbar = ActionUtils.createTabsToolbar(CustomEntryPointActionGroups.getColorExprTabsEntryPoint(), pane);

            addComponent(actionToolbar.getComponent());
            addComponent(pane);
            addColorPreview(this, param);
        }};
    }

    private static JPanelBuilder tabsBuilder(Project project,
                                             Ref<LightCalloutPopup> popupRef,
                                             Ref<ArcColorExpressionRenderSettings> param) {
        return new JPanelBuilder() {{
            List<ArcColorExpression> list = param.get().sequence().list();
            ColorExpressionParserTabs tabs = new ColorExpressionParserTabs(project, this::hashCode);
            ArcColorExpressionRenderer[] renderers = new ArcColorExpressionRenderer[list.size()];
            for (int i = 0; i < list.size(); i++) {
                ArcColorExpression expr = list.get(i);
                JPanel panel = (renderers[i] = expr.createRenderer())
                        .getTabComponent(project, popupRef, param)
                        .buildJPanel();
                tabs.addTab(new TabInfo(panel)
                                .setObject(i)
                        .setText(expr.calculateTabTitle())
                        .setPreferredFocusableComponent(panel)
                );
            }
            tabs.addListener(new TabsListener() {
                @Override
                public void beforeSelectionChanged(TabInfo oldSelection, TabInfo newSelection) {
                    if (newSelection == null) return;
                    int id = (int) newSelection.getObject();
                    JPanel panel = renderers[id]
                            .getTabComponent(project, popupRef, param)
                            .buildJPanel();
                    newSelection
                            .setComponent(panel)
                            .setPreferredFocusableComponent(panel);
                }
            });
            tabs.setMaximumSize(maximumSizeForRootComponent());
            addComponent(tabs);
            addColorPreview(this, param);

        }};
    }

    private static Dimension maximumSizeForRootComponent() {
        return JBUI.size(ColorPickerBuilderKt.PICKER_PREFERRED_WIDTH + 10, (int) (ColorPickerBuilderKt.PICKER_PREFERRED_WIDTH * 1.5f));
    }

    private static void addColorPreview(JPanelBuilder builder, Ref<ArcColorExpressionRenderSettings> param) {
        Dimension size = new Dimension(50, 50);
        JPanel panel = FormBuilder.createFormBuilder()
                .addLabeledComponent(new JBLabel("Color preview: ") {{
                    setVerticalAlignment(SwingConstants.TOP);
                }}, new ColorComponent(param.get().sequence.calculateResultAwtColor(), size, size) {{
                    param.get().registerListener(null, () -> {
                        setColor(param.get().sequence.calculateResultAwtColor());
                    });
                }}).getPanel();
        builder.addComponent(panel);
    }

    @Override
    public void showPopup(Project project, Editor editor, boolean writable) {
        SimplePopup.lambdaPopup.show(project, editor, it ->
                getJPanelBuilder(project,
                        it,
                        Ref.create(new ArcColorExpressionRenderSettings(self, writable))));
    }

    @Override
    public Icon getIcon() {
        return JBUIScale.scaleIcon(new ColorIcon(12, getIconColor()));
    }

    @Override
    public Color getIconColor() {
        return self.calculateResultAwtColor();
    }


    @Override
    public Icon mergeIcons(MyTuple<ExpressionSequence<?>, ArcColorExpressionSequenceRenderer, Color>[] array) {
        Color[] colors = new Color[array.length];
        for (int i = 0; i < array.length; i++) {
            colors[i] = array[i].c;
        }
        return JBUIScale.scaleIcon(new ColorsIcon(12, colors));
    }
}
