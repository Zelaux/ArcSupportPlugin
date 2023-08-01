package com.zelaux.arcplugin.ui.components

import java.awt.Color
import java.awt.image.ColorModel
import java.awt.image.MemoryImageSource
import java.util.*

fun MemoryImageSource.monoColor(imageWidth: Int, imageHeight: Int, color: Color) {
    val pixels = IntArray(imageWidth * imageHeight)
    newPixels(pixels, ColorModel.getRGBdefault(), 0, imageWidth)
//    setAnimated(true)
    Arrays.fill(pixels, color.rgb)
    newPixels()
}

fun MemoryImageSource.horizontalGradient(imageWidth: Int, imageHeight: Int, left: Color, right: Color) {
    val pixels = IntArray(imageWidth * imageHeight)
    newPixels(pixels, ColorModel.getRGBdefault(), 0, imageWidth)
//    setAnimated(true)
    for (index in pixels.indices) {
        val x = index % imageWidth
        val p = x.toFloat() / imageWidth
        pixels[index] = Color(
            lerp(left.red, right.red, p),
            lerp(left.green, right.green, p),
            lerp(left.blue, right.blue, p),
            lerp(left.alpha, right.alpha, p)
        ).rgb
    }
    newPixels()
}

fun MemoryImageSource.verticalGradient(imageWidth: Int, imageHeight: Int, bottom: Color, top: Color) {
    val pixels = IntArray(imageWidth * imageHeight)
    newPixels(pixels, ColorModel.getRGBdefault(), 0, imageWidth)
//    setAnimated(true)
    for (index in pixels.indices) {
        val y = index / imageWidth
        val p = y.toFloat() / imageHeight
        pixels[index] = Color(
            lerp(bottom.red, top.red, p),
            lerp(bottom.green, top.green, p),
            lerp(bottom.blue, top.blue, p),
            lerp(bottom.alpha, top.alpha, p)
        ).rgb
    }
    newPixels()
}

fun MemoryImageSource.fourColorGradient(
    imageWidth: Int, imageHeight: Int, leftBottom: Color, leftTop: Color, rightTop: Color, rightBottom: Color
) {
    val pixels = IntArray(imageWidth * imageHeight)
    newPixels(pixels, ColorModel.getRGBdefault(), 0, imageWidth)
//    setAnimated(true)
    for (index in pixels.indices) {
        val x = index % imageWidth
        val px = x.toFloat() / imageWidth
        val y = index / imageWidth
        val py = y.toFloat() / imageHeight
        pixels[index] = Color(
            lerp(
                lerp(leftBottom.red, leftTop.red, py),
                lerp(rightBottom.red, rightTop.red, py),
                px
            ),
            lerp(
                lerp(leftBottom.green, leftTop.green, py),
                lerp(rightBottom.green, rightTop.green, py),
                px
            ),
            lerp(
                lerp(leftBottom.blue, leftTop.blue, py),
                lerp(rightBottom.blue, rightTop.blue, py),
                px
            ),
            lerp(
                lerp(leftBottom.alpha, leftTop.alpha, py),
                lerp(rightBottom.alpha, rightTop.alpha, py),
                px
            ),
        ).rgb
    }
    newPixels()
}

@Suppress("NOTHING_TO_INLINE")
private inline fun lerp(a: Int, b: Int, p: Float) = (a + (b - a) * p).toInt();