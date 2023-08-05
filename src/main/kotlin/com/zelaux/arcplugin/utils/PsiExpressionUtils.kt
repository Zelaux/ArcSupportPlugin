package com.zelaux.arcplugin.utils

import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiExpression
import com.intellij.psi.impl.JavaConstantExpressionEvaluator
import org.jetbrains.uast.UExpression
import org.jetbrains.uast.evaluation.uValueOf

//region get



fun getInt(expr: PsiExpression): Int {
    return (getObject(expr) as? Int) ?: throw WrongExpressionTypeException
}

object WrongExpressionTypeException : RuntimeException()

fun getFloat(expr: PsiExpression): Float {
    return (getObject(expr) as? Number)?.toFloat() ?: throw WrongExpressionTypeException
}

fun getObject(expr: PsiExpression): Any? {
    return JavaConstantExpressionEvaluator.computeConstantExpression(expr, true)
}

//endregion

//region replace
@JvmOverloads
fun replaceInt(expr: PsiExpression, newValue: Int, hex: Boolean = false, alpha: Boolean = true) {

    val factory = JavaPsiFacade.getElementFactory(expr.project)
    if (getInt(expr) != newValue) {
        var text: String?
        if (hex) {
            text = "0x"
            val stringNumber = Integer.toHexString(newValue)
            text += StringUtil.toUpperCase(buildString {
                append("0".repeat(Math.max(0, (if (alpha) 8 else 6) - stringNumber.length)))
                append(stringNumber)
            })
        } else {
            text = newValue.toString()
        }
        expr.replace(factory.createExpressionFromText(text, null))
    }
}


fun replaceFloat(expr: PsiExpression, newValue: Float) {
    val factory = JavaPsiFacade.getElementFactory(expr.project)
    if (getFloat(expr) != newValue) {
        expr.replace(factory.createExpressionFromText(newValue.toString() + "f", null))
    }
}

fun replaceString(expr: PsiExpression, newValue: String) {
    val factory = JavaPsiFacade.getElementFactory(expr.project)
    if (getObject(expr) != newValue) {
        val expressionFromText = factory.createExpressionFromText("\"" + newValue + "\"", null)
        expr.replace(expressionFromText)
    }
}
//endregion