package com.zelaux.arcplugin.colorViewer

import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiElement
import java.awt.Color
import java.awt.event.MouseEvent

interface CustomColorViewer {
    val color: Color?
    val isWritable: Boolean
    fun showColorPicker(element: PsiElement, e: MouseEvent, elt: PsiElement, editor: Editor)
}