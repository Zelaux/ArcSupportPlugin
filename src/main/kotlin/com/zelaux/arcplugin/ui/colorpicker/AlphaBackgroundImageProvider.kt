package com.zelaux.arcplugin.ui.colorpicker

import java.awt.Color
import java.awt.image.ColorModel
import java.awt.image.MemoryImageSource

class AlphaBackgroundImage(val imageWidth: Int, val imageHeight: Int, val background: Color, val checked: Color, val cellSize: Int) : MemoryImageSource(imageWidth, imageHeight, null, 0, imageWidth) {

    init {
//        val saturation = FloatArray(imageWidth * imageHeight)
        val checkedArray = BooleanArray(imageWidth * imageHeight)

        // create lookup tables
        for (x in 0 until imageWidth) {
            for (y in 0 until imageHeight) {
                val index = x + y * imageWidth
//                saturation[index] = x.toFloat() / imageWidth
                val dy = y / cellSize + ((x / cellSize) % 2)
//                dy
                checkedArray[index] = dy % 2 == 0
//                brightness[index] = 1.0f - y.toFloat() / imageHeight
            }
        }

        val pixels = IntArray(imageWidth * imageHeight)
        newPixels(pixels, ColorModel.getRGBdefault(), 0, imageWidth)
//        setAnimated(true)
        for (index in pixels.indices) {
            val p = checkedArray[index]
            pixels[index] = (if (p) checked else background).rgb
        }
        newPixels()
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun lerp(a: Int, b: Int, p: Float) = (a + (b - a) * p).toInt();
    fun sameValues(width: Int, height: Int, background: Color, checked: Color,cellSize: Int) =
        imageWidth == width && imageHeight == height && this.background == background && this.checked == checked && this.cellSize==cellSize
}