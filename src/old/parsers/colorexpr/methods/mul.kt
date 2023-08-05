@file:Suppress("PackageDirectoryMismatch")

package com.zelaux.arcplugin.colorExpression.methods

import com.intellij.psi.PsiType
import com.zelaux.arcplugin.MetaData
import com.zelaux.arcplugin.parsers.colorexpr.ParserFunc
import com.zelaux.arcplugin.psi.PrimitiveType

internal object mul: ParserFunc() {
    override fun load() {
        "mul"(MetaData.Color.PATH) { TODO("mul(Color) parser") }
        "mul"(PrimitiveType.FLOAT) { TODO("mul(Float) parser") }
        "mula"(PrimitiveType.FLOAT) { TODO("mul(Float) parser") }
    }
}