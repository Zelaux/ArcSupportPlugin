package com.zelaux.arcplugin.colorExpression.methods

import arc.graphics.Color
import com.zelaux.arcplugin.colorExpression.ClassQualifier
import com.zelaux.arcplugin.colorExpression.ParserRegisterDSL

abstract class ColorRegister : ParserRegisterDSL(ClassQualifier(Color::class.java.canonicalName)) {
    companion object {
        fun register() {
            arrayOf(
                    components,
                    cpy,
                    hsv,
                    other,
                    `set&constructor`,
                    staticMethods,
                    valueOf,
                    lerp
            ).forEach(ParserRegisterDSL::load)
        }
    }
}