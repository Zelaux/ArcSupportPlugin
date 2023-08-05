package com.zelaux.arcplugin.colorExpression.drawmethods

import com.zelaux.arcplugin.MetaData
import com.zelaux.arcplugin.expressions.resolve.methods.StaticSetColorExpression
import com.zelaux.arcplugin.psi.PrimitiveType
import org.jetbrains.uast.UCallExpression

object linesmethods:DrawRegister(MetaData.Lines.PATH) {
    override fun load() {
        "stroke"(PrimitiveType.FLOAT.canonicalText,MetaData.Color.PATH){
            StaticSetColorExpression(it, UCallExpression::class.java, "stroke(Color)") {
                (it as UCallExpression).valueArguments[1]
            }
        }
    }
}