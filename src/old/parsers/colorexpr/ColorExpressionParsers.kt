package com.zelaux.arcplugin.parsers.colorexpr

import arc.struct.IntMap
import arc.struct.ObjectMap
import arc.struct.Seq
import arc.util.Tmp
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Ref
import com.intellij.psi.PsiType
import com.intellij.ui.colorpicker.LightCalloutPopup
import com.zelaux.arcplugin.parsers.ExpressionParser
import com.zelaux.arcplugin.parsers.ExpressionParserWrapper
import com.zelaux.arcplugin.parsers.ITargetableColorExpParser
import com.zelaux.arcplugin.psi.PrimitiveType
import org.jetbrains.uast.UCallExpression
import org.jetbrains.uast.UElement

sealed class ColorExpressionParser<UAST_TYPE:UElement>(element: UElement,clazz:Class<UAST_TYPE>) :
        ExpressionParser<ColorExpParserSequence, ColorExpSequenceManager, ColorExpressionParserWrapper,UAST_TYPE>(element,clazz) {

    constructor(element: UAST_TYPE) : this(element, element.javaClass)

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

class ColorExpressionParserWrapper(val expressionParser: ColorExpressionParser<out UElement>, override val tabTitle: String) :
        ColorExpressionParser<UElement>(expressionParser.element, expressionParser.clazz as Class<UElement>), ExpressionParserWrapper {
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
        element: UElement,
        private var targetSelector: T.(UElement) -> UElement
) : ColorExpressionParser(element), ITargetableColorExpParser<T> {

    override fun getTargetSelector(): (T, UElement) -> UElement {
        return targetSelector::invoke;
    }
}
/*

sealed class CustomParameterProvider
*/

sealed class ColorCallExpressionParser(expression: UCallExpression) : ColorExpressionParser<UCallExpression>(expression,UCallExpression::class.java) {
    val expression get() = element as UCallExpression
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

fun interface ColorExpressionParserConstructor {
    fun invoke(expression: UCallExpression): ColorExpressionParser
}

private typealias ParamsTypesMap = Seq<Pair<ParamsTypes, ColorExpressionParserConstructor>>

abstract class ParserFunc {
    abstract fun load();

    @JvmName("addMethod")
    operator fun String.invoke(vararg paramsTypes: PrimitiveType, constructor: ColorExpressionParserConstructor) {
        val params = paramsTypes.map { it.canonicalText }.toTypedArray()
        val seq = ParserMap.nameMap[this, { IntMap() }][paramsTypes.size, { Seq() }]
        if (seq.contains { it.first.contentEquals(params) }) throw IllegalArgumentException("method dublication")
        seq.add(params to constructor)
    }

    protected companion object {
        val noParams = emptyArray<String>();
    }

    @JvmName("addMethod")
    operator fun String.invoke(vararg paramsTypes: String, constructor: ColorExpressionParserConstructor) {
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
    fun get(methodName: String, expression: UCallExpression): ColorExpressionParserConstructor? {
        val argumentList = expression.valueArguments
        val value = nameMap[methodName] ?: return null
        val pairs = value[argumentList.size] ?: return null
        val mappedTypes = expression.typeArguments.map { it.canonicalText }.toTypedArray()

        val (_, constructor) = pairs.find { it.first.contentEquals(mappedTypes) } ?: run {
            val typedArray = expression.resolve()?.parameterList?.parameters?.map { it.type.canonicalText }?.toTypedArray()
                    ?: return@run null
            if (BooleanArray(mappedTypes.size) { i ->
                        PrimitiveType.FLOAT.equalsToText(typedArray[i]) && PrimitiveType.INT.equalsToText(mappedTypes[i]) || typedArray[i] == mappedTypes[i]
                    }.contains(false)) {
                return@run null
            }
            pairs.find { it.first.contentEquals(typedArray) }
        } ?: return null
        return constructor

    }
}

