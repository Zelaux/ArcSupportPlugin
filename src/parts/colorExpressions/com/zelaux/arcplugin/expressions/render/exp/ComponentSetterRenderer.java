package com.zelaux.arcplugin.expressions.render.exp;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.Ref;
import com.intellij.ui.colorpicker.LightCalloutPopup;
import com.intellij.ui.components.JBLabel;
import com.zelaux.arcplugin.expressions.render.ArcColorExpressionRenderSettings;
import com.zelaux.arcplugin.expressions.render.ArcColorExpressionRenderer;
import com.zelaux.arcplugin.expressions.render.RenderUtils;
import com.zelaux.arcplugin.expressions.resolve.ArcColorExpressionSequence;
import com.zelaux.arcplugin.expressions.resolve.methods.SetComponentExpression;
import com.zelaux.arcplugin.ui.JPanelBuilder;
import com.zelaux.arcplugin.ui.components.GradientComponent;
import com.zelaux.arcplugin.utils.UastExpressionUtils;

import java.awt.*;

public class ComponentSetterRenderer extends ArcColorExpressionRenderer {
    public final SetComponentExpression self;

    public ComponentSetterRenderer(SetComponentExpression self) {
        this.self = self;
    }

    @Override
    public JPanelBuilder getTabComponent(Project project,
                                         Ref<LightCalloutPopup> popupRef,
                                         Ref<ArcColorExpressionRenderSettings> param) {
        Pair<Color, Color> colorPair = sideColors(param.get().sequence());
        JBLabel label = new JBLabel("Has no effect on result");
        JPanelBuilder builder = new JPanelBuilder();
        if (colorPair.first == colorPair.second) {
            return builder.addComponent(label);
        }

        builder.addComponent(new GradientComponent(colorPair.first, colorPair.second, true, param.get().isWritable()) {{
            ArcColorExpressionRenderSettings settings = param.get();
            preferredSize = settings.calculateSliderSize();
            minimumSize = settings.calculateSliderSize();
            setSize(settings.calculateSliderSize());

            GradientComponent gradientComponent = this;
            settings.registerListener(self, () -> {
                Pair<Color, Color> newSideColors = sideColors(param.get().sequence());
//                this.setProgressColorValue(currentValue()!!)
                setColorA(newSideColors.first);
                setColorB(newSideColors.second);

                boolean shouldLabel = newSideColors.first == newSideColors.second;
                boolean hasLabel = label.getParent() != null;
                if (shouldLabel == hasLabel) return;
                if (shouldLabel) {
                    RenderUtils.replaceComponent(gradientComponent, label);
                } else {
                    RenderUtils.replaceComponent(label, gradientComponent);
                }
            });
            Float currentValue = self.currentValue();
            boolean hasValue = currentValue != null;
            if (hasValue) {
                this.setProgressColorValue(currentValue);
                boolean updating = false;
            /*val key = AfterChangeAppliedKey {
                UastExpressionUtils.getObject(expression.valueArguments[offset])
            }*/
                addProgressListener(progress -> {
                    writeColorAction(project, () -> {
                        //TODO system like in color picker
                        self.replaceParamExpression(0, progress, UastExpressionUtils::replaceFloat);
//                    element = sourcePsi.toUElement()!!
                        param.get().fireUpdate(self);
                    });
                });
            }
        }});
        builder.withFocus();
        return builder;
    }

    private Pair<java.awt.Color, java.awt.Color> sideColors(ArcColorExpressionSequence sequence) {
        self.setTmpValue(0, 0f);
        Color firstColor = sequence.calculateResultAwtColor();
        self.setTmpValue(0, 1f);
        Color secondColor = sequence.calculateResultAwtColor();
        self.resetTmpValues();
        return new Pair<>(firstColor, secondColor);
    }
}
