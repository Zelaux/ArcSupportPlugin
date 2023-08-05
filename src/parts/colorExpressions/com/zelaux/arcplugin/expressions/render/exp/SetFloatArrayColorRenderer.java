package com.zelaux.arcplugin.expressions.render.exp;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Ref;
import com.intellij.ui.colorpicker.LightCalloutPopup;
import com.zelaux.arcplugin.expressions.render.ArcColorExpressionRenderSettings;
import com.zelaux.arcplugin.expressions.render.ArcColorExpressionRenderer;
import com.zelaux.arcplugin.expressions.resolve.methods.SetFloatsExpression;
import com.zelaux.arcplugin.ui.JPanelBuilder;
import com.zelaux.arcplugin.ui.picker.popup.DefaultColorPickerPopup;
import com.zelaux.arcplugin.utils.UastExpressionUtils;

import java.awt.*;

public class SetFloatArrayColorRenderer extends ArcColorExpressionRenderer {
    public final SetFloatsExpression self;

    public SetFloatArrayColorRenderer(SetFloatsExpression self) {
        this.self = self;
    }

    @Override
    public JPanelBuilder getTabComponent(Project project, Ref<LightCalloutPopup> popupRef, Ref<ArcColorExpressionRenderSettings> param) {
        Color awtColor = awtColorAt(self);
        if (param.get().isWritable()) {
            return DefaultColorPickerPopup
                    .instance(awtColor, true)
                    .builder(project, (color, p0) -> {
                        handleNewColor(color);
                        param.get().fireUpdate(self);
                    }, popupRef);
        }

        return monoColorJPanelBuilder(param, awtColor);
    }

    protected void handleNewColor(Color color) {
        self.replaceParamExpression(0, color.getRed() / 255f, UastExpressionUtils::replaceFloat);
        self.replaceParamExpression(1, color.getGreen() / 255f, UastExpressionUtils::replaceFloat);
        self.replaceParamExpression(2, color.getBlue() / 255f, UastExpressionUtils::replaceFloat);
        if (self.hasAlpha)
            self.replaceParamExpression(3, color.getAlpha() / 255f, UastExpressionUtils::replaceFloat);
    }
}
