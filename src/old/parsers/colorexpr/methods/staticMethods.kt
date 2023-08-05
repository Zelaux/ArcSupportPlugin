@file:Suppress("PackageDirectoryMismatch")

package com.zelaux.arcplugin.colorExpression.methods

import arc.util.Tmp
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Ref
import com.intellij.psi.PsiType
import com.intellij.ui.colorpicker.LightCalloutPopup
import com.intellij.ui.picker.ColorListener
import com.zelaux.arcplugin.MetaData
import com.zelaux.arcplugin.psi.PrimitiveType
import com.zelaux.arcplugin.ui.JPanelBuilder
import com.zelaux.arcplugin.ui.components.GradientComponent
import com.zelaux.arcplugin.ui.picker.listeners.GrayScaleColorListener
import com.zelaux.arcplugin.ui.picker.popup.DefaultColorPickerPopup
import com.zelaux.arcplugin.ui.picker.popup.GrayScaleColorPickerPopup
import com.zelaux.arcplugin.utils.AfterChangeAppliedKey
import com.zelaux.arcplugin.utils.UastExpressionUtils

import org.jetbrains.uast.UCallExpression

internal object staticMethods : ParserFunc() {
    override fun load() {
        "HSVtoRGB"(*(PrimitiveType.FLOAT * 3)) { FromHsvColorParser(it, mapvalues = true, hasAlpha = false).wrap("HSVtoRGB(float,float,float)") }
        "HSVtoRGB"(*(PrimitiveType.FLOAT * 4)) { FromHsvColorParser(it, mapvalues = true, hasAlpha = true).wrap("HSVtoRGB(float,float,float,float)") }
        "HSVtoRGB"(*(PrimitiveType.FLOAT.canonicalText * 3 + MetaData.Color.PATH)) { FromHsvColorParser(it, mapvalues = true, hasAlpha = false).wrap("HSVtoRGB(float,float,float,Color)") }

        "rgb888"(*(PrimitiveType.FLOAT * 3)) { SetFloat3Color(it).wrap("rgb888(float,float,float)") }
        "rgb888"(PrimitiveType.INT) { SetIntColor(it, false).wrap("rgb888(int)") }

        "rgba8888"(*(PrimitiveType.FLOAT * 4)) { SetFloat4Color(it).wrap("rgba8888(float,float,float,float)") }
        "rgba8888"(PrimitiveType.INT) { SetIntColor(it, true).wrap("rgba8888(int)") }
        "grays"(PrimitiveType.FLOAT) { GraysColor(it) }
        //TODO add rgb method
    }
}

class RgbStaticColor(expression: UCallExpression) : ColorCallExpressionParser(expression), OffsetableParser, StartPointParser {
    override val tabTitle = "rgb(int,int,int)"
    override val resetInner = true

    override fun apply(color: arcColor): Boolean {
        val list = expression.valueArguments
        val r = UastExpressionUtils.getInt(list[offset+0])!!;
        val g = UastExpressionUtils.getInt(list[offset+1])!!;
        val b = UastExpressionUtils.getInt(list[offset+2])!!;
        color.set(r / 255f, g / 255f, b / 255f, 1f)
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
        if (writable)
            return DefaultColorPickerPopup.instance(awtColor, false).builder(project, ColorListener { color, _ ->
                writeColorAction(project) {

                    val list = expression.valueArguments
                    UastExpressionUtils.replaceInt(list[offset+0], color.red)
                    UastExpressionUtils.replaceInt(list[offset+1], color.green)
                    UastExpressionUtils.replaceInt(list[offset+2], color.blue)
                    ApplicationManager.getApplication().invokeLater{
                        sequenceManager.get().fireUpdate(this@RgbStaticColor)//TODO is valid place for fireUpdate?
                    }
                }
            }, popupRef).apply {
                sequenceManager.get().registerListener(this@RgbStaticColor) {
                    val builder = this@apply
                    builder.model.setColor(selfAwtColor, null)
                }
            }
        return MonoColorJPanelBuilder(sequence, awtColor) { colorConsumer ->
            sequenceManager.get().registerListener(this) {
                colorConsumer(selfAwtColor)
            }
        }
    }

}

class GraysColor(expression: UCallExpression) : ColorCallExpressionParser(expression), OffsetableParser {
    override val tabTitle = "grays()"
    override val resetInner = true

    override fun apply(color: arcColor): Boolean {
        val graysValue = currentValue() ?: return false;
        color.set(graysValue, graysValue, graysValue, 1f)
        return true;
    }

    private fun currentValue(): Float? {
        return try {
            UastExpressionUtils.getFloat(expression.valueArguments[offset+0])
        } catch (npe: NullPointerException) {
            return null
        }
    }

    override fun getTabComponent(
        project: Project,
        popupRef: Ref<LightCalloutPopup>,
        sequence: ColorExpParserSequence,
        sequenceManager: Ref<ColorExpSequenceManager>,
        writable: Boolean
    ): JPanelBuilder {
        val value = currentValue() ?: 0f
        if (writable) {
            val awtColor = Tmp.c1.set(value, value, value, 1f).awtColor()
            val key=AfterChangeAppliedKey{UastExpressionUtils.getFloat(expression.valueArguments[offset+0])!!}
            return GrayScaleColorPickerPopup
                .instance(awtColor, false)
                .builder(project, GrayScaleColorListener { color, _, _ ->
                    key.runAfterChange{
                        writeColorAction(project) {
                            UastExpressionUtils.replaceFloat(expression.valueArguments[offset + 0], color)
                            it.newValue(color)
                            it.runAfterChange {

                                sequenceManager.get().fireUpdate(this@GraysColor)
                            }
                        }
                    }
                }, popupRef).apply {
                    sequenceManager.get().registerListener(this@GraysColor) {
                        val builder = this@apply
                        val newValue = currentValue() ?: 0f
                        val newAwtColor = Tmp.c1.set(newValue, newValue, newValue, 1f).awtColor()
                        builder.model.setColor(newAwtColor, null)
                    }
                }
        }
        return JPanelBuilder().apply {
            addComponent(GradientComponent(awtColor.black, awtColor.white, drawKnob = true, editable = false).apply {
                preferredSize = sequence.sliderSize
                minimumSize = sequence.sliderSize
                size = sequence.sliderSize
                setProgressColorValue(value)
            })
        }
    }

}
