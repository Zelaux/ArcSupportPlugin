@file:Suppress("PackageDirectoryMismatch")

package com.zelaux.arcplugin.colorExpression.methods

import arc.graphics.Color
import arc.util.Tmp
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Ref
import com.intellij.psi.CommonClassNames
import com.intellij.ui.colorpicker.LightCalloutPopup
import com.intellij.ui.picker.ColorListener
import com.zelaux.arcplugin.MetaData
import com.zelaux.arcplugin.ui.JPanelBuilder
import com.zelaux.arcplugin.ui.picker.popup.DefaultColorPickerPopup
import com.zelaux.arcplugin.utils.UastExpressionUtils
import org.jetbrains.uast.UCallExpression
import org.jetbrains.uast.ULiteralExpression

internal object valueOf : ParserFunc() {
    override fun load() {
        "valueOf"(MetaData.Color.PATH, CommonClassNames.JAVA_LANG_STRING) { ValueOfParser(it).offset(1) }
        "valueOf"(CommonClassNames.JAVA_LANG_STRING) { ValueOfParser(it) }
    }
}

class ValueOfParser(expression: UCallExpression) : ColorCallExpressionParser(expression), OffsetableParser, StartPointParser {
    override val tabTitle = "valueOf(_?,String)"

    override val resetInner = true

    override fun apply(color: Color): Boolean {
        run<Color?> {
            val list = expression.valueArguments
            val stringExpression = list[offset+0]
            val literalExpression = stringExpression as? ULiteralExpression
            val hex = literalExpression?.value as? String ?: return@run null

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


                val exp = expression.valueArguments
                writeColorAction(project) {
                    Tmp.c1.set(color)
                    UastExpressionUtils.replaceString(exp[offset], Tmp.c1.toString())
                    invalidateElement();
                    sequenceManager.get().fireUpdate(this@ValueOfParser)//TODO is valid place for fireUpdate?
                }
            }, popupRef).apply {
                /*sequenceManager.get().registerListener(this@ValueOfParser) {
                    val builder = this@apply
                    builder.model.setColor(selfAwtColor, null)
                }*/
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

