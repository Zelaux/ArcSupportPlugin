package com.zelaux.arcplugin.expressions.render.exp;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Ref;
import com.intellij.ui.colorpicker.LightCalloutPopup;
import com.intellij.ui.picker.ColorListener;
import com.zelaux.arcplugin.expressions.render.ArcColorExpressionRenderSettings;
import com.zelaux.arcplugin.expressions.render.ArcColorExpressionRenderer;
import com.zelaux.arcplugin.expressions.resolve.methods.ValueOfExpression;
import com.zelaux.arcplugin.ui.JPanelBuilder;
import com.zelaux.arcplugin.ui.picker.popup.DefaultColorPickerPopup;
import com.zelaux.arcplugin.utils.ColorUtils;
import com.zelaux.arcplugin.utils.UastExpressionUtils;

import java.awt.*;

public class ValueOfParserRenderer extends ArcColorExpressionRenderer {
    public final ValueOfExpression self;

    public ValueOfParserRenderer(ValueOfExpression self) {
        super();
        this.self = self;
    }

    @Override
    public JPanelBuilder getTabComponent(Project project,
                                         Ref<LightCalloutPopup> popupRef,
                                         Ref<ArcColorExpressionRenderSettings> param) {
        Color color = awtColorAt(self);
        if(param.get().isWritable()){
            return DefaultColorPickerPopup.instance(color,true).builder(project, (ColorListener) (color1, source) -> {
                writeColorAction(project,()->{
                    String newValue = ColorUtils.toArc(color1).toString();
                    self.replaceParamExpression(0,newValue, UastExpressionUtils::replaceString);
                    param.get().fireUpdate(self);
                });
            },popupRef);
        }
        return monoColorJPanelBuilder(param,color);
    }

}
