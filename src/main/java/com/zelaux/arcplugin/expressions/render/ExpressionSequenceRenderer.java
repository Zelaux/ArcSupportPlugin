package com.zelaux.arcplugin.expressions.render;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.zelaux.arcplugin.expressions.resolve.ExpressionSequence;
import groovy.lang.Tuple3;
import kotlin.TuplesKt;

import javax.swing.*;
import java.awt.*;

public interface ExpressionSequenceRenderer<SELF extends ExpressionSequenceRenderer<SELF>> {
    Icon getIcon();
    Color getIconColor();

    void showPopup(Project project, Editor editor, boolean writable);

    Icon mergeIcons(MyTuple<ExpressionSequence<?>,SELF,Color>[] array);

    class MyTuple<A,B,C>{
        public final A a;
        public final B b;
        public final C c;

        public MyTuple(A a, B b, C c) {
            this.a = a;
            this.b = b;
            this.c = c;
        }
    }
}
