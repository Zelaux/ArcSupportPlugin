package com.zelaux.arcplugin.expressions.render.exp;

import arc.graphics.Color;
import arc.util.Tmp;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Ref;
import com.intellij.ui.colorpicker.LightCalloutPopup;
import com.intellij.ui.components.JBLabel;
import com.zelaux.arcplugin.expressions.render.ArcColorExpressionRenderSettings;
import com.zelaux.arcplugin.expressions.render.ArcColorExpressionRenderer;
import com.zelaux.arcplugin.expressions.render.RenderUtils;
import com.zelaux.arcplugin.expressions.resolve.methods.SetHsvComponentExpression;
import com.zelaux.arcplugin.ui.JPanelBuilder;
import com.zelaux.arcplugin.ui.components.GradientComponent;
import com.zelaux.arcplugin.utils.ColorUtils;
import com.zelaux.arcplugin.utils.UastExpressionUtils;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

public class HsvComponentSetParserRenderer extends ArcColorExpressionRenderer {
    public final SetHsvComponentExpression self;

    public HsvComponentSetParserRenderer(SetHsvComponentExpression self) {
        super();
        this.self = self;
    }

    @Override
    public JPanelBuilder getTabComponent(Project project, Ref<LightCalloutPopup> popupRef, Ref<ArcColorExpressionRenderSettings> param) {
        Result result = calculateColor(param);
        JBLabel label = new JBLabel("Pointless operation");
        JPanelBuilder builder = new JPanelBuilder();

        GradientComponent gradientComponent = new GradientComponent(result.firstColor, result.secondColor, true, param.get().isWritable) {{
            Dimension sliderSize = param.get().calculateSliderSize();
            setPreferredSize(sliderSize);
            setMinimumSize(sliderSize);
            setSize(sliderSize);
        }};
        boolean[] programatic = {false};
        param.get().registerListener(self, () -> {
            Result newResult = calculateColor(param);
            gradientComponent.setColorA(newResult.firstColor);
            gradientComponent.setColorB(newResult.secondColor);
            boolean wasLabel = label.getParent() != null;
            boolean needLabel = newResult.firstColor == newResult.secondColor;
            programatic[0] = true;
            Float colorValue = self.currentValue();
            if (colorValue != null) gradientComponent.setProgressColorValue(colorValue);
            gradientComponent.setDrawKnob(param.get().isWritable() && colorValue != null);
            programatic[0] = false;
            if (wasLabel == needLabel) return;
            if (needLabel) {
                RenderUtils.replaceComponent(gradientComponent, label);
            } else {
                RenderUtils.replaceComponent(label, gradientComponent);
            }
        });
        {
            Float currentValue = self.currentValue();
            if (currentValue != null) gradientComponent.setProgressColorValue(currentValue);
            else {
                gradientComponent.setDrawKnob(false);
            }
        }
        gradientComponent.addProgressListener(progress -> {
            if (programatic[0] || self.currentValue()==null) return;
            float newValue = progress * self.component.maxValue;
            self.replaceParamExpression(0, newValue, UastExpressionUtils::replaceFloat);
            param.get().fireUpdate(self);
        });

        if (result.firstColor.equals(result.secondColor)) {
            builder.addComponent(label);
        } else {
            builder.addComponent(gradientComponent);
        }
        return builder;
    }


    @SuppressWarnings("DuplicatedCode")
    @NotNull
    protected Result calculateColor(Ref<ArcColorExpressionRenderSettings> param) {
        Color tmpC = ColorUtils.obtainColor();
        Color tmpC2 = ColorUtils.obtainColor();

        param.get().sequence.applyColorUntil(self, tmpC.set(0xff));
        self.component.set(tmpC2.set(tmpC), 0f);
        java.awt.Color firstColor = ColorUtils.toAwt(tmpC2);
        self.component.set(tmpC2.set(tmpC), self.component.maxValue);
        java.awt.Color secondColor = ColorUtils.toAwt(tmpC2);
        Result result = new Result(firstColor, secondColor);

        ColorUtils.freeColors(tmpC,tmpC2);
        return result;
    }


    protected static class Result {
        public final java.awt.Color firstColor;
        public final java.awt.Color secondColor;

        public Result(java.awt.Color firstColor, java.awt.Color secondColor) {
            this.firstColor = firstColor;
            this.secondColor = secondColor;
        }
    }
}
