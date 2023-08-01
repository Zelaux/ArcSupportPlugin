package com.zelaux.arcplugin.parsers.colorexpr

import arc.graphics.Color
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Ref
import com.intellij.psi.PsiCallExpression
import com.intellij.psi.PsiType
import com.intellij.ui.colorpicker.LightCalloutPopup
import com.intellij.ui.components.Label
import com.zelaux.arcplugin.parsers.colorexpr.*
import com.zelaux.arcplugin.parsers.colorexpr.OffsetableParser
import com.zelaux.arcplugin.parsers.colorexpr.awtColor
import com.zelaux.arcplugin.parsers.colorexpr.swapComponent
import com.zelaux.arcplugin.parsers.colorexpr.writeColorAction
import com.zelaux.arcplugin.ui.JPanelBuilder
import com.zelaux.arcplugin.ui.components.GradientComponent
import com.zelaux.arcplugin.utils.getFloat
import com.zelaux.arcplugin.utils.replaceFloat

internal object components : ParserFunc() {
    override fun load() {

        for (type in ComponentSetter.Companion.ComponentType.values()) {
            type.name(PsiType.FLOAT) { ComponentSetter(it, type) }
        }
    }
}

@Suppress("MemberVisibilityCanBePrivate")
class ComponentSetter(expression: PsiCallExpression, val componentType: ComponentType) : ColorCallExpressionParser(expression), OffsetableParser {
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
        return getFloat(expression.argumentList?.expressions?.get(offset) ?: return null)
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
    override fun getTabComponent(project: Project, popupRef: Ref<LightCalloutPopup>, sequence: ColorExpParserSequence, sequenceManager: Ref<ColorExpSequenceManager>, writable: Boolean) = JPanelBuilder().apply {
        val (firstColor, secondColor) = sideColors(sequence)
        val label = Label("Has no effect on result")
        if (firstColor == secondColor) {
            addComponent(label)
            return@apply
        }

        addComponent(GradientComponent(firstColor, secondColor, editable = writable).apply {
            preferredSize = sequence.sliderSize
            minimumSize = sequence.sliderSize
            size=sequence.sliderSize;
            val gradientComponent = this
            sequenceManager.get().registerListener(this@ComponentSetter) {
                val (firstColor, secondColor) = sideColors(sequence)
//                this.setProgressColorValue(currentValue()!!)
                colorA = firstColor
                colorB = secondColor
                val shouldLabel = colorA == colorB
                val hasLabel = label.parent != null;
                if (shouldLabel == hasLabel) return@registerListener

                val (from,to)= if (shouldLabel) gradientComponent to label else label to gradientComponent
                swapComponent(from, to)
            }
            this.setProgressColorValue(currentValue()!!)
            var updating=false
            addProgressListener { progress ->

                if (updating)return@addProgressListener
                updating=true;
                writeColorAction(project) {
                    val expr = expression.argumentList!!.expressions
                    replaceFloat(expr[offset], progress)
                    sequenceManager.get().fireUpdate(this@ComponentSetter)
                    updating=false;
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