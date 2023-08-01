package com.zelaux.arcplugin.marker.result

import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.util.Pair
import com.intellij.psi.PsiElement
import com.intellij.ui.awt.RelativePoint
import com.zelaux.arcplugin.colorViewer.CustomColorViewer
import com.zelaux.arcplugin.ui.picker.popup.GradientColorPickerPopup
import com.zelaux.arcplugin.utils.ColorUtils
import java.awt.Color
import java.awt.event.MouseEvent

@Suppress("MemberVisibilityCanBePrivate")
open class GradientColorResult(val colorA: Color, val colorB: Color, val progress: Float, val writable: Boolean, val hasAlpha: Boolean) : ColorResult() {
    override fun toColorViewer(resultListener: ColorResultListener): CustomColorViewer {
        return GradientColorViewer(colorA, colorB, progress, { psiElement, progress, _ -> resultListener(psiElement, ColorUtils.lerp(colorA, colorB,  /*(int)(alpha * 255),*/progress)) }, writable).setHasAlpha(
            hasAlpha
        )
    }

    override val color: Color get() = ColorUtils.lerp(colorA, colorB, progress)

    companion object {
        @JvmStatic
        fun writable(colorA: Color, colorB: Color, progress: Float, hasAlpha: Boolean): GradientColorResult {
            return GradientColorResult(colorA, colorB, progress, true, hasAlpha)
        }

        @JvmStatic
        fun notWritable(colorA: Color, colorB: Color, progress: Float): GradientColorResult {
            return GradientColorResult(colorA, colorB, progress, writable = false, hasAlpha = false)
        }
    }
}

class GradientColorViewer(
    @JvmField val colorA: Color,
    @JvmField val colorB: Color,
    @JvmField val progress: Float,
    @JvmField val apply: GradientColorViewerApply,
    override val isWritable: Boolean
) : CustomColorViewer {
    @Suppress("unused")
    constructor(colorB: Color, colorA: Color, progress: Float, apply: GradientColorViewerApply?) :
            this(colorA, colorB, progress, apply ?: GradientColorViewerApply.defaultApply, apply != null)

    override val color: Color get() = ColorUtils.lerp(colorA, colorB, progress)

    @JvmField
    var hasAlpha = false

    fun setHasAlpha(hasAlpha: Boolean) = apply { this.hasAlpha = hasAlpha }

    fun apply(psiElement: PsiElement, progress: Float, alpha: Int) =
        WriteAction.run<RuntimeException> { apply.invoke(psiElement, progress, alpha / 255f) }


    override fun showColorPicker(element: PsiElement, e: MouseEvent, elt: PsiElement, editor: Editor) {
        val relativePoint = RelativePoint(e.component, e.point)
        GradientColorPickerPopup.instance(color, hasAlpha).show(element.project, Pair(this, elt), relativePoint)
    }

    fun interface GradientColorViewerApply {
        operator fun invoke(psiElement: PsiElement, progress: Float, alpha: Float)

        companion object {
            val defaultApply: GradientColorViewerApply = GradientColorViewerApply { _, _, _ -> }
        }
    }
}