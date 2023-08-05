package com.zelaux.arcplugin.expressions.render;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Ref;
import com.intellij.ui.colorpicker.LightCalloutPopup;
import com.zelaux.arcplugin.expressions.resolve.ArcColorExpression;
import com.zelaux.arcplugin.expressions.resolve.ArcColorExpressionSequence;
import com.zelaux.arcplugin.expressions.resolve.ArcImportableColorExpressionRenderer;
import com.zelaux.arcplugin.expressions.resolve.Expression;
import com.zelaux.arcplugin.ui.JPanelBuilder;
import com.zelaux.arcplugin.ui.components.ColorComponent;
import com.zelaux.arcplugin.utils.ColorUtils;

import java.awt.*;

public abstract class ArcColorExpressionRenderer implements ExpressionRenderer<ArcColorExpressionSequence, ArcColorExpressionRenderSettings> , ArcImportableColorExpressionRenderer {
    private static final arc.graphics.Color tmpColor1 = new arc.graphics.Color();

    protected <T extends ArcColorExpression & Expression.ExpressionEntryPoint> Color awtColorAt(T expression) {
        expression.apply(tmpColor1);
        return ColorUtils.toAwt(tmpColor1);
    }

    @Override
    public abstract JPanelBuilder getTabComponent(Project project,
                                                  Ref<LightCalloutPopup> popupRef,
                                                  Ref<ArcColorExpressionRenderSettings> param);

    protected JPanelBuilder monoColorJPanelBuilder(Ref<ArcColorExpressionRenderSettings> param, Color color) {
        return new JPanelBuilder()
                .addComponent(new ColorComponent(color) {{
                    setSize(param.get().calculateSliderSize());

                }});
    }

    @Deprecated
    protected void writeColorAction(Project project, Runnable block) {
        block.run();
//        WriteAction.run(() -> {
//            CommandProcessor.getInstance().executeCommand(project, block, JavaBundle.message("change.color.command.text"), null);
//        });
    }
}
