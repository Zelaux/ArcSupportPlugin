@file:Suppress("PackageDirectoryMismatch")

package com.zelaux.arcplugin.colorExpression.methods

import com.zelaux.arcplugin.MetaData
import com.zelaux.arcplugin.expressions.resolve.methods.CpyExpression

//import com.zelaux.arcplugin.ArcColorUtils


internal object cpy : ColorRegister() {
    override fun load() {
        "cpy"(*emptyArray<String>()) { CpyExpression(it,"cpy()") }
        "write"(MetaData.Color.PATH) { CpyExpression(it,"write(Color)") }

    }
}
/*

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

}*/
