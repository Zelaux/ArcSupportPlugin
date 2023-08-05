package com.zelaux.arcplugin.colorExpression.methods

import arc.util.Tmp
import com.intellij.icons.AllIcons
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Ref

import com.intellij.ui.colorpicker.LightCalloutPopup
import com.zelaux.arcplugin.parsers.colorexpr.*
import com.zelaux.arcplugin.parsers.colorexpr.arcColor
import com.zelaux.arcplugin.ui.JPanelBuilder
import com.zelaux.arcplugin.ui.components.ColorComponent
import com.zelaux.arcplugin.ui.components.IconComponent
import org.jetbrains.uast.UCallExpression
import java.awt.Dimension

internal object other : ParserFunc() {
    override fun load() {
        "inv"(*noParams) { InvApply(it) }
        "premultiplyAlpha"(*noParams) { PremultiplyAlphaApply(it) }
//  TODO      "lerp"(MetaData.Color.PATH) {InvApply(it)}
//        "lerp"(*PsiType.FLOAT * 4) {}
    }
}

class PremultiplyAlphaApply(expression: UCallExpression) : ColorCallExpressionParser(expression) {
    override val tabTitle = "premultiplyAlpha()"

    override fun apply(color: arcColor): Boolean {
        color.premultiplyAlpha()
        return true
    }

    override fun getTabComponent(
        project: Project,
        popupRef: Ref<LightCalloutPopup>,
        sequence: ColorExpParserSequence,
        sequenceManager: Ref<ColorExpSequenceManager>,
        writable: Boolean
    ) =
        JPanelBuilder(false).apply {
            sequence.stopColorOn(this@PremultiplyAlphaApply, Tmp.c1.set(0xff))
            val preferredSize = Dimension(sequence.sliderSize.width / 3, sequence.sliderSize.height)

            val originalComp = ColorComponent(Tmp.c1.awtColor(),
                preferredSize = preferredSize,
                minimumSize = preferredSize,
            )
            val newComp = ColorComponent(Tmp.c1.premultiplyAlpha().awtColor(),
                preferredSize = preferredSize,
                minimumSize = preferredSize,
            )
            addComponent(originalComp)
            addComponent(IconComponent(AllIcons.Diff.ArrowRight))
            addComponent(newComp)
            sequenceManager.get().registerListener(this@PremultiplyAlphaApply) {
                sequence.stopColorOn(this@PremultiplyAlphaApply, Tmp.c1.set(0xff))
                originalComp.color = Tmp.c1.awtColor()
                newComp.color = Tmp.c1.premultiplyAlpha().awtColor()
            }
//        addComponent(IconAllIcons.Diff.Arrow)
        }

}

class InvApply(expression: UCallExpression) : ColorCallExpressionParser(expression) {
    override val tabTitle = "inv()"

    override fun apply(color: arcColor): Boolean {
        color.inv()

        return true
    }

    override fun getTabComponent(
        project: Project,
        popupRef: Ref<LightCalloutPopup>,
        sequence: ColorExpParserSequence,
        sequenceManager: Ref<ColorExpSequenceManager>,
        writable: Boolean
    ) = JPanelBuilder(false).apply {
        sequence.stopColorOn(this@InvApply, Tmp.c1.set(0xff))
        val preferredSize = Dimension(sequence.sliderSize.width / 3, sequence.sliderSize.height)
        val originalComp = ColorComponent(Tmp.c1.awtColor(),
            preferredSize = preferredSize,
            minimumSize = preferredSize,
        )
        val newComp = ColorComponent(Tmp.c1.inv().awtColor(),
            preferredSize = preferredSize,
            minimumSize = preferredSize,
        )
        addComponent(originalComp)
        addComponent(IconComponent(AllIcons.Diff.ArrowRight))
        addComponent(newComp)
        sequenceManager.get().registerListener(this@InvApply) {
            sequence.stopColorOn(this@InvApply, Tmp.c1.set(0xff))
            originalComp.color = Tmp.c1.awtColor()
            newComp.color = Tmp.c1.inv().awtColor()
        }
//        addComponent(IconAllIcons.Diff.Arrow)
    }

}
