
@file:Suppress("JAVA_MODULE_DOES_NOT_EXPORT_PACKAGE")
package com.zelaux.arcplugin.ui.colorpicker.ide

import com.intellij.ui.colorpicker.*

import com.google.common.annotations.VisibleForTesting
import com.intellij.openapi.wm.WindowManager
import com.intellij.ui.picker.ColorListener
import com.intellij.util.ui.JBUI
import sun.awt.AWTPermissions
import java.awt.*
import javax.swing.BoxLayout
import javax.swing.JPanel
import javax.swing.plaf.basic.BasicButtonUI
import kotlin.math.abs
import kotlin.math.roundToInt

private val PANEL_PREFERRED_SIZE = JBUI.size(PICKER_PREFERRED_WIDTH, 80)
private val PANEL_BORDER = JBUI.Borders.empty(4, HORIZONTAL_MARGIN_TO_PICKER_BORDER, 0, HORIZONTAL_MARGIN_TO_PICKER_BORDER)

private val PIPETTE_BUTTON_BORDER = JBUI.Borders.empty()

private val COLOR_INDICATOR_SIZE = JBUI.size(45)
private val COLOR_INDICATOR_BORDER = JBUI.Borders.empty(6)

private val HUE_SLIDER_BORDER = JBUI.Borders.empty(0, 16, 8, 16)

private val ALPHA_SLIDER_BORDER = JBUI.Borders.empty(8, 16, 0, 16)

 class DefaultColorAdjustPanel(private val model: ColorPickerModel,
                               private val pipetteProvider: ColorPipetteProvider,
                               showAlpha: Boolean = false)
    : JPanel(GridBagLayout()), ColorListener {

    private val pipetteButton by lazy {
        val colorPipetteButton = ColorPipetteButton(model, pipetteProvider.createPipette(this@DefaultColorAdjustPanel))
        with (colorPipetteButton) {
            border = PIPETTE_BUTTON_BORDER
            background = PICKER_BACKGROUND_COLOR

            setUI(BasicButtonUI())

            isFocusable = false
            preferredSize = COLOR_INDICATOR_SIZE
        }
        colorPipetteButton
    }

    @VisibleForTesting
    val colorIndicator = ColorIndicator().apply {
        border = COLOR_INDICATOR_BORDER
        preferredSize = COLOR_INDICATOR_SIZE
    }

    @VisibleForTesting
    val hueSlider = HueSliderComponent().apply {
        border = HUE_SLIDER_BORDER
        background = PICKER_BACKGROUND_COLOR

        addListener {
            if (model.color.red == model.color.green && model.color.green == model.color.blue) {
                return@addListener
            }
            val hue = it / 360f
            val hsbValues = Color.RGBtoHSB(model.color.red, model.color.green, model.color.blue, null)
            val rgb = Color.HSBtoRGB(hue, hsbValues[1], hsbValues[2])
            val argb = (model.color.alpha shl 24) or (rgb and 0x00FFFFFF)
            val newColor = if (showAlpha) Color(argb, true) else Color(rgb)
            model.setColor(newColor, this@DefaultColorAdjustPanel)
        }
    }

    @VisibleForTesting
    val alphaSlider = AlphaSliderComponent().apply {
        border = ALPHA_SLIDER_BORDER
        background = PICKER_BACKGROUND_COLOR

        addListener {
            model.setColor(Color(model.color.red, model.color.green, model.color.blue, it), this@DefaultColorAdjustPanel)
        }
    }

    init {
        border = PANEL_BORDER
        background = PICKER_BACKGROUND_COLOR
        preferredSize = PANEL_PREFERRED_SIZE

        // TODO: replace GridBag with other layout.
        val c = GridBagConstraints()

        if (canPickupColorFromDisplay()) {
            c.gridx = 0
            c.gridy = 0
            c.weightx = 0.12
            add(pipetteButton, c)
        }

        c.gridx = 1
        c.gridy = 0
        c.weightx = 0.12
        add(colorIndicator, c)

        c.fill = GridBagConstraints.BOTH
        c.gridx = 2
        c.gridy = 0
        c.weightx = 0.76
        val sliderPanel = JPanel()
        sliderPanel.background = PICKER_BACKGROUND_COLOR
        if (showAlpha) {
            sliderPanel.border = JBUI.Borders.empty()
            sliderPanel.layout = BoxLayout(sliderPanel, BoxLayout.Y_AXIS)
            sliderPanel.add(hueSlider)
            sliderPanel.add(alphaSlider)
        }
        else {
            sliderPanel.border = JBUI.Borders.empty(9, 0)
            sliderPanel.layout = BorderLayout()
            sliderPanel.add(hueSlider)
        }
        add(sliderPanel, c)

        model.addListener(this)
    }

    override fun colorChanged(color: Color, source: Any?) {
        if (colorIndicator.color != color) {
            colorIndicator.color = color
        }

        val hue = Color.RGBtoHSB(color.red, color.green, color.blue, null)[0]
        val hueDegree = (hue * 360).roundToInt()
        // Don't change hueSlider.value when (hueSlider.value, hueDegree) is (0, 360) or (360, 0).
        if (abs(hueSlider.value - hueDegree) != 360) {
            hueSlider.value = hueDegree
        }

        alphaSlider.sliderBackgroundColor = color
        if (alphaSlider.value != color.alpha) {
            alphaSlider.value = color.alpha
        }

        repaint()
    }
}

private fun canPickupColorFromDisplay(): Boolean {
    val alphaModeSupported = WindowManager.getInstance()?.isAlphaModeSupported ?: false
    if (!alphaModeSupported) {
        return false
    }

    return try {
        System.getSecurityManager()?.checkPermission(AWTPermissions.READ_DISPLAY_PIXELS_PERMISSION)
        true
    }
    catch (e: SecurityException) {
        false
    }
}
