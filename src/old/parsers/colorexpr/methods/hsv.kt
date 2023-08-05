@file:Suppress("PackageDirectoryMismatch")

package com.zelaux.arcplugin.colorExpression.methods

import arc.graphics.Color
import arc.util.Tmp
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Ref
import com.intellij.psi.PsiType
import com.intellij.ui.colorpicker.LightCalloutPopup
import com.intellij.ui.components.Label
import com.zelaux.arcplugin.psi.PrimitiveType
import com.zelaux.arcplugin.ui.JPanelBuilder
import com.zelaux.arcplugin.ui.components.GradientComponent
import com.zelaux.arcplugin.ui.picker.popup.DefaultColorPickerPopup
import com.zelaux.arcplugin.utils.AfterChangeAppliedKey
import com.zelaux.arcplugin.utils.CustomUastTreeUtil
import com.zelaux.arcplugin.utils.UastExpressionUtils
import org.jetbrains.uast.UCallExpression

internal object hsv : ParserFunc() {
    override fun load() {
        "fromHsv"(*(PrimitiveType.FLOAT * 3)) { FromHsvColorParser(it, false, hasAlpha = false) }
        for (component in ShiftHsvComponent.Companion.HsvComponent.values()) {
            component.name(PrimitiveType.FLOAT) { ShiftHsvComponent(it, component) }
        }
        //TODO add support for hsv setters
    }
}

class ShiftHsvComponent(expression: UCallExpression, val componentType: HsvComponent) : ColorCallExpressionParser(expression), OffsetableParser {
    override val tabTitle get() = componentType.tabTitle

    companion object {

        enum class HsvComponent(val tabTitle: String, val maxvalue: Float, val getter: Color.() -> Float, val shifter: Color.(Float) -> Unit) {
            shiftHue("shiftHue()", 360f, { hue() }, { shiftHue(it) }), //TODO add hue slider
            shiftSaturation("shiftSaturation()", 1f, { saturation() }, { shiftSaturation(it) }),
            shiftValue("shiftValue()", 1f, { value() }, { shiftValue(it) })
        }
    }

    fun currentValue(): Float {
        return UastExpressionUtils.getFloat(expression.valueArguments[offset])!!
    }

    override fun apply(color: Color): Boolean {
        try {
            componentType.shifter(color, currentValue())
            return true
        } catch (npe: NullPointerException) {
            return false
        }
    }

    override fun getTabComponent(project: Project, popupRef: Ref<LightCalloutPopup>, sequence: ColorExpParserSequence, sequenceManager: Ref<ColorExpSequenceManager>, writable: Boolean): JPanelBuilder = JPanelBuilder().apply {
        val getter = componentType.getter
        val shifter = componentType.shifter

        var (inputValue, firstColor, secondColor) = calculateColor(sequence, getter, shifter)

        val label = Label("Has no effect on result")
        if (firstColor == secondColor) {
            addComponent(label)
            return@apply
        }

        addComponent(GradientComponent(firstColor, secondColor, editable = writable).apply {
            preferredSize = sequence.sliderSize
            minimumSize = sequence.sliderSize
            size = sequence.sliderSize
            val gradientComponent = this;
            sequenceManager.get().registerListener(this@ShiftHsvComponent) {
                val (inputValue1, firstColor1, secondColor1) = calculateColor(sequence, getter, shifter)
                inputValue = inputValue1
                colorA = firstColor1
                colorB = secondColor1
                val shouldLabel = firstColor1 == secondColor1;
                val hasLabel = label.parent != null;
//                this.setProgressColorValue((inputValue + currentValue()) / componentType.maxvalue)
                if (shouldLabel == hasLabel) return@registerListener

                val (from, to) = if (shouldLabel) gradientComponent to label else label to gradientComponent
                swapComponent(from, to)

            }
            var updating = false;
            this.setProgressColorValue((inputValue + currentValue()) / componentType.maxvalue)
            addProgressListener { progress ->
                if (updating) return@addProgressListener
                updating = true;
                writeColorAction(project) {
                    val expr = expression.valueArguments
                    val newValue = progress * componentType.maxvalue - inputValue
                    UastExpressionUtils.replaceFloat(expr[offset], newValue)
                    CustomUastTreeUtil.ChangeAppliedListener(newValue, { UastExpressionUtils.getObject(expression.valueArguments[offset]) }) {
                        updating = false
                        sequenceManager.get().fireUpdate(this@ShiftHsvComponent)
                    }
//                    element = sourcePsi.toUElement()!!
//                    element = CustomUastTreeUtil.restore(psiFile, prevrange, delta)


//                    updateCallback.get()(this@ShiftHsvComponent)
                }
            }
        })
        withFocus()
    }


