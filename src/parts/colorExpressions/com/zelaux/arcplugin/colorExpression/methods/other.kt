package com.zelaux.arcplugin.colorExpression.methods

import arc.graphics.Color


import com.zelaux.arcplugin.expressions.resolve.methods.NoParamFunctionExpression

internal object other : ColorRegister() {
    override fun load() {
        "inv"(*noParams) { NoParamFunctionExpression(it,"inv()",Color::inv) }
        "premultiplyAlpha"(*noParams) { NoParamFunctionExpression(it,"premultiplyAlpha",Color::premultiplyAlpha) }
//  TODO      "lerp"(MetaData.Color.PATH) {InvApply(it)}
//        "lerp"(*PsiType.FLOAT * 4) {}
    }
}