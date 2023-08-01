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
import java.awt.image.MemoryImageSource
import javax.swing.JComponent
import kotlin.collections.ArrayList
import kotlin.math.ceil

private val KNOB_COLOR = Color.BLACK
private const val KNOB_RADIUS = 4

@Suppress("FINAL_UPPER_BOUND", "FunctionName")
private inline fun <reified T : Int> LocalAlphaBackgroundImage(imageWidth: T, imageHeight: T) = AlphaBackgroundImage(imageWidth, imageHeight, Color.LIGHT_GRAY, Color.GRAY, 6);

@Suppress("FINAL_UPPER_BOUND")
private inline fun <reified T : AlphaBackgroundImage> T.sameValues(width: Int, height: Int): Boolean = sameValues(width, height, Color.LIGHT_GRAY, Color.GRAY, 6)
fun interface FourGradientSliderListener {
    fun progressChanged(progressX: Float, progressY: Float);
}

open class FourGradientComponent(var leftBottom: Color, var leftTop: Color, var rightTop: Color, var rightBottom: Color, var drawKnob: Boolean = true, var editable: Boolean = true) : JComponent() {
    var progressX = 1f
        private set
    var progressY = 1f
        private set
    var pipetteMode = false
    val robot = Robot()

    private var tmpImage = createImage(0, 0) to FourGradientImageProducer(0, 0, Color.black, Color.white, Color.CYAN, Color.yellow)
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

    private val progressListeners = ArrayList<FourGradientSliderListener>()
    fun addProgressListener(listener: FourGradientSliderListener) {
        progressListeners.add(listener)
    }

    private fun handleMouseEvent(e: MouseEvent) {
        if (!editable) return
        val x = Math.max(0, Math.min(e.point.x, size.width))
        val y = Math.max(0, Math.min(e.point.y, size.height))
        setProgressColorValue(x.toFloat() / size.width, y.toFloat() / size.height)
    }


    override fun getPreferredSize(): Dimension = JBUI.size(PICKER_PREFERRED_WIDTH, 150 / 2)

    override fun getMinimumSize(): Dimension = JBUI.size(150, 140 / 2)

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

        drawBackground(g, component)
        //endregion
        //region gradient
        val image = drawImage(g, component)
        //endregion
        //region knob
        if (drawKnob) {
            val knobX = Math.round(progressX * component.width)
            val knobY = Math.round(progressY * component.height)
//        val knobY = Math.round(component.height * (1.0f - brightness))

            knobColor(image, knobX,knobY, g)
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

    open  fun drawImage(g: Graphics, component: Rectangle): Image {
        if (!tmpImage.second.sameValues(size.width, size.height, leftBottom, leftTop, rightTop, rightBottom) && !(monoColor())) {

            val imageProducer = FourGradientImageProducer(size.width, size.height, leftBottom, leftTop, rightTop, rightBottom);
            tmpImage = createImage(imageProducer) to imageProducer
        }
        val (image) = tmpImage;
        if (monoColor()) {
            g.color = leftBottom
            g.fillRect(0, 0, width, height)
            g.color = UIUtil.getPanelBackground()
        } else {
            g.drawImage(image, component.x, component.y, null)
        }
        return image;
    }

    open  fun drawBackground(g: Graphics, component: Rectangle) {
        if (leftBottom.alpha < 255 || leftTop.alpha < 255 || rightTop.alpha < 255 || rightBottom.alpha < 255) {
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
    }

    protected open fun knobColor(image: Image, knobX: Int, knobY: Int, g: Graphics) {
        if (monoColor()) {
            g.color = if (ColorUtil.isDark(leftBottom)) Color.WHITE else Color.BLACK
        } else if (image is ToolkitImage && image.bufferedImage.width > knobX/* && image.bufferedImage.height > knobY*/) {
            val rgb = image.bufferedImage.getRGB(knobX, knobY)
            //            g.color = if (ColorUtil.isDark(Color(rgb))) Color.WHITE else Color.BLACK
            g.color = if (ColorUtil.isDark(Color(rgb))) Color.WHITE else Color.BLACK
        } else {
            g.color = KNOB_COLOR
        }
    }

    protected fun monoColor() = leftBottom == leftTop && leftTop == rightTop && rightTop == rightBottom


    public fun setProgressColorValue(progressX: Float, progressY: Float) {
        this.progressX = progressX
        this.progressY = progressY
        for (listener in progressListeners) {
            listener.progressChanged(progressX, progressY)
        }
        repaint()
    }
}

private class FourGradientImageProducer(val imageWidth: Int, val imageHeight: Int, val leftBottom: Color, val leftTop: Color, val rightTop: Color, val rightBottom: Color) : MemoryImageSource(imageWidth, imageHeight, null, 0, imageWidth) {

    init {
        if (leftBottom == leftTop && leftTop == rightTop && rightTop == rightBottom) {
            monoColor(imageWidth, imageHeight, leftBottom)
        } else if (leftBottom == leftTop && rightBottom == rightTop) {
            horizontalGradient(imageWidth, imageHeight, leftBottom, rightBottom)
        } else if (leftBottom == rightBottom && leftTop == rightTop) {
            verticalGradient(imageWidth, imageHeight, leftBottom, leftTop)
        } else {
            fourColorGradient(imageWidth, imageHeight, leftBottom, leftTop, rightTop, rightBottom)
        }
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun lerp(a: Int, b: Int, p: Float) = (a + (b - a) * p).toInt();
    fun sameValues(width: Int, height: Int, leftBottom: Color, leftTop: Color, rightTop: Color, rightBottom: Color) =
        imageWidth == width && imageHeight == height && this.leftBottom == leftBottom && this.leftTop == leftTop && this.rightTop == rightTop && this.rightBottom == rightBottom
}