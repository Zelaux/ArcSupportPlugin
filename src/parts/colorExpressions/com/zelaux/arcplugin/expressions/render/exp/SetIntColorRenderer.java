package com.zelaux.arcplugin.expressions.render.exp;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Ref;
import com.intellij.ui.colorpicker.LightCalloutPopup;
import com.zelaux.arcplugin.expressions.render.ArcColorExpressionRenderSettings;
import com.zelaux.arcplugin.expressions.render.ArcColorExpressionRenderer;
import com.zelaux.arcplugin.expressions.resolve.methods.SetIntExpression;
import com.zelaux.arcplugin.ui.JPanelBuilder;
import com.zelaux.arcplugin.ui.picker.popup.DefaultColorPickerPopup;
import com.zelaux.arcplugin.utils.UastExpressionUtils;

import java.awt.*;

public class SetIntColorRenderer extends ArcColorExpressionRenderer {
    public final SetIntExpression self;

    public SetIntColorRenderer(SetIntExpression self) {
        this.self = self;
    }

    @Override
    public JPanelBuilder getTabComponent(Project project,
                                         Ref<LightCalloutPopup> popupRef,
                                         Ref<ArcColorExpressionRenderSettings> param) {
        Color awtColor = awtColorAt(self);
        if (param.get().isWritable()) {
            return DefaultColorPickerPopup
                    .instance(awtColor, self.hasAplha)
                    .builder(project, (color, p0) -> {
                        writeColorAction(project, () -> {
                            int newValue = self.hasAplha ?
                                    arc.graphics.Color.packRgba(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()) :
                                    arc.graphics.Color.packRgba(color.getRed(), color.getGreen(), color.getBlue(), 255);
                            self.replaceParamExpression(0, newValue, (a, b) -> UastExpressionUtils.replaceInt(a, b, true));
                            param.get().fireUpdate(self);
                        });
                    }, popupRef);
        }

        return monoColorJPanelBuilder(param, awtColor);
    }
}
