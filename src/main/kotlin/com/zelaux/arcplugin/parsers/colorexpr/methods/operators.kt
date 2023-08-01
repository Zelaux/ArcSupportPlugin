@file:Suppress("PackageDirectoryMismatch")

package com.zelaux.arcplugin.parsers.colorexpr

import com.intellij.psi.PsiType
import com.zelaux.arcplugin.MetaData
import com.zelaux.arcplugin.parsers.colorexpr.ParserFunc
import com.zelaux.arcplugin.parsers.colorexpr.times

internal object operators : ParserFunc() {
    override fun load() {
        "add"(MetaData.Color.PATH) { TODO("add function") }
        "add"(*(PsiType.FLOAT * 4)) { TODO("add function") }
        "add"(*(PsiType.FLOAT * 3)) { TODO("add function") }

        "sub"(MetaData.Color.PATH) { TODO("sub function") }
        "sub"(*(PsiType.FLOAT * 3)) { TODO("sub function") }
        "sub"(*(PsiType.FLOAT * 4)) { TODO("sub function") }
    }
}