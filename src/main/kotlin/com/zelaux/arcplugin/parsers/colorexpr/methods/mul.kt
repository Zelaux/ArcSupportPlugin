@file:Suppress("PackageDirectoryMismatch")

package com.zelaux.arcplugin.parsers.colorexpr

import com.intellij.psi.PsiType
import com.zelaux.arcplugin.MetaData
import com.zelaux.arcplugin.parsers.colorexpr.ParserFunc

internal object mul: ParserFunc() {
    override fun load() {
        "mul"(MetaData.Color.PATH) { TODO("mul(Color) parser") }
        "mul"(PsiType.FLOAT) { TODO("mul(Float) parser") }
        "mula"(PsiType.FLOAT) { TODO("mul(Float) parser") }
    }
}