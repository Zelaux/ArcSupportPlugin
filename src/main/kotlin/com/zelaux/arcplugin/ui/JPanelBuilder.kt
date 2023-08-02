package com.zelaux.arcplugin.ui

import com.intellij.ui.colorpicker.*

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CustomShortcutSet
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.ui.Gray
import com.intellij.ui.JBColor
import com.intellij.ui.picker.ColorListener
import com.intellij.util.ui.JBUI
import java.awt.Color
import java.awt.Component
import java.awt.Container
import java.awt.Dimension
import java.awt.event.ActionEvent
import javax.swing.*

val PICKER_BACKGROUND_COLOR = JBColor(Gray._252, Gray._64)
val PICKER_TEXT_COLOR = Color(186, 186, 186)


private val PICKER_BORDER = JBUI.Borders.emptyBottom(10)

private const val SEPARATOR_HEIGHT = 5

/**
 * Builder class to help to create customized picker components depends on the requirement.
 */
open class JPanelBuilder(val vertical: Boolean = true) {

    private val componentsToBuild = mutableListOf<JComponent>()
    private var requestFocusWhenDisplay = false
    private var focusCycleRoot = false
    private var focusedComponentIndex = -1
    private val actionMap = mutableMapOf<KeyStroke, Action>()


    /**
     * Add the custom components in to color picker.
     */
    fun addComponent(component: JComponent) = apply { componentsToBuild.add(component) }

    fun addSeparator() = apply {
        val separator = JSeparator(if (vertical) JSeparator.HORIZONTAL else JSeparator.VERTICAL)
        separator.border = JBUI.Borders.empty()
        separator.preferredSize = if (vertical) JBUI.size(PICKER_PREFERRED_WIDTH, SEPARATOR_HEIGHT) else JBUI.size(SEPARATOR_HEIGHT, PICKER_PREFERRED_WIDTH)
        componentsToBuild.add(separator)
    }

    /**
     * Set if Color Picker should request focus when it is displayed.<br>
     *
     * The default value is **false**
     */
    fun focusWhenDisplay(focusWhenDisplay: Boolean) = apply { requestFocusWhenDisplay = focusWhenDisplay }

    /**
     * Set if Color Picker is the root of focus cycle.<br>
     * Set to true to makes the focus traversal inside this Color Picker only. This is useful when the Color Picker is used in an independent
     * window, e.g. a popup component or dialog.<br>
     *
     * The default value is **false**.
     *
     * @see Component.isFocusCycleRoot
     */
    fun setFocusCycleRoot(focusCycleRoot: Boolean) = apply { this.focusCycleRoot = focusCycleRoot }

    /**
     * When getting the focus, focus to the last added component.<br>
     * If this function is called multiple times, only the last time effects.<br>
     * By default, nothing is focused in ColorPicker.
     */
    fun withFocus() = apply { focusedComponentIndex = componentsToBuild.size - 1 }
    fun hasFocus() = focusedComponentIndex != -1
    fun addKeyAction(keyStroke: KeyStroke, action: Action) = apply { actionMap[keyStroke] = action }

    @JvmOverloads
    open fun buildLightCalloutPopup(closedCallback: (() -> Unit)? = null, cancelCallBack: (() -> Unit)? = null, beforeShownCallback: (() -> Unit)? = null) = LightCalloutPopup(buildJPanel(), closedCallback, cancelCallBack, beforeShownCallback)
    open fun buildJPanel(): JPanel {
        if (componentsToBuild.isEmpty()) {
            throw IllegalStateException("The Color Picker should have at least one picking component.")
        }

        val width: Int
        val height: Int
        if (vertical) {
            width = componentsToBuild.maxOf { it.preferredSize.width }
            height = componentsToBuild.sumOf { it.preferredSize.height }
        } else {
            width = componentsToBuild.sumOf { it.preferredSize.width }
            height = componentsToBuild.maxOf { it.preferredSize.height }
        }

        var defaultFocusComponent = componentsToBuild.getOrNull(focusedComponentIndex)
        if (defaultFocusComponent is ColorValuePanel) {
            defaultFocusComponent = defaultFocusComponent.hexField
        }

        val panel = object : JPanel() {
            override fun requestFocusInWindow() = defaultFocusComponent?.requestFocusInWindow() ?: false

            override fun addNotify() {
                super.addNotify()
                if (requestFocusWhenDisplay) {
                    requestFocusInWindow()
                }
            }
        }
        panel.layout = BoxLayout(panel, if (vertical) BoxLayout.Y_AXIS else BoxLayout.X_AXIS)
        panel.border = PICKER_BORDER
        panel.preferredSize = Dimension(width, height)
        panel.background = PICKER_BACKGROUND_COLOR

        for (component in componentsToBuild) {
            panel.add(component)
        }

        panel.isFocusCycleRoot = focusCycleRoot
        if (defaultFocusComponent != null) {
            panel.isFocusTraversalPolicyProvider = true
            panel.focusTraversalPolicy = MyFocusTraversalPolicy(defaultFocusComponent)
        }

        actionMap.forEach { (keyStroke, action) ->
            DumbAwareAction.create { e: AnActionEvent? ->
                action.actionPerformed(ActionEvent(e?.inputEvent, 0, ""))
            }.registerCustomShortcutSet(CustomShortcutSet(keyStroke), panel)
        }
        return panel
    }
}

private class MyFocusTraversalPolicy(val defaultComponent: Component?) : LayoutFocusTraversalPolicy() {
    override fun getDefaultComponent(aContainer: Container?): Component? = defaultComponent
}

private data class ColorListenerInfo(val colorListener: ColorListener, val invokeOnEveryColorChange: Boolean)
