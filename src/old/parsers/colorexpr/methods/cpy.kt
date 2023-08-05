@file:Suppress("PackageDirectoryMismatch")

package com.zelaux.arcplugin.colorExpression.methods

import arc.graphics.Color
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Ref
import com.intellij.ui.colorpicker.LightCalloutPopup
//import com.zelaux.arcplugin.ArcColorUtils
import com.zelaux.arcplugin.colorExpression.ColorExpressionResolver
import com.zelaux.arcplugin.ui.JPanelBuilder
import org.jetbrains.uast.UCallExpression

internal object cpy : ParserFunc() {
    override fun load() {
        "cpy"(*emptyArray<String>()) { CpyMethodColor(it) }
    }
}

class CpyMethodColor(expression: UCallExpression) : ColorCallExpressionParser(expression) {
    private val color: Color? by lazy {

        val expr = expression.receiver ?: return@lazy null
        Color().set(ColorExpressionResolver.resolveColor(expr) ?: return@lazy null)
    }

    override fun apply(color: Color): Boolean {
        if (this.color == null) return false
        color.set(this.color)
        return true
    }

    override val tabTitle: String get() = "cpy()"
    override val resetInner: Boolean get() = true
    override val isStatic: Boolean get() = true

    override fun getTabComponent(project: Project, popupRef: Ref<LightCalloutPopup>, sequence: ColorExpParserSequence, sequenceManager: Ref<ColorExpSequenceManager>, writable: Boolean): JPanelBuilder {
        val awtColor = this@CpyMethodColor.color?.awtColor() ?: java.awt.Color.black
        return MonoColorJPanelBuilder(sequence, awtColor) { colorConsumer ->
            sequenceManager.get().registerListener(this) {
                colorConsumer(this@CpyMethodColor.color?.awtColor() ?: java.awt.Color.black)
            }
        }
    }

}