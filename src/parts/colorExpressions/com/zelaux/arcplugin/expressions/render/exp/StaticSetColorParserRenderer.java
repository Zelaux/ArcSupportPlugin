package com.zelaux.arcplugin.expressions.render.exp;

import arc.struct.Seq;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.util.Ref;
import com.intellij.psi.PsiClass;
import com.intellij.ui.colorpicker.LightCalloutPopup;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.scale.JBUIScale;
import com.intellij.util.ui.ColorIcon;
import com.intellij.util.ui.FormBuilder;
import com.zelaux.arcplugin.colorExpression.ColorExpressionResolver;
import com.zelaux.arcplugin.expressions.render.ArcColorExpressionRenderSettings;
import com.zelaux.arcplugin.expressions.render.ArcColorExpressionRenderer;
import com.zelaux.arcplugin.expressions.resolve.ArcImportableColorExpressionRenderer;
import com.zelaux.arcplugin.expressions.resolve.methods.StaticSetColorExpression;
import com.zelaux.arcplugin.ui.JPanelBuilder;
import com.zelaux.arcplugin.utils.CustomUastTreeUtil;
import com.zelaux.arcplugin.utils.LazyValue;
import com.zelaux.arcplugin.utils.resolve.FieldData;
import com.zelaux.arcplugin.utils.resolve.StaticFieldResolver;
import kotlin.collections.ArraysKt;
import org.jetbrains.uast.*;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

public class StaticSetColorParserRenderer extends ArcColorExpressionRenderer implements ArcImportableColorExpressionRenderer {
    public final StaticSetColorExpression self;

    public StaticSetColorParserRenderer(StaticSetColorExpression self) {
        this.self = self;

    }

    @Override
    public boolean canBeImport() {
        if (fields.get().length <= 1) return false;
        UExpression targetExpression = self.getTargetExpression();
        if (!(targetExpression instanceof UResolvable)) return false;
        UElement element = UastContextKt.toUElement(((UResolvable) targetExpression).resolve());
        return element instanceof UField;
    }

    private FieldWithColor[] calculateFields() {
        arc.graphics.Color color = self.myColor.get();
        if (color == null) return FieldWithColor.EMPTY_ARRAY;
        UField currentField = CustomUastTreeUtil.resolveRecursiveField(self.getTargetExpression());
        if (currentField == null) return FieldWithColor.EMPTY_ARRAY;
        Seq<FieldWithColor> seq = new Seq<>();
        List<FieldData> fields = StaticFieldResolver.getFields((PsiClass) CustomUastTreeUtil.getContainingClass(currentField).getSourcePsi())
                .map(Map.Entry::getValue)
                .toList()
                ;
        if (fields.isEmpty()) return FieldWithColor.EMPTY_ARRAY;
        for (FieldData field : fields) {
            Color resolved = ColorExpressionResolver.resolveColor(field.uastInitializer());
            if (resolved == null) continue;
            seq.add(new FieldWithColor(field.uastField(), resolved));
        }
        return seq.toArray(FieldWithColor.class);
    }

    @Override
    public JPanelBuilder getTabComponent(Project project, Ref<LightCalloutPopup> popupRef, Ref<ArcColorExpressionRenderSettings> param) {
        FieldWithColor[] fields = this.fields.get();
        if (fields.length > 1 && param.get().isWritable()) {
            UExpression expression = self.getTargetExpression();
            UElement __resolved = CustomUastTreeUtil.resolveElement(expression);
            if (__resolved instanceof UField) {
                UField currentField = (UField) __resolved;
                return new JPanelBuilder()
                        .addComponent(new ComboBox<>(fields) {{
                            setMinimumAndPreferredWidth(param.get().calculateSliderSize().width);
                            setSelectedIndex(ArraysKt.indexOfFirst(fields, it -> Objects.equals(it.field.getName(), currentField.getName())));
                            setRenderer(new PsiFieldWithColorRenderer());
                            addItemListener(event -> {
                                if (event.getStateChange() != 1) return;
                                FieldWithColor item = (FieldWithColor) event.getItem();
                                String newText = CustomUastTreeUtil.getFullName(item.field);
                                self.replaceTarget(newText);
                                param.get().fireUpdate(self);
                            });
                        }});
            }
        }
        return monoColorJPanelBuilder(param, awtColorAt(self));
    }

    private static class FieldWithColor {
        public static final FieldWithColor[] EMPTY_ARRAY = new FieldWithColor[0];

        public UField field;
        public Color color;

        public FieldWithColor(UField field, Color color) {
            this.field = field;
            this.color = color;
        }
    }

    private static class PsiFieldWithColorRenderer implements ListCellRenderer<FieldWithColor> {

        @Override
        public Component getListCellRendererComponent(JList<? extends FieldWithColor> list, FieldWithColor value, int index, boolean isSelected, boolean cellHasFocus) {
            return FormBuilder.createFormBuilder().addComponent(new JBLabel(String.format("%s", CustomUastTreeUtil.getFullName(value.field)), JBUIScale.scaleIcon(new ColorIcon(12, value.color)), SwingConstants.LEFT)).getPanel();
        }
    }

    public final LazyValue<FieldWithColor[]> fields = new LazyValue<>(this::calculateFields) {
        @Override
        public void reset() {
            super.reset();
            fieldsAmount.reset();
        }
    };


    public final LazyValue<Integer> fieldsAmount = LazyValue.create(() -> fields.get().length);


}
