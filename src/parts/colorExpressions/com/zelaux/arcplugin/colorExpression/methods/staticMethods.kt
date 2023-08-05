@file:Suppress("PackageDirectoryMismatch")

package com.zelaux.arcplugin.colorExpression.methods

import com.zelaux.arcplugin.MetaData
import com.zelaux.arcplugin.expressions.resolve.methods.GraysExpression
import com.zelaux.arcplugin.expressions.resolve.methods.SetFloatsExpression
import com.zelaux.arcplugin.expressions.resolve.methods.SetHsvColorExpression
import com.zelaux.arcplugin.expressions.resolve.methods.SetIntExpression
import com.zelaux.arcplugin.psi.PrimitiveType

internal object staticMethods : ColorRegister() {
    override fun load() {
        "HSVtoRGB"(*(PrimitiveType.FLOAT * 3)) { SetHsvColorExpression(it, true, false, "HSVtoRGB(float,float,float)") }
        "HSVtoRGB"(*(PrimitiveType.FLOAT * 4)) { SetHsvColorExpression(it, true, true, "HSVtoRGB(float,float,float,float)") }
        "HSVtoRGB"(*(PrimitiveType.FLOAT.canonicalText * 3 + MetaData.Color.PATH)) { SetHsvColorExpression(it, true, false, "HSVtoRGB(float,float,float,Color)") }

        "rgb888"(*(PrimitiveType.FLOAT * 3)) { SetFloatsExpression(it, false, "rgb888") }
        "rgb888"(PrimitiveType.INT) { SetIntExpression(it, "rgb888(int)", false) }

        "grays"(PrimitiveType.FLOAT) { GraysExpression(it) }

        "RGBtoHSV"(PrimitiveType.FLOAT, PrimitiveType.FLOAT, PrimitiveType.FLOAT) { SetFloatsExpression(it, false, "RGBtoHSV") }
        for (name in arrayOf("rgba4444","rgba8888","argb8888","toFloatBits","toDoubleBits")){
            name(PrimitiveType.FLOAT, PrimitiveType.FLOAT, PrimitiveType.FLOAT, PrimitiveType.FLOAT) { SetFloatsExpression(it, false, name) }
        }
        for(method in arrayOf("ri","gi","bi","ai","rgba8888")){
            method(PrimitiveType.INT) { SetIntExpression(it, method + "(int)", true) }
        }
        //TODO add rgb method
    }
}