    private fun calculateColor(
            sequence: ColorExpParserSequence,
            getter: Color.() -> Float,
            shifter: Color.(Float) -> Unit
    ): Triple<Float, awtColor, awtColor> {
        val inputValue = sequence.stopColorOn(this@ShiftHsvComponent, Tmp.c1.set(0xff)).getter()

        val firstColor = Tmp.c2.set(Tmp.c1).apply { shifter(-inputValue) }.awtColor()
        val secondColor = Tmp.c2.set(Tmp.c1).apply { shifter(componentType.maxvalue - inputValue) }.awtColor()
        return Triple(inputValue, firstColor, secondColor)
    }
}

class FromHsvColorParser(expression: UCallExpression, mapvalues: Boolean, val hasAlpha: Boolean) : ColorCallExpressionParser(expression), OffsetableParser, StartPointParser {
    override val tabTitle = "fromHsv(float,float,float)"
    override val resetInner = true

    var componentMultiples = FloatArray(3) { 1f }

    init {
        if (mapvalues) {
            componentMultiples[1] = 100f;
            componentMultiples[2] = 100f;
        }
    }

    override fun apply(color: Color): Boolean {
        val list = expression.valueArguments
        color.fromHsv(FloatArray(3) { i -> UastExpressionUtils.getFloat(list[offset + i])!! / componentMultiples[i] })
        if (hasAlpha()) {
            color.a = UastExpressionUtils.getFloat(list[offset + 3])!!
        }

        return true
    }

    override fun getTabComponent(project: Project, popupRef: Ref<LightCalloutPopup>, sequence: ColorExpParserSequence, sequenceManager: Ref<ColorExpSequenceManager>, writable: Boolean): JPanelBuilder {
        val awtColor = selfAwtColor
        if (writable) {
            val key = if (hasAlpha) {
                AfterChangeAppliedKey {
                    floatArrayOf(
                            UastExpressionUtils.getFloat(expression.valueArguments[offset])!!,
                            UastExpressionUtils.getFloat(expression.valueArguments[offset + 1])!!,
                            UastExpressionUtils.getFloat(expression.valueArguments[offset + 2])!!,
                            UastExpressionUtils.getFloat(expression.valueArguments[offset + 3])!!
                    )
                }
            } else {
                AfterChangeAppliedKey {
                    floatArrayOf(
                            UastExpressionUtils.getFloat(expression.valueArguments[offset])!!,
                            UastExpressionUtils.getFloat(expression.valueArguments[offset + 1])!!,
                            UastExpressionUtils.getFloat(expression.valueArguments[offset + 2])!!
                    )
                }
            }
            return DefaultColorPickerPopup.instance(awtColor, hasAlpha()).builder(project, { color, _ ->
                val hsv = color.arcColor().toHsv(FloatArray(3))
                for ((i, v) in hsv.withIndex()) {
                    hsv[i] = v * componentMultiples[i]
                }

                val newAlphaValue = color.alpha / 255f
                val newValues = if (hasAlpha) {
                    floatArrayOf(hsv[0], hsv[1], hsv[2], newAlphaValue)
                } else {
                    hsv
                }
                key.runAfterChange {
                    val list = expression.valueArguments
                    writeColorAction(project) {
                        var delta = 0;
                        for (i in hsv.indices) {
                            delta += UastExpressionUtils.replaceFloat(list[offset + i], hsv[i])
                        }
                        if (hasAlpha()) {
                            delta += UastExpressionUtils.replaceFloat(list[offset + 3], newAlphaValue)
                        }
                        it.newValue(newValues)
//                    element = sourcePsi.toUElement()!!
//                    element=CustomUastTreeUtil.restore(file,range,delta)
                        it.runAfterChange {
                            sequenceManager.get().fireUpdate(this@FromHsvColorParser)
                        }
                    }
                }
            }, popupRef).apply {
                sequenceManager.get().registerListener(this@FromHsvColorParser) {
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

    private fun hasAlpha(): Boolean {
        return hasAlpha && expression.valueArguments.let { it.size > 3 + offset && PrimitiveType.FLOAT.isEqual(it[offset + 3].getExpressionType()) }
    }

}