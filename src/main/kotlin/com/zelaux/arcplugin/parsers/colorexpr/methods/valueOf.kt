@file:Suppress("PackageDirectoryMismatch")

package com.zelaux.arcplugin.parsers.colorexpr

import arc.graphics.Color
import arc.util.Tmp
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Ref
import com.intellij.psi.CommonClassNames
import com.intellij.psi.PsiCallExpression
import com.intellij.psi.PsiLiteralExpression
import com.intellij.ui.colorpicker.LightCalloutPopup
import com.intellij.ui.picker.ColorListener
import com.zelaux.arcplugin.MetaData
import com.zelaux.arcplugin.parsers.colorexpr.*
import com.zelaux.arcplugin.parsers.colorexpr.MonoColorJPanelBuilder
import com.zelaux.arcplugin.parsers.colorexpr.OffsetableParser
import com.zelaux.arcplugin.parsers.colorexpr.StartPointParser
import com.zelaux.arcplugin.parsers.colorexpr.set
import com.zelaux.arcplugin.parsers.colorexpr.writeColorAction
import com.zelaux.arcplugin.ui.JPanelBuilder
import com.zelaux.arcplugin.ui.picker.popup.DefaultColorPickerPopup
import com.zelaux.arcplugin.utils.replaceString

internal object valueOf : ParserFunc() {
    override fun load() {
        "valueOf"(MetaData.Color.PATH, CommonClassNames.JAVA_LANG_STRING) { ValueOfParser(it).offset(1) }
        "valueOf"(CommonClassNames.JAVA_LANG_STRING) { ValueOfParser(it) }
    }
}

class ValueOfParser(expression: PsiCallExpression) : ColorCallExpressionParser(expression), OffsetableParser, StartPointParser {
    override val tabTitle = "valueOf(_?,String)"

    override val resetInner = true

    override fun apply(color: Color): Boolean {
        run<Color?> {
            val list = expression.argumentList ?: return@run null
            val stringExpression = list.expressions[offset+0]
            val literalExpression = stringExpression as? PsiLiteralExpression
            val hex = literalExpression?.value as? String

            try {
                Color.valueOf(hex)
            } catch (e: Exception) {
                null
            }
        }.let {
            if (it == null) {
                this.unpredictable = true
                color.set(0xFF)
                return false
            } else {
                color.set(it)
            }
        }
        return true
    }

    override fun getTabComponent(project: Project, popupRef: Ref<LightCalloutPopup>, sequence: ColorExpParserSequence, sequenceManager: Ref<ColorExpSequenceManager>, writable: Boolean): JPanelBuilder {
        //TODO color selection
        val awtColor = selfAwtColor

        if (writable) {
            return DefaultColorPickerPopup.instance(awtColor, true).builder(project, ColorListener { color, _ ->
                val list = expression.argumentList ?: return@ColorListener

                val exp = list.expressions
                writeColorAction(project) {
                    Tmp.c1.set(color)
                    replaceString(exp[offset], Tmp.c1.toString())
                    sequenceManager.get().fireUpdate(this@ValueOfParser)
                }
            }, popupRef).apply {
                sequenceManager.get().registerListener(this@ValueOfParser) {
                    val builder = this@apply
                    builder.model.setColor(selfAwtColor, null)
                }
            }
        } else {
            return MonoColorJPanelBuilder(sequence, awtColor) { colorConsumer ->
                sequenceManager.get().registerListener(this) {
                    colorConsumer(selfAwtColor)
                }
            }
        }
    }

}

