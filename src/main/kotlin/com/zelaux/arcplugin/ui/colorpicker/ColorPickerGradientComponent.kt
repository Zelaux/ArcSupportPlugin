@file:Suppress("JAVA_MODULE_DOES_NOT_EXPORT_PACKAGE")

package com.zelaux.arcplugin.ui.colorpicker


import com.intellij.openapi.util.registry.Registry
import com.intellij.ui.colorpicker.*
import com.intellij.ui.picker.ColorListener
import com.zelaux.arcplugin.awt.GradientColor
import com.zelaux.arcplugin.ui.components.GradientComponent
import com.zelaux.arcplugin.utils.ColorUtils
import java.awt.*
import javax.swing.JComponent
import kotlin.math.ceil




open class ColorPickerGradientComponent(protected val myModel: ColorPickerModel, colorA: Color, colorB: Color) : GradientComponent(colorA, colorB), ColorListener, ColorPipette.Callback {
    init {
        myModel.addListener(this)
        if (Registry.`is`("ide.color.picker.new.pipette")) {
            myModel.addPipetteListener(this)
        }
        addProgressListener { myModel.setColor(getColorByProgress(it), this) }
    }

    open fun getColorByProgress(p: Float): Color {
//        val x = Math.max(0, Math.min(p.x, size.width))
//        val y = Math.max(0, Math.min(p.y, size.height))

//        val saturation = x.toFloat() / size.width
//        val progress = x.toFloat() / size.width
//        val brightness = 1.0f - y.toFloat() / size.height
        return ColorUtils.lerp(colorA, colorB, p)
    }

private var isColorChangind=false
    override fun colorChanged(color: Color, source: Any?) {
        if (isColorChangind)return
//        val hsbValues = Color.RGBtoHSB(color.red, color.green, color.blue, null)
//        setHSBAValue(hsbValues[0], hsbValues[1], hsbValues[2], color.alpha)
        isColorChangind=true;
        if (color is GradientColor) {
            setProgressColorValue(color.progress)
        } else {
            setProgressColorValue(ColorUtils.progress(color, colorA, colorB))
        }
        isColorChangind=false;
    }


    override fun picked(pickedColor: Color) {
        pipetteMode = false
    }

    override fun update(updatedColor: Color) {
        pipetteMode = true
        repaint()
    }

    override fun cancel() {
        pipetteMode = false
    }
}


open class ColorPickerGradientComponentWithAlpha(myModel: ColorPickerModel, colorA: Color, colorB: Color) : ColorPickerGradientComponent(myModel, colorA, colorB) {

    var alpha: Int = 255
        private set

//    @Suppress("ReplaceJavaStaticMethodWithKotlinAnalog")
    override fun getColorByProgress(p: Float): Color {
//        val x = Math.max(0, Math.min(p.x, size.width))
//        val y = Math.max(0, Math.min(p.y, size.height))

//        val saturation = x.toFloat() / size.width
//        val progress = x.toFloat() / size.width
//        val brightness = 1.0f - y.toFloat() / size.height
        return ColorUtils.lerp(colorA, colorB, alpha, p)
    }


    override fun colorChanged(color: Color, source: Any?) {
//        val hsbValues = Color.RGBtoHSB(color.red, color.green, color.blue, null)
//        setHSBAValue(hsbValues[0], hsbValues[1], hsbValues[2], color.alpha)
        alpha = color.alpha;
        super.colorChanged(color, source)
    }
}


