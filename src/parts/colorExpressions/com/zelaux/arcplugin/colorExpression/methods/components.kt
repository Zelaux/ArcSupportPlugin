package com.zelaux.arcplugin.colorExpression.methods


import com.zelaux.arcplugin.expressions.resolve.methods.SetComponentExpression
import com.zelaux.arcplugin.psi.PrimitiveType

internal object components : ColorRegister() {
    override fun load() {

        for (type in SetComponentExpression.ComponentType.values()) {
            type.name(PrimitiveType.FLOAT) { SetComponentExpression(it, type) }
        }
    }
}
