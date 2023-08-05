package com.zelaux.arcplugin.colorExpression.methods

import arc.graphics.Color
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Ref
import com.intellij.psi.PsiType
import com.intellij.ui.colorpicker.LightCalloutPopup
import com.intellij.ui.components.Label
import com.zelaux.arcplugin.psi.PrimitiveType
import com.zelaux.arcplugin.ui.JPanelBuilder
import com.zelaux.arcplugin.ui.components.GradientComponent
import com.zelaux.arcplugin.utils.AfterChangeAppliedKey
import com.zelaux.arcplugin.utils.CustomUastTreeUtil
import com.zelaux.arcplugin.utils.UastExpressionUtils
import org.jetbrains.uast.UCallExpression
import org.jetbrains.uast.toUElement

internal object components : ParserFunc() {
    override fun load() {

        for (type in ComponentSetter.Companion.ComponentType.values()) {
            type.name(PrimitiveType.FLOAT) { ComponentSetter(it, type) }
        }
    }
}

@Suppress("MemberVisibilityCanBePrivate")
class ComponentSetter(expression: UCallExpression, val componentType: ComponentType) : ColorCallExpressionParser(expression), OffsetableParser {
    override val tabTitle get() = componentType.tabTitle

    companion object {

        enum class ComponentType(val tabTitle: String, val setter: Color.(Float) -> Unit) {
            r("r()", { r = it }),
            g("g()", { g = it }),
            b("b()", { b = it }),
            a("a()", { a = it }),
        }
    }

    fun currentValue(): Float? {
        return UastExpressionUtils.getFloat(expression.valueArguments[offset])
    }

    private var isCustomValue = false
    private var customValue = 0f
    override fun apply(color: Color): Boolean {
        componentType.setter(
                color, if (isCustomValue) {
            customValue
        } else {
            currentValue() ?: return false
        }
        )
        return true;
    }

    @Suppress("NAME_SHADOWING")
    override fun getTabComponent(project: Project, popupRef: Ref<LightCalloutPopup>, sequence: ColorExpParserSequence, sequenceManager: Ref<ColorExpSequenceManager>, writable: Boolean) =
            JPanelBuilder().apply {
        val (firstColor, secondColor) = sideColors(sequence)
        val label = Label("Has no effect on result")
        if (firstColor == secondColor) {
            addComponent(label)
            return@apply
        }

        addComponent(GradientComponent(firstColor, secondColor, editable = writable).apply {
            preferredSize = sequence.sliderSize
            minimumSize = sequence.sliderSize
            size = sequence.sliderSize;
            val gradientComponent = this
            sequenceManager.get().registerListener(this@ComponentSetter) {
                val (firstColor, secondColor) = sideColors(sequence)
//                this.setProgressColorValue(currentValue()!!)
                colorA = firstColor
                colorB = secondColor
                val shouldLabel = colorA == colorB
                val hasLabel = label.parent != null;
                if (shouldLabel == hasLabel) return@registerListener

                val (from, to) = if (shouldLabel) gradientComponent to label else label to gradientComponent
                swapComponent(from, to)
            }
            this.setProgressColorValue(currentValue()!!)
            var updating = false
            val key= AfterChangeAppliedKey{UastExpressionUtils.getObject(expression.valueArguments[offset])}
            addProgressListener { progress ->

                key.runAfterChange {
                    writeColorAction(project) {
                        val expr = expression.valueArguments
                        UastExpressionUtils.replaceFloat(expr[offset], progress)
//                    element = sourcePsi.toUElement()!!

                        it.runAfterChange {
                            sequenceManager.get().fireUpdate(this@ComponentSetter)
                        }
                        it.newValue(progress)
                    }
                }
            }
        })
        withFocus()
    }

    private fun sideColors(sequence: ColorExpParserSequence): Pair<awtColor, awtColor> {
        isCustomValue = true;
        customValue = 0f;
        val firstColor = sequence.resultColor
        customValue = 1f;
        val secondColor = sequence.resultColor
        isCustomValue = false;
        return Pair(firstColor, secondColor)
    }
}