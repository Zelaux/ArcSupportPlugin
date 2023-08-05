package com.zelaux.arcplugin.expressions.render.exp;

import arc.util.Tmp;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Ref;
import com.intellij.ui.colorpicker.LightCalloutPopup;
import com.zelaux.arcplugin.expressions.render.ArcColorExpressionRenderSettings;
import com.zelaux.arcplugin.expressions.render.ArcColorExpressionRenderer;
import com.zelaux.arcplugin.expressions.resolve.ArcColorExpressionSequence;
import com.zelaux.arcplugin.expressions.resolve.methods.NoParamFunctionExpression;
import com.zelaux.arcplugin.ui.JPanelBuilder;
import com.zelaux.arcplugin.ui.components.ColorComponent;
import com.zelaux.arcplugin.ui.components.IconComponent;
import com.zelaux.arcplugin.utils.ColorUtils;

import java.awt.*;

public class NoParamFuncitonExpressionRenderer extends ArcColorExpressionRenderer {
    public final NoParamFunctionExpression self;

    public NoParamFuncitonExpressionRenderer(NoParamFunctionExpression self) {
        super();
        this.self = self;
    }

    @Override
    public JPanelBuilder getTabComponent(Project project, Ref<LightCalloutPopup> popupRef, Ref<ArcColorExpressionRenderSettings> param) {
        JPanelBuilder builder = new JPanelBuilder(false);
        ArcColorExpressionSequence sequence = param.get().sequence();
        sequence.applyColorUntil(self, Tmp.c1.set(0xff));

        sequence.applyColorUntil(self, Tmp.c1.set(0xff));
        Dimension sliderSize = param.get().calculateSliderSize();
        Dimension preferredSize = new Dimension(sliderSize.width / 3, sliderSize.height);

        ColorComponent originalComp = new ColorComponent(ColorUtils.toAwt(Tmp.c1),
                preferredSize,
                preferredSize
        );
        self.apply(Tmp.c1);
        ColorComponent newComp = new ColorComponent(ColorUtils.toAwt(Tmp.c1),
                preferredSize,
                preferredSize);
        builder.addComponent(originalComp)
                .addComponent(new IconComponent(AllIcons.Diff.ArrowRight))
                .addComponent(newComp);
        param.get().registerListener(self, () -> {
            sequence.applyColorUntil(self, Tmp.c1.set(0xff));
            originalComp.setColor(ColorUtils.toAwt(Tmp.c1));
            self.apply(Tmp.c1);
            newComp.setColor(ColorUtils.toAwt(Tmp.c1));
        });
//        addComponent(IconAllIcons.Diff.Arrow)
        return builder;
    }
}
