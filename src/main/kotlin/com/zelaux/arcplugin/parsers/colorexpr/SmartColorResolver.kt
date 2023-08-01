package com.zelaux.arcplugin.parsers.colorexpr

import com.intellij.psi.*
import com.zelaux.arcplugin.MetaData
import com.zelaux.arcplugin.utils.PsiUtils

object SmartColorResolver {
    @OptIn(ExperimentalStdlibApi::class)
    @JvmStatic
    fun PsiExpression.resolveColor(): ColorExpParserSequence? {
        if (this !is PsiCallExpression) return null;
        try {
            if (parentCall != null) return null;
            val sequence = ColorExpParserSequence()

            var firstMethod: PsiCallExpression = reciver as? PsiCallExpression ?: return sequence.takeIf {
                !this@resolveColor.resolveColorInner(it) && it.list.isNotEmpty()
            }
            val allPsiCalls = buildList {
                add(0, this@resolveColor)
                add(0, firstMethod)
                while (!firstMethod.reciver.let { it is PsiReferenceExpression } && firstMethod !is PsiNewExpression && firstMethod.reciver is PsiCallExpression) {
                    firstMethod = firstMethod.reciver as PsiCallExpression
                    add(0, firstMethod)
                }
            }

            var pointless = false;
            var prevSize = 0;
            for (i in allPsiCalls.size - 1 downTo 0) {
                prevSize = sequence.list.size;
                if (!allPsiCalls[i].resolveColorInner(sequence)) {
                    if (prevSize != sequence.list.size) {
                        if (pointless) sequence.list[0].pointless = pointless
                        pointless = true;
                    } else {
                        break
                    }

                }
            }
            return sequence.takeIf { it.list.isNotEmpty() }

        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null;
    }

    internal fun PsiCallExpression.resolveColorInner(sequence: ColorExpParserSequence): Boolean {
        val methodName = if (this is PsiNewExpression) "" else methodName.text
        val argumentList: PsiExpressionList = argumentList ?: return false;
        if (!PsiUtils.isType(MetaData.Color.PATH, resolveMethod()?.containingClass)) return false;
        val parser = ParserMap.get(methodName, this)?.invoke(this) ?: return false
        if (!parser.validate())return false;
        sequence.list.add(
            0, parser
        )
//        return methodName !in arrayOf("set", "", "valueOf", "fromHsv", "HSVtoRGB");
        return !parser.resetInner;
    }

}