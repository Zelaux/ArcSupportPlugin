package com.zelaux.arcplugin.expressions.render.exp;

import arc.util.Tmp;
import com.zelaux.arcplugin.expressions.resolve.methods.SetHsvColorExpression;
import com.zelaux.arcplugin.utils.ColorUtils;
import com.zelaux.arcplugin.utils.UastExpressionUtils;

import java.awt.*;

public class SetHsvColorExpressionRenderer extends SetFloatArrayColorRenderer {
    private static final float[] buildinMax = {360f, 1f, 1f};
    public final SetHsvColorExpression self;

    public SetHsvColorExpressionRenderer(SetHsvColorExpression self) {
        super(self);
        this.self = self;
    }

    @Override
    protected void handleNewColor(Color color) {
        float[] hsv = ColorUtils.toArc(color, Tmp.c1).toHsv(new float[3]);
        for (int i = 0; i < 3; i++) {
            self.replaceParamExpression(i, hsv[i] / buildinMax[i] * self.maxValues[i], UastExpressionUtils::replaceFloat);
        }
        if (self.hasAlpha) {
            self.replaceParamExpression(3, color.getAlpha() / 255f, UastExpressionUtils::replaceFloat);
        }
    }
}
