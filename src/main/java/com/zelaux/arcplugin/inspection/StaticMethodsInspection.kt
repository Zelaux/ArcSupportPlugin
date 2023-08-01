package com.zelaux.arcplugin.inspection

import arc.struct.*
import com.intellij.codeInsight.daemon.impl.analysis.JavaGenericsUtil
import com.intellij.codeInspection.*
import com.intellij.psi.*
import com.intellij.uast.*
import com.zelaux.arcplugin.utils.*
import com.zelaux.arcplugin.utils.PsiUtils.isType
import org.jetbrains.uast.*
import org.jetbrains.uast.visitor.*

class StaticMethodsInspection : AbstractBaseUastLocalInspectionTool() {
    override fun getProblemElement(psiElement: PsiElement): PsiNamedElement? {
        return super.getProblemElement(psiElement)
    }

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return UastVisitorAdapter(object : AbstractUastNonRecursiveVisitor() {
            private val visited: MutableSet<PsiElement?> = HashSet()
            override fun visitElement(node: UElement): Boolean {
                return false
            }

            override fun visitCallExpression(node: UCallExpression): Boolean {
//                System.out.println("afterVisitCallExpression: "+node );
                if (visited.add(node.sourcePsi)) addDescriptors(checkCallExpression(node, holder.manager, isOnTheFly))
                return false
            }

            private fun addDescriptors(descriptors: Array<ProblemDescriptor>?) {
                if (descriptors != null) {
                    for (descriptor in descriptors) {
                        holder.registerProblem(descriptor)
                    }
                }
            }
        }, true)
    }

    private fun checkCallExpression(expression: UCallExpression, manager: InspectionManager, isOnTheFly: Boolean): Array<ProblemDescriptor> {
        for (descriptor in descriptors) {
            if (!expression.isStaticMethod(descriptor.clazz, descriptor.name)) {
                continue
            }
            val typeArguments = expression.valueArguments
            val problemsHolder = ProblemsHolder(manager, expression.sourcePsi!!.containingFile, isOnTheFly)
            val extra = typeArguments.size % descriptor.parameterStrategies.size
            val error = ProblemHighlightType.ERROR
            if (extra != 0) {
                val last = typeArguments[typeArguments.size - 1]
                val missing = descriptor.parameterStrategies.size - extra
                var message = "You missed $missing parameters"
                if (missing == 1) {
                    message = "You missed 1 parameter"
                }
                problemsHolder.registerProblem(manager.createProblemDescriptor(last.sourcePsi!!, message, isOnTheFly, LocalQuickFix.EMPTY_ARRAY, error))
            }
            val sourcePsi = expression.sourcePsi
            val callExpression = sourcePsi as PsiCallExpression?
            val generics = callExpression!!.typeArguments
            callExpression

            for (i in typeArguments.indices) {
                val strategy = descriptor.parameterStrategies[i % descriptor.parameterStrategies.size]
                if (strategy == ParameterStrategyType.IgnoreType) continue
                val psiType = typeArguments[i].getExpressionType()
                val value = typeArguments[i]
                if (strategy is ParameterStrategyType.StaticType) {
                    var valid = false

                    if (psiType == null) continue
                    val allTypes = if (psiType is PsiPrimitiveType) {
                        psiType.getBoxedType(sourcePsi!!)!!.superTypes.toMutableList().also { it.add(0, psiType) }
                    } else {
                        psiType.superTypes.toMutableList().also { it.add(0, psiType) }
                    }
                    for (datum in strategy.classes) {

                        for (type in allTypes) {
                            if (isType(datum.name, type) || !datum.isPrimitive && i % descriptor.parameterStrategies.size != 0 && type === PsiType.NULL) {
                                valid = true
                                break
                            }
                        }
                        if (valid) break
                    }
                    if (!valid) {
                        problemsHolder.registerProblem(manager.createProblemDescriptor(
                            value.sourcePsi!!,
                            "Wrong type(shoud be " + strategy.classes.joinToString(" or ") { it.name } + ")",
                            isOnTheFly,
                            LocalQuickFix.EMPTY_ARRAY,
                            error))
                    }
                    continue
                }
                if (generics.isEmpty()) continue
                if (strategy is ParameterStrategyType.FirstGeneric) {
                    if (generics[0].canonicalText != psiType!!.canonicalText) {
                        problemsHolder.registerProblem(manager.createProblemDescriptor(value.sourcePsi!!, "Wrong type(shoud be " + generics[0].canonicalText + ")", isOnTheFly, LocalQuickFix.EMPTY_ARRAY, error))
                    }
                    continue
                }
                if (strategy is ParameterStrategyType.SecondGeneric) {
                    if (generics[1].canonicalText != psiType!!.canonicalText) {
                        problemsHolder.registerProblem(manager.createProblemDescriptor(value.sourcePsi!!, "Wrong type(shoud be " + generics[1].canonicalText + ")", isOnTheFly, LocalQuickFix.EMPTY_ARRAY, error))
                    }
                    continue
                }
            }
            return problemsHolder.resultsArray
        }
        return ProblemDescriptor.EMPTY_ARRAY
    }

    sealed class ParameterStrategyType {
        object IgnoreType : ParameterStrategyType()
        object FirstGeneric : ParameterStrategyType()
        class StaticType internal constructor(vararg classes: ClassDescriptor) : ParameterStrategyType() {
            internal var classes: Array<out ClassDescriptor>

            init {
                this.classes = classes;
            }

            constructor(vararg classes: Class<*>?) : this(*classes.let {
                Array<ClassDescriptor>(it.size) { i ->
                    ClassDescriptor(it[i]!!.canonicalName, it[i]!!.isPrimitive)
                }
            })
        }

        object SecondGeneric : ParameterStrategyType()
    }

    private class MethodDescriptor {
        val name: String
        val clazz: String
        val parameterStrategies: Array<out ParameterStrategyType>

        constructor(name: String, clazz: Class<*>, vararg parameterStrategies: ParameterStrategyType) {
            this.name = name
            this.clazz = clazz.canonicalName
            this.parameterStrategies = parameterStrategies
        }

        constructor(name: String, clazz: String, vararg parameterStrategies: ParameterStrategyType) {
            this.name = name
            this.clazz = clazz
            this.parameterStrategies = parameterStrategies
        }
    }

    internal class ClassDescriptor @JvmOverloads constructor(var name: String, var isPrimitive: Boolean = false)
    companion object {
        private val descriptors = arrayOf(
            MethodDescriptor(
                "of", IntIntMap::class.java,
                ParameterStrategyType.IgnoreType, ParameterStrategyType.IgnoreType
            ),

            MethodDescriptor(
                "of", IntMap::class.java,
                ParameterStrategyType.StaticType(Int::class.javaPrimitiveType, Char::class.javaPrimitiveType),
                ParameterStrategyType.FirstGeneric
            ),

            MethodDescriptor(
                "of",
                ObjectMap::class.java,
                ParameterStrategyType.FirstGeneric,
                ParameterStrategyType.SecondGeneric
            ),
            MethodDescriptor(
                "of",
                OrderedMap::class.java,
                ParameterStrategyType.FirstGeneric,
                ParameterStrategyType.SecondGeneric
            ),
            MethodDescriptor(
                "of",
                StringMap::class.java,
                ParameterStrategyType.StaticType(String::class.java),
                ParameterStrategyType.StaticType(String::class.java)
            )
        )
    }
}