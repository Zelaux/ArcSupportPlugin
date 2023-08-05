@file:Suppress("PackageDirectoryMismatch")

package com.zelaux.arcplugin.colorExpression.methods

import com.intellij.psi.CommonClassNames
import com.zelaux.arcplugin.MetaData
import com.zelaux.arcplugin.expressions.resolve.methods.ValueOfExpression

internal object valueOf : ColorRegister() {
    override fun load() {
        "valueOf"(MetaData.Color.PATH, CommonClassNames.JAVA_LANG_STRING) { ValueOfExpression(it, "valueOf(Color,String)").also{it.parameterOffset=1} }
        "valueOf"(CommonClassNames.JAVA_LANG_STRING) { ValueOfExpression(it, "valueOf(String)") }
    }
}