package com.zelaux.arcplugin.ui.components

import com.intellij.ui.colorpicker.PICKER_PREFERRED_WIDTH
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.UIUtil
import com.zelaux.arcplugin.ui.colorpicker.AlphaBackgroundImage
import java.awt.*
import javax.swing.JComponent

@Suppress("FINAL_UPPER_BOUND", "FunctionName")
private inline fun <reified T : Int> LocalAlphaBackgroundImage(imageWidth: T, imageHeight: T) = AlphaBackgroundImage(imageWidth, imageHeight, Color.LIGHT_GRAY, Color.GRAY, 6);

@Suppress("FINAL_UPPER_BOUND")
private inline fun <reified T : AlphaBackgroundImage> T.sameValues(width: Int, height: Int): Boolean = sameValues(width, height, Color.LIGHT_GRAY, Color.GRAY, 6)
class ColorComponent(
    color: Color,

    @get:JvmName("getPreferredSize1") @set:JvmName("setPreferredSize1")
    var preferredSize: Dimension = Dimension(PICKER_PREFERRED_WIDTH, 150 / 2),

    @get:JvmName("getMinimumSize1") @set:JvmName("setMinimumSize1")
    var minimumSize: Dimension = Dimension(150, 140 / 2)
) : JComponent() {
    private var alphaBackgroundImage = createImage(0, 0) to LocalAlphaBackgroundImage(0, 0)
    var color = color
        set(value) {
            field = value
            repaint()
        }

    init {
        isOpaque = false
        background = Color.WHITE
    }


    override fun getPreferredSize() = JBUI.size(preferredSize)
    override fun getMaximumSize() = preferredSize

    override fun getMinimumSize(): Dimension = JBUI.size(minimumSize)


    override fun paintComponent(g: Graphics) {
        val component = Rectangle(0, 0, size.width, size.height)
        //region background

        if (color.alpha < 255) {
            if (!alphaBackgroundImage.second.sameValues(size.width, size.height)) {
                val imageProvider = LocalAlphaBackgroundImage(size.width, size.height)
                alphaBackgroundImage = createImage(imageProvider) to imageProvider
            }
            val (background) = alphaBackgroundImage;
            g.drawImage(background, component.x, component.y, null)
        } else {
            g.color = UIUtil.getPanelBackground()
            g.fillRect(0, 0, width, height)
        }
        //endregion
        //region drawColor
        g.color = color
        g.fillRect(0, 0, width, height)
        g.color = UIUtil.getPanelBackground()
        //endregion
    }
}