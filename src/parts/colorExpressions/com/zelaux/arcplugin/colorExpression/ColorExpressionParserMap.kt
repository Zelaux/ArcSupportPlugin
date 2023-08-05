package com.zelaux.arcplugin.colorExpression

import arc.struct.IntMap
import arc.struct.ObjectMap
import arc.struct.Seq
import com.zelaux.arcplugin.colorExpression.drawmethods.DrawRegister
import com.zelaux.arcplugin.colorExpression.methods.*
import com.zelaux.arcplugin.expressions.resolve.ArcColorExpression
import com.zelaux.arcplugin.psi.PrimitiveType
import org.jetbrains.uast.UCallExpression

private typealias ParamsTypes = Array<String>

fun interface ColorExpressionParserConstructor {
    fun invoke(expression: UCallExpression): ArcColorExpression
}

private typealias ParamsTypesMap = Seq<Pair<ParamsTypes, ColorExpressionParserConstructor>>

abstract class ParserRegisterDSL(val classQualifier: ClassQualifier) {
    /*constructor(classQualifier: String) : this(ClassQualifier(classQualifier))*/

    abstract fun load()

    inline operator fun <reified T> T.times(int: Int) = Array<T>(int) { this }

    @JvmName("addMethod")
    operator fun String.invoke(vararg paramsTypes: PrimitiveType, constructor: ColorExpressionParserConstructor) {
        val params = paramsTypes.map { it.canonicalText }.toTypedArray()
        internal(params, constructor)
    }


    protected companion object {
        val noParams = emptyArray<String>()
    }

    @JvmName("addMethod")
    operator fun String.invoke(vararg paramsTypes: String, constructor: ColorExpressionParserConstructor) {
        @Suppress("UNCHECKED_CAST")
        internal(paramsTypes as Array<String>, constructor)
    }

    private fun String.internal(paramsTypes: Array<String>, constructor: ColorExpressionParserConstructor) {
        val seq: Seq<Pair<Array<String>, ColorExpressionParserConstructor>> = ColorExpressionParserMap.classMap
                .get(classQualifier, { ObjectMap() })
                .get(this, { IntMap() })
                .get(paramsTypes.size, { Seq() })
        if (seq.contains { it.first.contentEquals(paramsTypes) }) throw IllegalArgumentException("method dublication")
        seq.add(paramsTypes to constructor)
    }
}

@JvmInline
value class ClassQualifier(val qualifier: String)
object ColorExpressionParserMap {
    /**[classQualifier -> [methodName->[paramAmount -> [paramsTypes -> constructor]]]]*/
    val classMap = ObjectMap<ClassQualifier, ObjectMap<String, IntMap<ParamsTypesMap>>>()

    init {
        ColorRegister.register();
        DrawRegister.register();

    }

    fun get(methodName: String, expression: UCallExpression): ColorExpressionParserConstructor? {
        val resolvedMethod = expression.resolve() ?: return null
        val nameMap = run {
            var clazz = resolvedMethod.containingClass
            while (clazz != null) {
                val value = classMap[ClassQualifier(clazz.qualifiedName!!)]
                if (value != null) return@run value
                clazz = clazz.superClass
            }
            return null;
        }
        val argumentList = expression.valueArguments
        val value = nameMap[methodName] ?: return null
        val pairs = value[argumentList.size] ?: return null
        val mappedTypes = expression.valueArguments.map { it.getExpressionType()?.canonicalText }.toTypedArray().takeIf { !it.contains(null) }?:return null;

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

