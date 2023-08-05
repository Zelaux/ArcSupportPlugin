@file:Suppress("PackageDirectoryMismatch")

package com.zelaux.arcplugin.colorExpression.methods

import com.intellij.psi.PsiType
import com.zelaux.arcplugin.MetaData
import com.zelaux.arcplugin.parsers.colorexpr.ParserFunc
import com.zelaux.arcplugin.parsers.colorexpr.times
import com.zelaux.arcplugin.psi.PrimitiveType

internal object operators : ParserFunc() {
    override fun load() {
        "add"(MetaData.Color.PATH) { TODO("add function") }
        "add"(*(PrimitiveType.FLOAT * 4)) { TODO("add function") }
        "add"(*(PrimitiveType.FLOAT * 3)) { TODO("add function") }

        "sub"(MetaData.Color.PATH) { TODO("sub function") }
        "sub"(*(PrimitiveType.FLOAT * 3)) { TODO("sub function") }
        "sub"(*(PrimitiveType.FLOAT * 4)) { TODO("sub function") }
    }
}