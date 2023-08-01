package com.zelaux.arcplugin.parsers.colorexpr

import arc.struct.IntMap
import arc.struct.ObjectMap
import arc.struct.Seq
import arc.util.Tmp
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Ref
import com.intellij.psi.PsiCallExpression
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiType
import com.intellij.ui.colorpicker.LightCalloutPopup
import com.zelaux.arcplugin.parsers.*

sealed class ColorExpressionParser(element: PsiElement) :
    ExpressionParser<ColorExpParserSequence, ColorExpSequenceManager, ColorExpressionParserWrapper>(element) {


    override val resetInner: Boolean get() = this is StartPointParser;
    override val isStatic: Boolean get() = false;

    override fun wrap(tabTitle: String) = ColorExpressionParserWrapper(this, tabTitle)
    override fun asSequence() = ColorExpParserSequence().also { it.list.add(this) }
    abstract fun apply(color: arcColor): Boolean
    /*  abstract fun getTabComponent(
          project: Project,
          popupRef: Ref<LightCalloutPopup>,
          sequence: ColorExpParserSequence,
          sequenceManager: Ref<ColorExpSequenceManager>,
          writable: Boolean
      ): JPanelBuilder*/

    override fun validate() = true
}

class ColorExpressionParserWrapper(val expressionParser: ColorExpressionParser, override val tabTitle: String) : ColorExpressionParser(expressionParser.element), ExpressionParserWrapper {
    override val resetInner: Boolean
        get() = expressionParser.resetInner
    override val isStatic: Boolean
        get() = expressionParser.isStatic


    override fun apply(color: arcColor) = expressionParser.apply(color)

    override fun getTabComponent(
        project: Project,
        popupRef: Ref<LightCalloutPopup>,
        sequence: ColorExpParserSequence,
        sequenceManager: Ref<ColorExpSequenceManager>,
        writable: Boolean
    ) = expressionParser.getTabComponent(project, popupRef, sequence, sequenceManager, writable)
}

sealed class TargetableColorExpParser<T : TargetableColorExpParser<T>>(
    element: PsiElement,
    override var targetSelector: T.(PsiElement) -> PsiElement
) : ColorExpressionParser(element), ITargetableColorExpParser<T> {
}
/*

sealed class CustomParameterProvider
*/

sealed class ColorCallExpressionParser(expression: PsiCallExpression) : ColorExpressionParser(expression) {
    val expression = element as PsiCallExpression
}

internal interface StartPointParser {
}

internal interface OffsetableParser {
    fun offset() = (this as ColorExpressionParser)._P_R_I_V_A_T_E__O_F_F_S_E_T
    fun <T : ColorExpressionParser> offset(offset: Int): T {
        (this as ColorExpressionParser)._P_R_I_V_A_T_E__O_F_F_S_E_T = offset
        @Suppress("UNCHECKED_CAST")
        return this as T
    }
}

internal val <T> T.selfAwtColor where T : StartPointParser, T : ColorExpressionParser
    get() = Tmp.c1.set(0xff).apply { apply(this) }.awtColor()
internal val <T> T.offset where T : OffsetableParser, T : ColorExpressionParser
    get() = _P_R_I_V_A_T_E__O_F_F_S_E_T
/*internal var <T> T.offset where T : OffsetableParser, T : ColorExpressionParser
    get() = privateOffset
    set(value) = */

//@JvmName("MethodSetColor")


private typealias ParamsTypes = Array<String>
private typealias Constructor = (PsiCallExpression) -> ColorExpressionParser

private typealias ParamsTypesMap = Seq<Pair<ParamsTypes, Constructor>>

abstract class ParserFunc {
    abstract fun load();

    @JvmName("addMethod")
    operator fun String.invoke(vararg paramsTypes: PsiType, constructor: Constructor) {
        val params = paramsTypes.map { it.canonicalText }.toTypedArray()
        val seq = ParserMap.nameMap[this, { IntMap() }][paramsTypes.size, { Seq() }]
        if (seq.contains { it.first.contentEquals(params) }) throw IllegalArgumentException("method dublication")
        seq.add(params to constructor)
    }

    protected companion object {
        val noParams = emptyArray<String>();
    }

    @JvmName("addMethod")
    operator fun String.invoke(vararg paramsTypes: String, constructor: Constructor) {
        @Suppress("UNCHECKED_CAST")
        ParserMap.nameMap[this, { IntMap() }][paramsTypes.size, { Seq() }].add(paramsTypes as ParamsTypes to constructor)
    }
}

object ParserMap {
    val nameMap = ObjectMap<String, IntMap<ParamsTypesMap>>()

    init {
        arrayOf(
            components,
            cpy,
            hsv,
            other,
            `set&constructor`,
            staticMethods,
            valueOf,
            gradient
        ).forEach(ParserFunc::load)

    }


    fun get(methodName: String, expression: PsiCallExpression): Constructor? {
        val argumentList = expression.argumentList ?: return null
        val value = nameMap[methodName] ?: return null
        val pairs = value[argumentList.expressionCount] ?: return null
        val mappedTypes = argumentList.expressionTypes.map { it.canonicalText }.toTypedArray()

        val (_, constructor) = pairs.find { it.first.contentEquals(mappedTypes) } ?: run {
            val typedArray = expression.resolveMethod()?.parameterList?.parameters?.map { it.type.canonicalText }?.toTypedArray() ?: return@run null
            if (BooleanArray(mappedTypes.size) { i ->
                    PsiType.FLOAT.equalsToText(typedArray[i]) && PsiType.INT.equalsToText(mappedTypes[i]) || typedArray[i] == mappedTypes[i]
                }.contains(false)) {
                return@run null
            }
            pairs.find { it.first.contentEquals(typedArray) }
        } ?: return null
        return constructor

    }
}

