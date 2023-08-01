package com.zelaux.arcplugin.ui.colorpicker

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CustomShortcutSet
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.ui.JBColor
import com.intellij.ui.colorpicker.*
import com.intellij.ui.picker.ColorListener
import com.intellij.util.Function
import com.intellij.util.ui.JBUI
import com.zelaux.arcplugin.ui.JPanelBuilder
import com.zelaux.arcplugin.ui.colorpicker.ide.DefaultColorAdjustPanel
import java.awt.Color
import java.awt.Component
import java.awt.Container
import java.awt.Dimension
import java.awt.event.ActionEvent
import java.awt.event.KeyEvent
import javax.swing.*

val PICKER_BACKGROUND_COLOR = JBColor(Color(252, 252, 252), Color(64, 64, 64))
val PICKER_TEXT_COLOR = Color(186, 186, 186)

private val PICKER_BORDER = JBUI.Borders.emptyBottom(10)

private const val SEPARATOR_HEIGHT = 5

/**
 * Builder class to help to create customized picker components depends on the requirement.
 */

class ColorPickerJPanelBuilder(private val showAlpha: Boolean = false, private val showAlphaAsPercent: Boolean = true, vertical: Boolean = true) : JPanelBuilder(vertical) {
    constructor(showAlpha: Boolean, showAlphaAsPercent: Boolean) : this(showAlpha, showAlphaAsPercent, true) {

    }

    val model = ColorPickerModel()
    private var originalColor: Color? = null
    private val colorListeners = mutableListOf<ColorListenerInfo>()

    fun setOriginalColor(originalColor: Color?) = apply { this.originalColor = originalColor }

    fun addSaturationBrightnessComponent() = apply { addComponent(SaturationBrightnessComponent(model)) }

    @JvmOverloads
    fun addColorAdjustPanel(colorPipetteProvider: ColorPipetteProvider = GraphicalColorPipetteProvider()) = apply {
        addComponent(DefaultColorAdjustPanel(model, colorPipetteProvider, showAlpha))
    }

    fun addColorValuePanel() = apply { addComponent(ColorValuePanel(model, showAlpha, showAlphaAsPercent)) }

    /**
     * If both [okOperation] and [cancelOperation] are null, [IllegalArgumentException] will be raised.
     */
    fun addOperationPanel(okOperation: ((Color) -> Unit)?, cancelOperation: ((Color) -> Unit)?) = apply {
        addComponent(OperationPanel(model, okOperation, cancelOperation))
        if (cancelOperation != null) {
            addKeyAction(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, true), object : AbstractAction() {
                override fun actionPerformed(e: ActionEvent?) = cancelOperation.invoke(model.color)
            })
        }
    }

    /**
     * Add the custom components in to color picker.
     */
    fun addCustomComponent(provider: ColorPickerComponentProvider) = apply { addComponent(provider.createComponent(model)) }


    fun addColorListener(colorListener: ColorListener) = addColorListener(colorListener, true)

    fun addColorListener(colorListener: ColorListener, invokeOnEveryColorChange: Boolean) = apply {
        colorListeners.add(ColorListenerInfo(colorListener, invokeOnEveryColorChange))
    }

    override fun buildJPanel(): JPanel {
        val c = originalColor
        if (c != null) {
            model.setColor(c, null)
        }
        val jPanel = super.buildJPanel()


        colorListeners.forEach { model.addListener(it.colorListener, it.invokeOnEveryColorChange) }
        return jPanel
    }

    override fun buildLightCalloutPopup(closedCallback: (() -> Unit)?, cancelCallBack: (() -> Unit)?, beforeShownCallback: (() -> Unit)?): LightCalloutPopup {
        return super.buildLightCalloutPopup(closedCallback ?: { model.onClose() }, cancelCallBack ?: { model.onCancel() }, beforeShownCallback)
    }

}

private class MyFocusTraversalPolicy(val defaultComponent: Component?) : LayoutFocusTraversalPolicy() {
    override fun getDefaultComponent(aContainer: Container?): Component? = defaultComponent
}

private data class ColorListenerInfo(val colorListener: ColorListener, val invokeOnEveryColorChange: Boolean)
