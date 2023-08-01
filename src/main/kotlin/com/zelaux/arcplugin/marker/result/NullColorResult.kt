package com.zelaux.arcplugin.marker.result

import com.zelaux.arcplugin.colorViewer.CustomColorViewer
import java.awt.Color
import java.lang.UnsupportedOperationException

class NullColorResult private constructor() : ColorResult() {
    override fun toColorViewer(resultListener: ColorResultListener) = throw UnsupportedOperationException()

    override val color: Color? get() = null

    companion object {
        @JvmField
        val INSTANCE = NullColorResult()
    }
}