package com.zelaux.arcplugin.interp.expressions.render;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Ref;
import com.intellij.ui.colorpicker.LightCalloutPopup;
import com.intellij.ui.scale.JBUIScale;
import com.zelaux.arcplugin.expressions.render.ExpressionRenderer;
import com.zelaux.arcplugin.expressions.resolve.ExpressionSequence;
import com.zelaux.arcplugin.interp.expressions.resolve.InterpContainer;
import com.zelaux.arcplugin.interp.expressions.resolve.InterpExpression;
import com.zelaux.arcplugin.ui.JPanelBuilder;
import com.zelaux.arcplugin.ui.components.InterpGraphComponent;

import java.awt.*;

public class InterpExpressionRenderer implements ExpressionRenderer<InterpContainer, InterpExpressionRenderSettings> {
    public final InterpExpression self;

    public InterpExpressionRenderer(InterpExpression self) {
        this.self = self;
    }

    @Override
    public final JPanelBuilder getTabComponent(Project project, Ref<LightCalloutPopup> popupRef, Ref<InterpExpressionRenderSettings> param) {
        int width = JBUIScale.scale(300);
        int height = width;

        return new JPanelBuilder(true) {{
            InterpGraphComponent component = new InterpGraphComponent(self.getInterpolation(), width, height) {{
                fontSize = 10f;
                setMinimumSize(new Dimension(width, height));
            }};
            addComponent(component);
            addSeparator();
            getTabComponentInternal(this, project, popupRef, param);
        }};
    }

    protected void getTabComponentInternal(JPanelBuilder builder, Project project, Ref<LightCalloutPopup> popupRef, Ref<InterpExpressionRenderSettings> param) {

    }
}
