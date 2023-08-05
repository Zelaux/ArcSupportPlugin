package com.zelaux.arcplugin.expressions.render.exp;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Ref;
import com.intellij.ui.colorpicker.HueSliderComponent;
import com.intellij.ui.colorpicker.LightCalloutPopup;
import com.intellij.ui.colorpicker.SliderComponent;
import com.intellij.ui.components.JBLabel;
import com.zelaux.arcplugin.expressions.render.ArcColorExpressionRenderSettings;
import com.zelaux.arcplugin.expressions.render.RenderUtils;
import com.zelaux.arcplugin.expressions.resolve.methods.SetHsvComponentExpression;
import com.zelaux.arcplugin.ui.JPanelBuilder;
import com.zelaux.arcplugin.utils.UastExpressionUtils;
import kotlin.Unit;

import java.awt.*;

public class HueComponentSetParserRenderer extends HsvComponentSetParserRenderer {


    public HueComponentSetParserRenderer(SetHsvComponentExpression self) {
        super(self);
    }

    @Override
    public JPanelBuilder getTabComponent(Project project, Ref<LightCalloutPopup> popupRef, Ref<ArcColorExpressionRenderSettings> param) {
        Result result = calculateColor(param);
//        JBLabel label = new JBLabel("Pointless operation");
        JPanelBuilder builder = new JPanelBuilder();
        SliderComponent<Integer> gradientComponent = new HueSliderComponent();
        Dimension sliderSize = param.get().calculateSliderSize();
        gradientComponent.setPreferredSize(sliderSize);
        gradientComponent.setMinimumSize(sliderSize);
        gradientComponent.setSize(sliderSize);
        boolean[] programatic = {false};
        param.get().registerListener(self, () -> {
            Result newResult = calculateColor(param);
//            boolean wasLabel = label.getParent() != null;
//            boolean needLabel = newResult.firstColor == newResult.secondColor;
            programatic[0] = true;
            Float colorValue = self.currentValue();
            if (colorValue != null) gradientComponent.setValue(colorValue.intValue());
            gradientComponent.setEnabled(param.get().isWritable() && colorValue != null);
//            gradientComponent.setDrawKnob(param.get().isWritable() && colorValue != null);
            programatic[0] = false;
//            if (wasLabel == needLabel) return;
//            if (needLabel) {
//                RenderUtils.replaceComponent(gradientComponent, label);
//            } else {
//                RenderUtils.replaceComponent(label, gradientComponent);
//            }
        });
        {
            Float currentValue = self.currentValue();
            if (currentValue != null) gradientComponent.setValue(currentValue.intValue());
            else {
                gradientComponent.setEnabled(false);
            }
        }
        gradientComponent.addListener(progress -> {
            if (programatic[0] || self.currentValue() == null) return Unit.INSTANCE;
            float newValue = progress;
            self.replaceParamExpression(0, newValue, UastExpressionUtils::replaceFloat);
            param.get().fireUpdate(self);
            return Unit.INSTANCE;
        });

//        if (result.firstColor.equals(result.secondColor)) {
//            builder.addComponent(label);
//        } else {
//        }
        builder.addComponent(gradientComponent);
        return builder;
    }
}
