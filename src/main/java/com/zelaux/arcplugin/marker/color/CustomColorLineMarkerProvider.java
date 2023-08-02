package com.zelaux.arcplugin.marker.color;

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
import com.intellij.util.Function;
import com.intellij.util.ui.*;
import com.zelaux.arcplugin.colorViewer.*;
import org.jetbrains.annotations.*;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

@SuppressWarnings("UseJBColor")
public class CustomColorLineMarkerProvider extends LineMarkerProviderDescriptor{
    public static final CustomColorLineMarkerProvider INSTANCE = new CustomColorLineMarkerProvider();

    public CustomColorLineMarkerProvider(){
    }

    @Override
    public LineMarkerInfo<?> getLineMarkerInfo(@NotNull PsiElement element){
        return null;
    }

    @Override
    public void collectSlowLineMarkers(@NotNull List<? extends PsiElement> elements,
                                       @NotNull Collection<? super LineMarkerInfo<?>> result){
        for(PsiElement element : elements){
            ElementColorViewerProvider.EP_NAME.computeSafeIfAny(provider -> {
                @Nullable CustomColorViewer colorFrom = provider.getColorViewerFrom(element);
                if(colorFrom == null){
                    return null;
                }

                MyInfo info = new MyInfo(element, colorFrom);
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

    private static class MyInfo extends MergeableLineMarkerInfo<PsiElement>{

        private final Color myColor;

        MyInfo(@NotNull final PsiElement element, final CustomColorViewer viewer){
            super(element,
            element.getTextRange(),
            JBUIScale.scaleIcon(new ColorIcon(12, viewer.getColor())),
            FunctionUtil.<Object, String>nullConstant(),
            !viewer.isWritable() ? null : (e, elt) -> {
                if(!elt.isWritable() || !viewer.isWritable()) return;

                final Editor editor = PsiEditorUtil.findEditor(elt);
                assert editor != null;

                viewer.showColorPicker(element, e, elt, editor);
            },
            GutterIconRenderer.Alignment.LEFT,()->"Color Preview");
            myColor = viewer.getColor();
        }

        @Override
        public boolean canMergeWith(@NotNull MergeableLineMarkerInfo<?> info){
            return info instanceof MyInfo;
        }

        @Override
        public Icon getCommonIcon(@NotNull List<? extends MergeableLineMarkerInfo<?>> infos){
            return JBUIScale.scaleIcon(new ColorsIcon(12, infos.stream().map(_info -> ((MyInfo)_info).myColor).toArray(Color[]::new)));
        }

        @NotNull
        @Override
        public Function<? super PsiElement, String> getCommonTooltip(@NotNull List<? extends MergeableLineMarkerInfo<?>> infos){
            return FunctionUtil.nullConstant();
        }
    }


}

