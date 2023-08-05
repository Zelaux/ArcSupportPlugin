package com.zelaux.arcplugin.expressions.render.exp;

import arc.util.Tmp;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Ref;
import com.intellij.ui.colorpicker.LightCalloutPopup;
import com.zelaux.arcplugin.expressions.render.ArcColorExpressionRenderSettings;
import com.zelaux.arcplugin.expressions.render.ArcColorExpressionRenderer;
import com.zelaux.arcplugin.expressions.resolve.ArcColorExpression;
import com.zelaux.arcplugin.expressions.resolve.ArcImportableColorExpressionRenderer;
import com.zelaux.arcplugin.expressions.resolve.methods.LerpExpression;
import com.zelaux.arcplugin.expressions.resolve.methods.StaticSetColorExpression;
import com.zelaux.arcplugin.ui.JPanelBuilder;
import com.zelaux.arcplugin.ui.components.GradientComponent;
import com.zelaux.arcplugin.utils.ColorUtils;
import com.zelaux.arcplugin.utils.UastExpressionUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.uast.*;

import java.awt.*;

public class LerpColorParserRenderer extends ArcColorExpressionRenderer {
    public final LerpExpression self;

    public LerpColorParserRenderer(LerpExpression self) {
        super();
        this.self = self;
    }

    private static void addSetField(Project project,
                                    Ref<LightCalloutPopup> popupRef,
                                    Ref<ArcColorExpressionRenderSettings> param, JPanelBuilder builder, ArcColorExpression other) {
        ArcColorExpressionRenderer ___renderer = other.createRenderer();
        if (!(___renderer instanceof ArcImportableColorExpressionRenderer)) return;
        ArcImportableColorExpressionRenderer renderer = (ArcImportableColorExpressionRenderer) ___renderer;
        if(!renderer.canBeImport())return;
        builder.addComponent(___renderer.getTabComponent(project, popupRef, param).buildJPanel());


    }

    @Override
    public JPanelBuilder getTabComponent(Project project,
                                         Ref<LightCalloutPopup> popupRef,
                                         Ref<ArcColorExpressionRenderSettings> param) {

        Color initColorA = calculateColorA(param);
        Color initColorB = calculateColorB();
        Float progress = self.progress();
        boolean hasProgress = progress != null;
        JPanelBuilder builder = new JPanelBuilder();
        {
            ArcColorExpression innerExp = self.innerColor.get();

            if (innerExp != null) {
                addSetField(project, popupRef, param, builder, innerExp);
            }
        }
        addSetField(project, popupRef, param, builder, self.secondColor.get());
        builder.addComponent(new GradientComponent(initColorA, initColorB, hasProgress, hasProgress && param.get().isWritable()) {{
            setSize(param.get().calculateSliderSize());
            if (hasProgress) setProgressColorValue(progress);
            param.get().registerListener(self, () -> {
                setColorA(calculateColorA(param));
                setColorB(calculateColorB());
            });
            if (hasProgress) {
                addProgressListener(newProgress -> {
                    self.replaceParamExpression(1,newProgress, UastExpressionUtils::replaceFloat);
                    param.get().fireUpdate(self);
                });
            }
        }});
        return builder;
    }

    @NotNull
    private Color calculateColorB() {
        ArcColorExpression secondExp = self.secondColor.get();
        secondExp.apply(Tmp.c1);
        return ColorUtils.toAwt(Tmp.c1);
    }

    @NotNull
    private Color calculateColorA(Ref<ArcColorExpressionRenderSettings> param) {
        param.get().sequence().applyColorUntil(self, Tmp.c1.set(0xff));
        ArcColorExpression innerExp = self.innerColor.get();
        if (innerExp != null) {
            innerExp.apply(Tmp.c1);
        }
        return ColorUtils.toAwt(Tmp.c1);
    }
}
