package com.zelaux.arcplugin.parsers.colorexpr

import arc.graphics.Color
import arc.util.Tmp
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Ref
import com.intellij.psi.PsiCallExpression
import com.intellij.psi.PsiNewExpression
import com.intellij.psi.PsiType
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
import com.zelaux.arcplugin.utils.getFloat
import com.zelaux.arcplugin.utils.getInt
import com.zelaux.arcplugin.utils.replaceFloat
import com.zelaux.arcplugin.utils.replaceInt

internal object `set&constructor` : ParserFunc() {
    override fun load() {
        for (name in arrayOf("", "set")) {
            name(MetaData.Color.PATH) { StaticSetColorParser(it) }
            name(PsiType.INT) { SetIntColor(it, true) }
            name(PsiType.FLOAT, PsiType.FLOAT, PsiType.FLOAT) { SetFloat3Color(it) }
            name(PsiType.FLOAT, PsiType.FLOAT, PsiType.FLOAT, PsiType.FLOAT) { SetFloat4Color(it) }
        }
    }
}

class SetFloat3Color(expression: PsiCallExpression) : ColorCallExpressionParser(expression), OffsetableParser, StartPointParser {
    override fun apply(color: Color): Boolean {
        val exprColor = run {
            val expressions = expression.argumentList?.expressions ?: return@run null
            Color(getFloat(expressions[offset+0]), getFloat(expressions[offset+1]), getFloat(expressions[offset+2]))
        } ?: return false
        color.set(exprColor)
        return true
    }

    override val tabTitle: String
        get() = "set(float,float,float)"
    override val resetInner: Boolean
        get() = true

    override fun getTabComponent(project: Project, popupRef: Ref<LightCalloutPopup>, sequence: ColorExpParserSequence, sequenceManager: Ref<ColorExpSequenceManager>, writable: Boolean): JPanelBuilder {
        val awtColor = selfAwtColor

        if (writable) {
            return DefaultColorPickerPopup.instance(awtColor, false).builder(project, ColorListener { color, _ ->
                val list = expression.argumentList ?: return@ColorListener

                val exp = list.expressions
                writeColorAction(project) {
                    replaceFloat(exp[offset+0], color.red / 255f)
                    replaceFloat(exp[offset+1], color.green / 255f)
                    replaceFloat(exp[offset+2], color.blue / 255f)
                    sequenceManager.get().fireUpdate(this@SetFloat3Color)
                }
            }, popupRef).apply {
                sequenceManager.get().registerListener(this@SetFloat3Color) {
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

class SetFloat4Color(expression: PsiCallExpression) : ColorCallExpressionParser(expression), OffsetableParser, StartPointParser {


    override val tabTitle = "set(float,float,float,float)"
    override val resetInner = true

    override fun apply(color: Color): Boolean {
        val exprColor = run {
            val expressions = expression.argumentList?.expressions ?: return@run null
            Color(getFloat(expressions[offset+0]), getFloat(expressions[offset+1]), getFloat(expressions[offset+2]), getFloat(expressions[offset+3]))
        } ?: return false
        color.set(exprColor)
        return true
    }

    override fun getTabComponent(
        project: Project,
        popupRef: Ref<LightCalloutPopup>,
        sequence: ColorExpParserSequence,
        sequenceManager: Ref<ColorExpSequenceManager>,
        writable: Boolean
    ): JPanelBuilder {

        val awtColor = selfAwtColor

        if (writable) {
            return DefaultColorPickerPopup.instance(awtColor, true).builder(project, ColorListener { color, _ ->
                val list = expression.argumentList ?: return@ColorListener

                val exp = list.expressions
                writeColorAction(project) {
                    replaceFloat(exp[offset+0], color.red / 255f)
                    replaceFloat(exp[offset+1], color.green / 255f)
                    replaceFloat(exp[offset+2], color.blue / 255f)
                    replaceFloat(exp[offset+3], color.alpha / 255f)
                    sequenceManager.get().fireUpdate(this@SetFloat4Color)
                }
            }, popupRef).apply {
                sequenceManager.get().registerListener(this@SetFloat4Color) {
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

class SetIntColor(expression: PsiCallExpression, val hasAlpha: Boolean) : ColorCallExpressionParser(expression), OffsetableParser, StartPointParser {
    override val tabTitle = (if (expression is PsiNewExpression) "new Color" else "set") + "(int)"
    override val resetInner = true

    override fun apply(color: Color): Boolean {
        val list = expression.argumentList ?: return false
        val psiLiteralExpression = list.expressions[offset]
        val intValue = getInt(psiLiteralExpression)
        if (hasAlpha) {
            color.set(intValue)
        } else {
            color.rgb888(intValue)
        }
        return true
    }

    override fun getTabComponent(
        project: Project,
        popupRef: Ref<LightCalloutPopup>,
        sequence: ColorExpParserSequence,
        sequenceManager: Ref<ColorExpSequenceManager>,
        writable: Boolean
    ): JPanelBuilder {

        val awtColor = selfAwtColor

        if (writable) {
            return DefaultColorPickerPopup.instance(awtColor, hasAlpha).builder(project, ColorListener { color, _ ->
                val list = expression.argumentList ?: return@ColorListener

                val exp = list.expressions
                writeColorAction(project) {
                    Tmp.c1.set(color)
                    val newValue = if (hasAlpha) Tmp.c1.rgba8888() else Tmp.c1.rgb888()
                    replaceInt(exp[offset], newValue, true)
                    sequenceManager.get().fireUpdate(this@SetIntColor)
                }
            }, popupRef).apply {
                sequenceManager.get().registerListener(this@SetIntColor) {
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