package com.zelaux.arcplugin.colorExpression.methods


import com.zelaux.arcplugin.MetaData
import com.zelaux.arcplugin.expressions.resolve.methods.SetFloatsExpression
import com.zelaux.arcplugin.expressions.resolve.methods.SetIntExpression
import com.zelaux.arcplugin.expressions.resolve.methods.StaticSetColorExpression
import com.zelaux.arcplugin.psi.PrimitiveType
import org.jetbrains.uast.UCallExpression

internal object `set&constructor` : ColorRegister() {
    override fun load() {
        for ((name, tab) in arrayOf("" to "new Color", "set" to "set")) {
            name(MetaData.Color.PATH) { StaticSetColorExpression(it, UCallExpression::class.java, tab + "(Color)") { (it as UCallExpression).valueArguments[0] } }
            name(PrimitiveType.INT) { SetIntExpression(it, tab + "(int)", true) }
            name(PrimitiveType.FLOAT, PrimitiveType.FLOAT, PrimitiveType.FLOAT) { SetFloatsExpression(it, false, tab) }
            name(PrimitiveType.FLOAT, PrimitiveType.FLOAT, PrimitiveType.FLOAT, PrimitiveType.FLOAT) { SetFloatsExpression(it, false, tab) }
        }

    }
}
