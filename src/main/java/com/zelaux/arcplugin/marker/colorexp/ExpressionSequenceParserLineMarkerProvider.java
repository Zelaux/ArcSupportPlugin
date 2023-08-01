package com.zelaux.arcplugin.marker.colorexp;

import com.intellij.codeInsight.*;
import com.intellij.codeInsight.daemon.*;
import com.intellij.icons.*;
import com.intellij.ide.*;
import com.intellij.openapi.editor.*;
import com.intellij.openapi.editor.markup.*;
import com.intellij.psi.*;
import com.intellij.psi.util.*;
import com.intellij.ui.scale.*;
import com.intellij.util.*;
import com.intellij.util.ui.*;
import com.zelaux.arcplugin.marker.*;
import com.zelaux.arcplugin.parsers.*;
import org.jetbrains.annotations.*;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.*;

@SuppressWarnings("UseJBColor")
public class ExpressionSequenceParserLineMarkerProvider extends LineMarkerProviderDescriptor{
    public static final ExpressionSequenceParserLineMarkerProvider INSTANCE = new ExpressionSequenceParserLineMarkerProvider();

    public ExpressionSequenceParserLineMarkerProvider(){
    }

    @Override
    public LineMarkerInfo<?> getLineMarkerInfo(@NotNull PsiElement element){
        return null;
    }

    @Override
    public void collectSlowLineMarkers(@NotNull List<? extends PsiElement> elements,
                                       @NotNull Collection<? super LineMarkerInfo<?>> result){
        for(PsiElement element : elements){
            ExpParserSeqProvider.EP_NAME.computeSafeIfAny(provider -> {
                @Nullable ExpressionParserSequence<?> colorExpression = provider.expressionParserSequenceFrom(element);
                if(colorExpression == null){
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
    public String getName(){
        return CodeInsightBundle.message("gutter.color.preview");
    }

    @NotNull
    @Override
    public Icon getIcon(){
        return AllIcons.Gutter.Colors;
    }

    private static class ExpressionParserSequenceRef{
        private ExpParserSeqProvider provider;
        private PsiElement element;
        private ExpressionParserSequence<?> sequence;

        public ExpressionParserSequenceRef(PsiElement element, ExpParserSeqProvider provider, ExpressionParserSequence<?> sequence){
            this.element = element;
            this.provider = provider;
            this.sequence = sequence;
        }

        public ExpressionParserSequence<?> get(){
            return sequence;
        }

        public void update(PsiElement element){
            ExpressionParserSequence<?> expression = provider.expressionParserSequenceFrom(element);
            if(expression == null) throw new NullPointerException();
            this.element=element;
            sequence = expression;
        }
    }

    private static class MyInfo extends MergeableLineMarkerInfo<PsiElement>{
        /*
         * */
        private final Color myColor;
        private final ExpressionParserSequenceRef ref;

        MyInfo( final ExpressionParserSequenceRef ref){
            super(ref.element,
//            element.getTextRange(),
            ref.get().getTextRange(),
            ref.get().getIcon(),
//            JBUIScale.scaleIcon(new ColorIcon(12, ref.get().getResultColor())),
            FunctionUtil.<Object, String>nullConstant(),
            (e, elt) -> {

//                if(!elt.isWritable() || !expressionParser.isWritable()) return;

                if(!ref.element.isValid()){
                    ref.update(elt);
//                    myColor=ref.get().getResultColor();
                }
                final Editor editor = PsiEditorUtil.findEditor(ref.element);
                assert editor != null;
                ref.get().showPopup(ref.element.getProject(), editor, ref.element.isWritable());

//                expressionParser.showColorPicker(element, e, elt, editor);
            },
            GutterIconRenderer.Alignment.LEFT);
this.ref=ref;
            myColor = ref.get().getIconColor();
//            this.highlighter
//            HighlighterLayer.ADDITIONAL_SYNTAX
        }

        @Override
        public boolean canMergeWith(@NotNull MergeableLineMarkerInfo<?> info){
            return info instanceof MyInfo && ((MyInfo)info).ref.sequence.getClass()==ref.sequence.getClass();
        }

        @Override
        public Icon getCommonIcon(@NotNull List<? extends MergeableLineMarkerInfo<?>> infos){
            return ((MyInfo)infos.get(0)).ref.sequence.mergeIcons(infos.stream().map(_info->((MyInfo)_info).ref.sequence).toArray(ExpressionParserSequence[]::new));
        }

        @NotNull
        @Override
        public Function<? super PsiElement, String> getCommonTooltip(@NotNull List<? extends MergeableLineMarkerInfo<?>> infos){
            return FunctionUtil.nullConstant();
        }
    }


}

