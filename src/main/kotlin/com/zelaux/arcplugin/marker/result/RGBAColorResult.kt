package com.zelaux.arcplugin.marker.result

import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiElement
import com.intellij.ui.awt.RelativePoint
import com.intellij.ui.picker.ColorListener
import com.zelaux.arcplugin.colorViewer.CustomColorViewer
import com.zelaux.arcplugin.ui.picker.popup.DefaultColorPickerPopup
import java.awt.Color
import java.awt.event.MouseEvent

@Suppress("MemberVisibilityCanBePrivate")
class RGBAColorResult(override val color: Color, val writable: Boolean, val hasAlpha: Boolean) : ColorResult() {
    override fun toColorViewer(resultListener: ColorResultListener): CustomColorViewer {
        return DefaultColorViewer(color, resultListener::invoke).setWritable(writable).setHasAlpha(hasAlpha)
    }

    companion object {
        @JvmStatic
        fun writable(color: Color, hasAlpha: Boolean): RGBAColorResult {
            return RGBAColorResult(color, true, hasAlpha)
        }

        @JvmStatic
        fun notWritable(color: Color): RGBAColorResult {
            return RGBAColorResult(color, writable = false, hasAlpha = false)
        }
    }
}

class DefaultColorViewer(override val color: Color, val apply: (PsiElement, Color) -> Unit) : CustomColorViewer {
    override var isWritable = false
        private set

    @JvmField
    var hasAlpha = true


    fun setHasAlpha(hasAlpha: Boolean): DefaultColorViewer {
        this.hasAlpha = hasAlpha
        return this
    }

    fun setWritable(writable: Boolean): DefaultColorViewer {
        isWritable = writable
        return this
    }

    override fun showColorPicker(element: PsiElement, e: MouseEvent, elt: PsiElement, editor: Editor) {
        val relativePoint = RelativePoint(e.component, e.point)
        DefaultColorPickerPopup.instance(color, hasAlpha).show(
            element.project, ColorListener { c: Color, _ -> WriteAction.run<RuntimeException> { apply.invoke(elt, c) } }, relativePoint
        )
    }
}