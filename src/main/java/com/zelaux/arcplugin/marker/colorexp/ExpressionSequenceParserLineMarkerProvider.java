package com.zelaux.arcplugin.marker.colorexp;

import com.intellij.codeInsight.CodeInsightBundle;
import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProviderDescriptor;
import com.intellij.codeInsight.daemon.MergeableLineMarkerInfo;
import com.intellij.codeInsight.daemon.NavigateAction;
import com.intellij.icons.AllIcons;
import com.intellij.ide.IdeBundle;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiEditorUtil;
import com.intellij.util.Function;
import com.intellij.util.FunctionUtil;
import com.zelaux.arcplugin.expressions.render.ExpressionSequenceRenderer;
import com.zelaux.arcplugin.expressions.resolve.ExpressionSequence;
import com.zelaux.arcplugin.marker.ExpressionSequenceProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.Collection;
import java.util.List;

@SuppressWarnings("UseJBColor")
public class ExpressionSequenceParserLineMarkerProvider extends LineMarkerProviderDescriptor {
    public static final ExpressionSequenceParserLineMarkerProvider INSTANCE = new ExpressionSequenceParserLineMarkerProvider();

    public ExpressionSequenceParserLineMarkerProvider() {
    }

    @Override
    public LineMarkerInfo<?> getLineMarkerInfo(@NotNull PsiElement element) {
        return null;
    }

    @Override
    public void collectSlowLineMarkers(@NotNull List<? extends PsiElement> elements, @NotNull Collection<? super LineMarkerInfo<?>> result) {
        for (PsiElement element : elements) {
            ExpressionSequenceProvider.EP_NAME.computeSafeIfAny(provider -> {
                @Nullable ExpressionSequence<?> colorExpression = provider.expressionParserSequenceFrom(element);
                if (colorExpression == null) {
                    return null;
                }

                MyInfo info = new MyInfo(new ExpressionParserSequenceRef(element, provider, colorExpression));
                NavigateAction.setNavigateAction(info, IdeBundle.message("dialog.title.choose.color"), null, AllIcons.Actions.Colors);
                result.add(info);
                return info;
            });
        }
    }

    @Override
    public String getName() {
        return CodeInsightBundle.message("gutter.color.preview");
    }

    @NotNull
    @Override
    public Icon getIcon() {
        return AllIcons.Gutter.Colors;
    }

    private static class ExpressionParserSequenceRef {
        private ExpressionSequenceProvider provider;
        private PsiElement element;
        private ExpressionSequence<?> sequence;
        private ExpressionSequenceRenderer<?> renderer;


        public ExpressionParserSequenceRef(PsiElement element, ExpressionSequenceProvider provider, ExpressionSequence<?> sequence) {
            this.element = element;
            this.provider = provider;
            setSequence(sequence);
        }

        private void setSequence(ExpressionSequence<?> sequence) {
            this.sequence = sequence;
            this.renderer = sequence.createRenderer();
        }


        public void update(PsiElement element) {
            ExpressionSequence<?> expression = provider.expressionParserSequenceFrom(element);
            if (expression == null) throw new NullPointerException();
            this.element = element;
            setSequence(expression);
        }
    }

    private static class MyInfo extends MergeableLineMarkerInfo<PsiElement> {
        private final ExpressionParserSequenceRef ref;
        private final Color myColor;

        MyInfo(final ExpressionParserSequenceRef ref) {
            super(ref.element,
//            element.getTextRange(),
                    ref.sequence.getTextRange(), ref.renderer.getIcon(),
//            JBUIScale.scaleIcon(new ColorIcon(12, ref.get().getResultColor())),
                    FunctionUtil.<Object, String>nullConstant(), (e, elt) -> {

//                if(!elt.isWritable() || !expressionParser.isWritable()) return;

                        if (!ref.element.isValid()) {
                            ref.update(elt);
//                    myColor=ref.get().getResultColor();
                        }
                        final Editor editor = PsiEditorUtil.findEditor(ref.element);
                        assert editor != null;
                        ref.renderer.showPopup(ref.element.getProject(), editor, ref.element.isWritable());

//                expressionParser.showColorPicker(element, e, elt, editor);
                    }, GutterIconRenderer.Alignment.LEFT, () -> "Color Preview");
            this.ref = ref;
            myColor = ref.renderer.getIconColor();
//            this.highlighter
//            HighlighterLayer.ADDITIONAL_SYNTAX
        }

        @Override
        public boolean canMergeWith(@NotNull MergeableLineMarkerInfo<?> info) {
            return info instanceof MyInfo && ((MyInfo) info).ref.sequence.getClass() == ref.sequence.getClass();
        }

        @SuppressWarnings({"unchecked", "rawtypes"})
        @Override
        public Icon getCommonIcon(@NotNull java.util.List<? extends MergeableLineMarkerInfo<?>> infos) {
            ExpressionSequenceRenderer.MyTuple[] array = infos.stream().map(_info -> {
                MyInfo info = (MyInfo) _info;
                return new ExpressionSequenceRenderer.MyTuple(info.ref.sequence, info.ref.renderer, info.myColor);
            }).toArray(ExpressionSequenceRenderer.MyTuple[]::new);
            return ((MyInfo) infos.get(0)).ref.renderer.mergeIcons(array);
        }

        @NotNull
        @Override
        public Function<? super PsiElement, String> getCommonTooltip(@NotNull List<? extends MergeableLineMarkerInfo<?>> infos) {
            return FunctionUtil.nullConstant();
        }
    }


}

