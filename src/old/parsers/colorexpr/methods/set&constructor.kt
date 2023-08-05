package com.zelaux.arcplugin.colorExpression.methods

import arc.graphics.Color
import arc.util.Tmp
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Ref
import com.intellij.psi.PsiNewExpression
import com.intellij.psi.PsiType
import com.intellij.ui.colorpicker.LightCalloutPopup
import com.intellij.ui.picker.ColorListener
import com.zelaux.arcplugin.MetaData
import com.zelaux.arcplugin.psi.PrimitiveType
import com.zelaux.arcplugin.ui.JPanelBuilder
import com.zelaux.arcplugin.ui.picker.popup.DefaultColorPickerPopup
import com.zelaux.arcplugin.utils.AfterChangeAppliedKey
import com.zelaux.arcplugin.utils.UastExpressionUtils
import org.jetbrains.uast.UCallExpression

internal object `set&constructor` : ParserFunc() {
    override fun load() {
        for (name in arrayOf("", "set")) {
            name(MetaData.Color.PATH) { StaticSetColorParser(it) }
            name(PrimitiveType.INT) { SetIntColor(it, true) }
            name(PrimitiveType.FLOAT, PrimitiveType.FLOAT, PrimitiveType.FLOAT) { SetFloat3Color(it) }
            name(PrimitiveType.FLOAT, PrimitiveType.FLOAT, PrimitiveType.FLOAT, PrimitiveType.FLOAT) { SetFloat4Color(it) }
        }
    }
}

class SetFloat3Color(expression: UCallExpression) : ColorCallExpressionParser(expression), OffsetableParser, StartPointParser {
    override fun apply(color: Color): Boolean {
        val exprColor = run {
            val expressions = expression.valueArguments
            Color(UastExpressionUtils.getFloat(expressions[offset + 0])!!,
                    UastExpressionUtils.getFloat(expressions[offset + 1])!!,
                    UastExpressionUtils.getFloat(expressions[offset + 2])!!)
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
            val key = AfterChangeAppliedKey {
                floatArrayOf(
                        UastExpressionUtils.getFloat(expression.valueArguments[offset + 0])!!,
                        UastExpressionUtils.getFloat(expression.valueArguments[offset + 1])!!,
                        UastExpressionUtils.getFloat(expression.valueArguments[offset + 2])!!
                )
            }
            return DefaultColorPickerPopup.instance(awtColor, false).builder(project, ColorListener { color, _ ->


                key.runAfterChange {
                    val exp = expression.valueArguments
                    writeColorAction(project) {
                        val sourcePsi = element.sourcePsi!!
                        UastExpressionUtils.replaceFloat(exp[offset + 0], color.red / 255f)
                        UastExpressionUtils.replaceFloat(exp[offset + 1], color.green / 255f)
                        UastExpressionUtils.replaceFloat(exp[offset + 2], color.blue / 255f)

//                    element = sourcePsi.toUElement()!!
                        it.newValue(floatArrayOf(color.red / 255f, color.green / 255f, color.blue / 255f))
                        it.runAfterChange { sequenceManager.get().fireUpdate(this@SetFloat3Color) }
                    }
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

class SetFloat4Color(expression: UCallExpression) : ColorCallExpressionParser(expression), OffsetableParser, StartPointParser {


    override val tabTitle = "set(float,float,float,float)"
    override val resetInner = true

    override fun apply(color: Color): Boolean {
        val exprColor = run {
            val expressions = expression.valueArguments
            Color(UastExpressionUtils.getFloat(expressions[offset + 0])!!,
                    UastExpressionUtils.getFloat(expressions[offset + 1])!!,
                    UastExpressionUtils.getFloat(expressions[offset + 2])!!,
                    UastExpressionUtils.getFloat(expressions[offset + 3])!!)
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
            val key = AfterChangeAppliedKey {
                floatArrayOf(
                        UastExpressionUtils.getFloat(expression.valueArguments[offset + 0])!!,
                        UastExpressionUtils.getFloat(expression.valueArguments[offset + 1])!!,
                        UastExpressionUtils.getFloat(expression.valueArguments[offset + 2])!!,
                        UastExpressionUtils.getFloat(expression.valueArguments[offset + 3])!!,
                )
            }
            return DefaultColorPickerPopup.instance(awtColor, true).builder(project, ColorListener { color, _ ->


                key.runAfterChange {
                    val exp = expression.valueArguments
                    writeColorAction(project) {
                        val sourcePsi = element.sourcePsi
                        UastExpressionUtils.replaceFloat(exp[offset + 0], color.red / 255f)
                        UastExpressionUtils.replaceFloat(exp[offset + 1], color.green / 255f)
                        UastExpressionUtils.replaceFloat(exp[offset + 2], color.blue / 255f)
                        UastExpressionUtils.replaceFloat(exp[offset + 3], color.alpha / 255f)

                        it.newValue(floatArrayOf(color.red / 255f, color.green / 255f, color.blue / 255f, color.alpha / 255f))
                        it.runAfterChange {
                            sequenceManager.get().fireUpdate(this@SetFloat4Color)
                        }
//                    element = sourcePsi.toUElement()!!
                    }
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

class SetIntColor(expression: UCallExpression, val hasAlpha: Boolean) : ColorCallExpressionParser(expression), OffsetableParser, StartPointParser {
    override val tabTitle = (if (expression is PsiNewExpression) "new Color" else "set") + "(int)"
    override val resetInner = true

    override fun apply(color: Color): Boolean {
        val psiLiteralExpression = expression.valueArguments[offset]
        val intValue = UastExpressionUtils.getInt(psiLiteralExpression) ?: return false
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
            return DefaultColorPickerPopup
                    .instance(awtColor, hasAlpha)
                    .builder(project, ColorListener { color, _ ->


                val exp = expression.valueArguments
                writeColorAction(project) {
                    Tmp.c1.set(color)
                    val newValue = if (hasAlpha) Tmp.c1.rgba8888() else Tmp.c1.rgb888()
                    UastExpressionUtils.replaceInt(exp[offset], newValue, true)
                 ApplicationManager.getApplication().invokeLater {
                     sequenceManager.get().fireUpdate(this@SetIntColor)//TODO is valid place for fireUpdate?
                 }
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