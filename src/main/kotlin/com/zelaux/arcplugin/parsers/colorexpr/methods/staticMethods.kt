@file:Suppress("PackageDirectoryMismatch")

package com.zelaux.arcplugin.parsers.colorexpr

import arc.util.Tmp
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Ref
import com.intellij.psi.PsiCallExpression
import com.intellij.psi.PsiType
import com.intellij.ui.colorpicker.LightCalloutPopup
import com.intellij.ui.picker.ColorListener
import com.zelaux.arcplugin.MetaData
import com.zelaux.arcplugin.ui.JPanelBuilder
import com.zelaux.arcplugin.ui.components.GradientComponent
import com.zelaux.arcplugin.ui.picker.listeners.GrayScaleColorListener
import com.zelaux.arcplugin.ui.picker.popup.DefaultColorPickerPopup
import com.zelaux.arcplugin.ui.picker.popup.GrayScaleColorPickerPopup
import com.zelaux.arcplugin.utils.getFloat
import com.zelaux.arcplugin.utils.getInt
import com.zelaux.arcplugin.utils.replaceFloat
import com.zelaux.arcplugin.utils.replaceInt

internal object staticMethods : ParserFunc() {
    override fun load() {
        "HSVtoRGB"(*(PsiType.FLOAT * 3)) { FromHsvColorParser(it, mapvalues = true, hasAlpha = false).wrap("HSVtoRGB(float,float,float)") }
        "HSVtoRGB"(*(PsiType.FLOAT * 4)) { FromHsvColorParser(it, mapvalues = true, hasAlpha = true).wrap("HSVtoRGB(float,float,float,float)") }
        "HSVtoRGB"(*(PsiType.FLOAT.canonicalText * 3 + MetaData.Color.PATH)) { FromHsvColorParser(it, mapvalues = true, hasAlpha = false).wrap("HSVtoRGB(float,float,float,Color)") }

        "rgb888"(*(PsiType.FLOAT * 3)) { SetFloat3Color(it).wrap("rgb888(float,float,float)") }
        "rgb888"(PsiType.INT) { SetIntColor(it, false).wrap("rgb888(int)") }

        "rgba8888"(*(PsiType.FLOAT * 4)) { SetFloat4Color(it).wrap("rgba8888(float,float,float,float)") }
        "rgba8888"(PsiType.INT) { SetIntColor(it, true).wrap("rgba8888(int)") }
        "grays"(PsiType.FLOAT) { GraysColor(it) }
        //TODO add rgb method
    }
}

class RgbStaticColor(expression: PsiCallExpression) : ColorCallExpressionParser(expression), OffsetableParser, StartPointParser {
    override val tabTitle = "rgb(int,int,int)"
    override val resetInner = true

    override fun apply(color: arcColor): Boolean {
        val list = expression.argumentList ?: return false
        val r = getInt(list.expressions[offset+0]);
        val g = getInt(list.expressions[offset+1]);
        val b = getInt(list.expressions[offset+2]);
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

                    val list = expression.argumentList!!
                    replaceInt(list.expressions[offset+0], color.red)
                    replaceInt(list.expressions[offset+1], color.green)
                    replaceInt(list.expressions[offset+2], color.blue)
                    sequenceManager.get().fireUpdate(this@RgbStaticColor)
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

class GraysColor(expression: PsiCallExpression) : ColorCallExpressionParser(expression), OffsetableParser {
    override val tabTitle = "grays()"
    override val resetInner = true

    override fun apply(color: arcColor): Boolean {
        val graysValue = currentValue() ?: return false;
        color.set(graysValue, graysValue, graysValue, 1f)
        return true;
    }

    private fun currentValue(): Float? {
        val list = expression.argumentList ?: return null
        return try {
            getFloat(list.expressions[offset+0])
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
            return GrayScaleColorPickerPopup
                .instance(awtColor, false)
                .builder(project, GrayScaleColorListener { color, _, _ ->
                    writeColorAction(project) {
                        replaceFloat(expression.argumentList!!.expressions[offset+0], color)
                        sequenceManager.get().fireUpdate(this@GraysColor)
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
