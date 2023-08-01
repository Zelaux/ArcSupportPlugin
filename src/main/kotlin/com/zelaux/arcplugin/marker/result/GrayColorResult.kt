package com.zelaux.arcplugin.marker.result

import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiElement
import com.intellij.ui.awt.RelativePoint
import com.zelaux.arcplugin.colorViewer.CustomColorViewer
import com.zelaux.arcplugin.ui.picker.listeners.GrayScaleColorListener
import com.zelaux.arcplugin.ui.picker.popup.GrayScaleColorPickerPopup
import java.awt.Color
import java.awt.event.MouseEvent

@Suppress("MemberVisibilityCanBePrivate")
class GrayColorResult(val grayValue: Float, val alphaValue: Float, val hasAlpha: Boolean, val writable: Boolean) : ColorResult() {
    override val color: Color
        get() = Color(grayValue, grayValue, grayValue, alphaValue)

    override fun toColorViewer(resultListener: ColorResultListener): CustomColorViewer {
        val apply: ((PsiElement, Float, Float) -> Unit)? =
            if (!writable) null else { psiElement: PsiElement, colorValue: Float, alpha: Float ->
                resultListener(psiElement, Color(colorValue, colorValue, colorValue, alpha))
            }
        return GrayScaleColorViewer(grayValue, alphaValue, apply).setHasAlpha(hasAlpha)
    }
}
private typealias ApplyType = (PsiElement, Float, Float) -> Unit

class GrayScaleColorViewer(
    color: Float,
    alpha: Float,
    val apply: ApplyType,
    override var isWritable: Boolean
) : CustomColorViewer {
    override val color = Color(color, color, color, alpha)

    @JvmField
    var hasAlpha = false
    fun setHasAlpha(hasAlpha: Boolean): GrayScaleColorViewer {
        this.hasAlpha = hasAlpha
        return this
    }

    @JvmOverloads
    constructor(color: Float, alpha: Float = Float.NaN, apply: ApplyType? = null) : this(
        color = color,
        alpha = alpha.takeUnless { it.isNaN() } ?: 1f,
        apply = apply ?: { _, _, _ -> },
        isWritable = apply != null,
    ) {
        hasAlpha = !alpha.isNaN() && isWritable
    }

    override fun showColorPicker(element: PsiElement, e: MouseEvent, elt: PsiElement, editor: Editor) {
        val relativePoint = RelativePoint(e.component, e.point)
        GrayScaleColorPickerPopup.instance(color,hasAlpha).show(
            element.project, GrayScaleColorListener { c: Float, a: Float, _ -> WriteAction.run<RuntimeException> { apply(elt, c, a) } },
            relativePoint
        )
    }
}