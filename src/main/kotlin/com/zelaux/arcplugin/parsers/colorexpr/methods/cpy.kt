@file:Suppress("PackageDirectoryMismatch")

package com.zelaux.arcplugin.parsers.colorexpr

import arc.graphics.Color
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Ref
import com.intellij.psi.PsiCallExpression
import com.intellij.ui.colorpicker.LightCalloutPopup
import com.zelaux.arcplugin.ArcColorUtils
import com.zelaux.arcplugin.parsers.colorexpr.*
import com.zelaux.arcplugin.parsers.colorexpr.MonoColorJPanelBuilder
import com.zelaux.arcplugin.parsers.colorexpr.set
import com.zelaux.arcplugin.ui.JPanelBuilder
import org.jetbrains.uast.UExpression
import org.jetbrains.uast.toUElement

internal object cpy : ParserFunc() {
    override fun load() {
        "cpy"(*emptyArray<String>()) { CpyMethodColor(it) }
    }
}

class CpyMethodColor(expression: PsiCallExpression) : ColorCallExpressionParser(expression) {
    private val color: Color? by lazy {
        val uExpression = expression.firstChild.firstChild.toUElement(UExpression::class.java) ?: return@lazy null
        Color().set(ArcColorUtils.resolveColor(uExpression) ?: return@lazy null)
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