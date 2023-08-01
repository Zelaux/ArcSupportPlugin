package com.zelaux.arcplugin.ui.components

import arc.math.Mathf
import com.intellij.ui.colorpicker.SliderComponent
import java.awt.Graphics2D
import kotlin.math.max
import kotlin.math.min

abstract class FloatSliderComponent(defValue: Float, val fromValue: Float, val toValue: Float, val shiftStep: Float = 0.001f) : SliderComponent<Float>(defValue) {
    override fun knobPositionToValue(knobPosition: Int) =
        Mathf.map(if (sliderWidth > 0) knobPosition.toFloat() / sliderWidth else 0f, fromValue, toValue)


    override fun slide(shift: Int) = Mathf.clamp(value + shift * shiftStep, fromValue, toValue)
    override fun valueToKnobPosition(value: Float) = Mathf.map(value, fromValue, toValue, 0f, sliderWidth.toFloat()).toInt()
}