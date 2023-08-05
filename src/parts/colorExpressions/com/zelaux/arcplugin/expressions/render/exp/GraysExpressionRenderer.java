package com.zelaux.arcplugin.expressions.render.exp;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Ref;
import com.intellij.ui.colorpicker.LightCalloutPopup;
import com.zelaux.arcplugin.expressions.render.ArcColorExpressionRenderSettings;
import com.zelaux.arcplugin.expressions.render.ArcColorExpressionRenderer;
import com.zelaux.arcplugin.expressions.resolve.methods.GraysExpression;
import com.zelaux.arcplugin.ui.JPanelBuilder;
import com.zelaux.arcplugin.ui.components.GradientComponent;
import com.zelaux.arcplugin.ui.picker.popup.GrayScaleColorPickerPopup;
import com.zelaux.arcplugin.utils.ColorUtils;
import com.zelaux.arcplugin.utils.UastExpressionUtils;

import java.awt.*;

public class GraysExpressionRenderer extends ArcColorExpressionRenderer {
    public final GraysExpression self;

    public GraysExpressionRenderer(GraysExpression self) {
        this.self = self;
    }

    @SuppressWarnings("UseJBColor")
    @Override
    public JPanelBuilder getTabComponent(Project project, Ref<LightCalloutPopup> popupRef, Ref<ArcColorExpressionRenderSettings> param) {
        Float currentValue = self.currentValue();
        Color color = ColorUtils.grays(currentValue);
        if (param.get().isWritable()) {
            return GrayScaleColorPickerPopup
                    .instance(color, false)
                    .builder(project, (newGrays, p0, p1) -> {
                        writeColorAction(project, () -> {
                            self.replaceParamExpression(0,newGrays, UastExpressionUtils::replaceFloat);
                            param.get().fireUpdate(self);
                        });
                    }, popupRef);
        }
        return new JPanelBuilder()
                .addComponent(new GradientComponent(Color.black, Color.white, true, false) {{
                    Dimension sliderSize = param.get().calculateSliderSize();
                    setPreferredSize(sliderSize);
                    setMinimumSize(sliderSize);
                    setSize(sliderSize);
                    setProgressColorValue(currentValue);
                }});
    }


}
