@file:Suppress("JAVA_MODULE_DOES_NOT_EXPORT_PACKAGE")

package com.zelaux.arcplugin.ui.components

import com.intellij.openapi.ui.GraphicsConfig
import com.intellij.openapi.util.registry.Registry
import com.intellij.ui.ColorUtil
import com.intellij.ui.colorpicker.PICKER_PREFERRED_WIDTH
import com.intellij.util.ui.GraphicsUtil
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.UIUtil
import com.zelaux.arcplugin.ui.colorpicker.AlphaBackgroundImage
import sun.awt.image.ToolkitImage
import java.awt.*
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.image.ColorModel
import java.awt.image.MemoryImageSource
import java.util.*
import javax.swing.JComponent
import kotlin.collections.ArrayList
import kotlin.math.ceil

private val KNOB_COLOR = Color.BLACK
private const val KNOB_RADIUS = 4

@Suppress("FINAL_UPPER_BOUND", "FunctionName")
private inline fun <reified T : Int> LocalAlphaBackgroundImage(imageWidth: T, imageHeight: T) = AlphaBackgroundImage(imageWidth, imageHeight, Color.LIGHT_GRAY, Color.GRAY, 6);

@Suppress("FINAL_UPPER_BOUND")
private inline fun <reified T : AlphaBackgroundImage> T.sameValues(width: Int, height: Int): Boolean = sameValues(width, height, Color.LIGHT_GRAY, Color.GRAY, 6)
fun interface GradientSliderListener {
    fun progressChanged(progress: Float);
}

open class GradientComponent(var colorA: Color, var colorB: Color, var drawKnob: Boolean = true, var editable: Boolean = true) : JComponent() {
    var progress = 1f
        private set
    var pipetteMode = false
    val robot = Robot()

    private var tmpImage = createImage(0, 0) to GradientImageProducer(0, 0, Color.black, Color.white)
    private var alphaBackgroundImage = createImage(0, 0) to LocalAlphaBackgroundImage(0, 0)

    init {
        isOpaque = false
        background = Color.WHITE

        val mouseAdapter = object : MouseAdapter() {
            override fun mousePressed(e: MouseEvent) {
                handleMouseEvent(e)
            }

            override fun mouseDragged(e: MouseEvent) {
                handleMouseEvent(e)
            }
        }
        addMouseListener(mouseAdapter)
        addMouseMotionListener(mouseAdapter)
    }

    private val progressListeners = ArrayList<GradientSliderListener>()
    fun addProgressListener(listener: GradientSliderListener) {
        progressListeners.add(listener)
    }

    private fun handleMouseEvent(e: MouseEvent) {
        if (!editable) return
        val x = Math.max(0, Math.min(e.point.x, size.width))
        setProgressColorValue(x.toFloat() / size.width)
    }

    @JvmField
    @get:JvmName("getPreferredSize1")
    @set:JvmName("setPreferredSize1")
    var preferredSize = Dimension(PICKER_PREFERRED_WIDTH, 150 / 2)
    @JvmField
    @get:JvmName("setMinimumSize1")
    @set:JvmName("getMinimumSize1")
    var minimumSize = Dimension(150, 140 / 2)
    override fun getPreferredSize(): Dimension = JBUI.size(preferredSize)

    override fun getMinimumSize(): Dimension = JBUI.size(minimumSize)

    fun paintPipetteMode(graphics: Graphics) {
        graphics.color = parent.background
        graphics.fillRect(0, 0, width, height)
        val g = graphics.create() as Graphics2D
        val p = MouseInfo.getPointerInfo().location
        val size = width / 21.0
        val img = robot.createMultiResolutionScreenCapture(Rectangle(p.x - 10, p.y - 5, 21, 11))
        val image = img.resolutionVariants.last()
        val iW = image.getWidth(null)
        val iH = image.getHeight(null)
        g.scale(width / 21.0, width / 21.0)
        g.drawImage(image, -((iW - 21) / 2.0).toInt(), -ceil((iH - 11) / 2.0).toInt(), null)
        g.dispose()
        val xx = ceil(size * 10).toInt()
        val yy = ceil(size * 5).toInt()
        graphics.color = Color.white
        graphics.drawRect(xx, yy, (size - 1).toInt(), (size - 1).toInt())
        graphics.color = Color.black
        graphics.drawRect(xx + 1, yy + 1, (size - 3).toInt(), (size - 3).toInt())
    }

    override fun paintComponent(g: Graphics) {
        if (Registry.`is`("ide.color.picker.new.pipette") && pipetteMode) {
            paintPipetteMode(g)
            return
        }
        val component = Rectangle(0, 0, size.width, size.height)
        //region background

        if (colorA.alpha < 255 || colorB.alpha < 255) {
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
        //region gradient
        if (!tmpImage.second.sameValues(size.width, size.height, colorA, colorB) && colorA != colorB) {

            val imageProducer = GradientImageProducer(size.width, size.height, colorA, colorB);
            tmpImage = createImage(imageProducer) to imageProducer
        }
        val (image) = tmpImage;
        if (colorA == colorB) {
            g.color = colorA
            g.fillRect(0, 0, width, height)
            g.color = UIUtil.getPanelBackground()
        } else {
            g.drawImage(image, component.x, component.y, null)
        }
        //endregion
        //region knob
        if (drawKnob) {
            val knobX = Math.round(progress * component.width)
//        val knobY = Math.round(component.height * (1.0f - brightness))

            knobColor(image, knobX, g)
            val config: GraphicsConfig = GraphicsUtil.setupAAPainting(g)
            val rad: Int = JBUI.scale(KNOB_RADIUS)
//        val rad2 = JBUI.scale(KNOB_RADIUS * 2)

            g.fillRect(knobX - rad, 0, 1, height)
            g.fillRect(knobX + rad, 0, 1, height)
//        g.fillRect(knobX-rad, 0, rad2, height)
            /*g.drawOval(knobX - rad,
                    knobY - rad,
                    rad2,
                    rad2
                )*/
            config.restore()
        }
        //endregion
    }

    protected open fun knobColor(image: Image, knobX: Int, g: Graphics) {
        if (colorA == colorB) {
            g.color = if (ColorUtil.isDark(colorA)) Color.WHITE else Color.BLACK
        } else if (image is ToolkitImage && image.bufferedImage.width > knobX/* && image.bufferedImage.height > knobY*/) {
            val rgb = image.bufferedImage.getRGB(knobX, 0)
            //            g.color = if (ColorUtil.isDark(Color(rgb))) Color.WHITE else Color.BLACK
            g.color = if (ColorUtil.isDark(Color(rgb))) Color.WHITE else Color.BLACK
        } else {
            g.color = KNOB_COLOR
        }
    }


    public fun setProgressColorValue(p: Float) {
       if (progress!=p){
           for (listener in progressListeners) {
               listener.progressChanged(p)
           }
       }
        progress = p
        repaint()
    }
}

private class GradientImageProducer(val imageWidth: Int, val imageHeight: Int, val colorA: Color, val colorB: Color) : MemoryImageSource(imageWidth, imageHeight, null, 0, imageWidth) {

    init {
        if (colorA == colorB) {
            monoColor(imageWidth, imageHeight, colorA)
        } else {
            horizontalGradient(imageWidth, imageHeight, colorA, colorB)
        }
    }

    fun sameValues(width: Int, height: Int, colorA: Color, colorB: Color) =
        imageWidth == width && imageHeight == height && this.colorA == colorA && this.colorB == colorB
}