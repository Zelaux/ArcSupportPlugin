package com.zelaux.arcplugin.marker.result

import com.intellij.psi.PsiElement
import com.zelaux.arcplugin.colorViewer.CustomColorViewer
import java.awt.Color

abstract class ColorResult {
    val isNull get() = this is NullColorResult

    abstract fun toColorViewer(resultListener: ColorResultListener): CustomColorViewer
    val isNotNull get() = !isNull
    abstract val color: Color?

    fun interface ColorResultListener {
        operator fun invoke(psiElement: PsiElement, color: Color)
    }
}