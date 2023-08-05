@file:Suppress("PackageDirectoryMismatch")

package com.zelaux.arcplugin.colorExpression.methods
import com.zelaux.arcplugin.expressions.resolve.methods.SetHsvComponentExpression
import com.zelaux.arcplugin.expressions.resolve.methods.SetHsvColorExpression
import com.zelaux.arcplugin.expressions.resolve.methods.ShiftHsvExpression
import com.zelaux.arcplugin.expressions.resolve.methods.hsv.HsvComponent
import com.zelaux.arcplugin.psi.PrimitiveType
import org.jetbrains.kotlin.lombok.utils.capitalize


internal object hsv : ColorRegister() {
    override fun load() {
        "fromHsv"(*(PrimitiveType.FLOAT * 3)) { SetHsvColorExpression(it, 360f,1f,1f,false,"fromHsv(float,float,float)") }
        for (component in HsvComponent.values()) {
            val shiftName = "shift" + component.name.capitalize()
            shiftName(PrimitiveType.FLOAT) { ShiftHsvExpression(it, "$shiftName(float)", component) }
            component.name(PrimitiveType.FLOAT) { SetHsvComponentExpression(it, "${component.name}(float)", component) }
        }
    }
}