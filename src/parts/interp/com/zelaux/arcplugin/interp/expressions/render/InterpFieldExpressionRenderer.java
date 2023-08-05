package com.zelaux.arcplugin.interp.expressions.render;

import arc.math.Interp;
import arc.struct.Seq;
import arc.util.Reflect;
import arc.util.Structs;
import com.intellij.lang.Language;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.util.Ref;
import com.intellij.psi.PsiElement;
import com.intellij.ui.colorpicker.LightCalloutPopup;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.scale.JBUIScale;
import com.intellij.util.ui.FormBuilder;
import com.zelaux.arcplugin.interp.InterpResolver;
import com.zelaux.arcplugin.interp.expressions.resolve.InterpExpression;
import com.zelaux.arcplugin.interp.expressions.resolve.InterpFieldExpression;
import com.zelaux.arcplugin.ui.JPanelBuilder;
import com.zelaux.arcplugin.ui.components.InterpGraphComponent;
import com.zelaux.arcplugin.utils.CustomUastTreeUtil;
import com.zelaux.arcplugin.utils.LazyValue;
import org.jetbrains.uast.UClass;
import org.jetbrains.uast.UElement;
import org.jetbrains.uast.UExpression;
import org.jetbrains.uast.UField;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;

import static com.zelaux.arcplugin.interp.InterpResolver.isInterpClass;

public class InterpFieldExpressionRenderer extends InterpExpressionRenderer {
    public final InterpFieldExpression self;
    public final LazyValue<InterpField[]> myFields = LazyValue.create(this::computeMyFields);

    public InterpFieldExpressionRenderer(InterpFieldExpression self) {
        super(self);
        this.self = self;
    }

    @Override
    public void getTabComponentInternal(JPanelBuilder builder, Project project, Ref<LightCalloutPopup> popupRef, Ref<InterpExpressionRenderSettings> ref) {
        int width = JBUIScale.scale(300);
        int height = width;

        InterpField[] fields = myFields.get();
        if (ref.get().isWritable() && fields.length > 1) {
            UElement expression = self.castElement();
            boolean isFieldSetter = CustomUastTreeUtil.resolveElement(expression) instanceof UField;
            if (isFieldSetter) {
                UField currentField = CustomUastTreeUtil.resolveRecursiveField(expression);
                assert currentField != null;
                builder.addComponent(new ComboBox<>(fields) {{
                    setMinimumAndPreferredWidth(width);
                    setSelectedIndex(Structs.indexOf(fields, it -> Objects.equals(it.field.getName(), currentField.getName())));
                    setRenderer(new InterpFieldRenderer());
                    addItemListener(event -> {
                        if (event.getStateChange() != 1) return;
                        InterpField item = (InterpField) event.getItem();
                        String newText = CustomUastTreeUtil.getFullName(item.field);
                        self.replaceTarget(newText);
                    });
                }});
            }
        }
    }

    private InterpField[] computeMyFields() {
        if (self.myInterpolation.get() == null) return InterpField.EMPTY_ARRAY;
        UElement __currentField = CustomUastTreeUtil.resolveElement(self.castElement());
        if (!(__currentField instanceof UField)) return InterpField.EMPTY_ARRAY;
        UField currentField = (UField) __currentField;
        if (currentField.getSourcePsi() == null) return InterpField.EMPTY_ARRAY;
        UClass containingClass = CustomUastTreeUtil.getContainingClass(currentField);
        if (containingClass == null) return InterpField.EMPTY_ARRAY;
        Seq<InterpField> seq = new Seq<>();

        Language language;
        {
            PsiElement sourcePsi = containingClass.getSourcePsi();
            language = sourcePsi == null ? Language.ANY : sourcePsi.getLanguage();
        }

        boolean isInterpClass = isInterpClass(containingClass);
        for (UField field : containingClass.getFields()) {
            UExpression initializer = field.getUastInitializer();
            if (initializer == null && !isInterpClass) continue;
            if (isInterpClass) {
                Interp interp = Reflect.get(Interp.class, Objects.requireNonNull(field.getName()));
                if (interp != null) seq.add(new InterpField(field, interp));
            } else {
                InterpExpression resolved = InterpResolver.resolve(initializer);
                if (resolved != null) {
                    Interp interp = resolved.getInterpolation();
                    if (interp != null) {
                        seq.add(new InterpField(field, interp));
                    }
                }
            }
        }
        return seq.toArray(InterpField.class);
    }

    static class InterpFieldRenderer implements ListCellRenderer<InterpField> {
        @Override
        public Component getListCellRendererComponent(JList<? extends InterpField> list, InterpField field, int index, boolean isSelected, boolean cellHasFocus) {
            int iconsize = 64;
            String fullName = CustomUastTreeUtil.getFullName(field.field);
            FormBuilder formBuilder = FormBuilder.createFormBuilder()
                    .addLabeledComponent(
                            new InterpGraphComponent(field.interp, iconsize, iconsize) {{
//                    this.stroke = 1f;
                                fontSize = 5f;
                                stroke = 2f;
//                    setWindowCenter(-0.0, -0.0, 2.1, 2.1);
                                repaint();
                            }},
                            new JBLabel(
                                    fullName == null ? "ERROR" : fullName,
                                    SwingConstants.LEFT
                            )
                    );

            return formBuilder.getPanel();
        }
    }

    static class InterpField {
        public static final InterpField[] EMPTY_ARRAY = {};
        public final UField field;
        public final Interp interp;

        public InterpField(UField psiField, Interp interp) {
            this.field = psiField;
            this.interp = interp;
        }
    }
}
