package com.zelaux.arcplugin

import com.intellij.psi.*
import com.zelaux.arcplugin.marker.color.ElementColorViewerProvider
import com.zelaux.arcplugin.parsers.colorexpr.ColorExpParserSequence
import com.zelaux.arcplugin.parsers.colorexpr.SmartColorResolver
import com.zelaux.arcplugin.parsers.colorexpr.StaticColorParser
import com.zelaux.arcplugin.parsers.colorexpr.arcColor
import com.zelaux.arcplugin.utils.resolveRecursiveField
import org.jetbrains.uast.UCallExpression
import org.jetbrains.uast.UExpression
import org.jetbrains.uast.findContaining
import java.awt.Color

object ArcColorUtils {
    @JvmStatic
    fun resolveColor(expression: UExpression): Color? = resolveColorSequence(expression)?.resultColor

    @JvmStatic
    fun resolveColorSequence(expression: UExpression): ColorExpParserSequence? {
        val sourcePsi = expression.sourcePsi!!
        if (sourcePsi is PsiCallExpression) {
            return SmartColorResolver.run { sourcePsi.resolveColor() }
        }
        val resolve: PsiVariable = sourcePsi.resolveRecursiveField() as? PsiVariable ?: return null
        val initializer = resolve.initializer
        if (initializer is PsiCallExpression) {
//            val firstChild = initializer.firstChild
//            val firstChildpsireferenceNameElement = (firstChild as)?.referenceNameElement
//            val firstChildFirstChild = firstChild?.firstChild
//            val firstChildFirstChildParent = firstChildFirstChild?.parent
//            val eq = firstChildFirstChildParent == initializer
//            val children = initializer.children
//            val context = initializer.context
            return SmartColorResolver.run { initializer.resolveColor() } ?: run {
                val containing = initializer.findContaining(UCallExpression::class.java)
                if (containing != null) {
                    for (extension in ElementColorViewerProvider.EP_NAME.extensions) {
                        val color = extension.getColor(containing)
                        if (color != null) {
                            return@run StaticColorParser(color.arcColor(), sourcePsi as? PsiExpression ?: return@run null).asSequence()
                        }
                    }
                }
                null
            }
            /**/
        }
        return null
    }
}
