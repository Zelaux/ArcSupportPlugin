package com.zelaux.arcplugin.parsers.colorexpr

import arc.graphics.Color
import arc.struct.Seq
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.util.Ref
import com.intellij.psi.*
import com.intellij.psi.util.PsiUtil
import com.intellij.ui.colorpicker.LightCalloutPopup
import com.intellij.ui.components.JBLabel
import com.intellij.ui.scale.JBUIScale
import com.intellij.util.ui.ColorIcon
import com.intellij.util.ui.FormBuilder
import com.zelaux.arcplugin.ArcColorUtils
import com.zelaux.arcplugin.ui.JPanelBuilder
import com.zelaux.arcplugin.utils.RelodableLazy
import com.zelaux.arcplugin.utils.relodableLazy
import com.zelaux.arcplugin.utils.resolveElement
import com.zelaux.arcplugin.utils.resolveRecursiveField
import org.jetbrains.uast.UExpression
import org.jetbrains.uast.toUElement
import java.awt.Component
import javax.swing.JList
import javax.swing.ListCellRenderer
import javax.swing.SwingConstants

class StaticSetColorParser(expression: PsiElement, targetSelector: StaticSetColorParser.(PsiElement) -> PsiElement) : TargetableColorExpParser<StaticSetColorParser>(expression, targetSelector), OffsetableParser, StartPointParser {

    constructor(callExpression: PsiCallExpression) : this(callExpression, {
        (it as PsiCallExpression).argumentList?.expressions?.get(offset)!!
    })

    override val tabTitle = (if (expression is PsiNewExpression) "new Color" else "set") + "(Color)"
    override val resetInner = true
    override val isStatic = true
    override fun validate() = color != null

    val color: Color? by relodableLazy {
        val targetExpression = try {
            targetExpression
        } catch (e: Exception) {
            e.printStackTrace(); return@relodableLazy null;
        }
        val uExpression = targetExpression.toUElement(UExpression::class.java) ?: return@relodableLazy null
        arcColor().set(ArcColorUtils.resolveColor(uExpression) ?: return@relodableLazy null)
    }

    override fun apply(color: arcColor): Boolean {
        if (this.color == null) return false
        color.set(this.color)
        return true
    }

    public val fieldsAmount get() = fields.size
    private val fields: Array<PsiFieldWithColor> by lazy {
        if (color == null) return@lazy emptyArray()
        val currentField = targetExpression.resolveRecursiveField() ?: return@lazy emptyArray()
        val seq = Seq<PsiFieldWithColor>()

        for (field in (currentField.containingClass ?: return@lazy emptyArray()).fields) {
            val initializer = field.initializer ?: continue
            val resolvedColor = ArcColorUtils.resolveColor(initializer.toUElement(UExpression::class.java) as UExpression) ?: continue
            seq.add(PsiFieldWithColor(field, resolvedColor))
        }
        seq.toArray(PsiFieldWithColor::class.java)
//        TODO()
    }

    private fun psiFieldWithColors(): Array<PsiFieldWithColor> {
        return emptyArray()
    }


    override fun getTabComponent(project: Project, popupRef: Ref<LightCalloutPopup>, sequence: ColorExpParserSequence, sequenceManager: Ref<ColorExpSequenceManager>, writable: Boolean): JPanelBuilder {
        if (fields.size > 1 && writable) {

            val psiExpression = targetExpression
            val isFieldSetter = psiExpression.resolveElement() is PsiField
            val currentField = psiExpression.resolveRecursiveField()!!
            if (isFieldSetter) return JPanelBuilder().apply {
//                addComponent(RecentColorsPalette())
                addComponent(ComboBox(fields).also {
                    it.setMinimumAndPreferredWidth(sequence.sliderSize.width)
//                    it.size=
                    it.selectedIndex = fields.indexOfFirst { field -> field.psiField.name == currentField.name };
                    it.renderer = PsiFieldWithColorRenderer()
                    it.addItemListener { event ->
                        if (event.stateChange!=1)return@addItemListener
                        val item = event.item as PsiFieldWithColor
                        val factory: PsiElementFactory = PsiElementFactory.getInstance(project)
                        val targetExpression = targetExpression
                        val newExp = factory.createExpressionFromText(PsiUtil.getMemberQualifiedName(item.psiField)!!, targetExpression.parent)

                        writeColorAction(project) {
                            replaceTarget(targetExpression,newExp)

                            RelodableLazy.resetValue(this@StaticSetColorParser, this@StaticSetColorParser::color)
                            sequenceManager.get().fireUpdate(this@StaticSetColorParser)
                        }
//                        item.psiField.name
                    }
                })
            }
        }
        //TODO color selection
        return MonoColorJPanelBuilder(sequence, selfAwtColor) { colorConsumer ->
            sequenceManager.get().registerListener(this) {
                colorConsumer(selfAwtColor)
            }
        }
    }

}

class StaticColorParser(val color: arcColor, expression: PsiExpression) : ColorExpressionParser(expression) {
    override val tabTitle = (if (expression is PsiNewExpression) "new Color" else "set") + "(Color)"
    override val resetInner = true
    override val isStatic = true


    override fun apply(color: arcColor): Boolean {
        color.set(this.color)
        return true
    }


    private fun psiFieldWithColors(): Array<PsiFieldWithColor> {
        return emptyArray()
    }

    override fun getTabComponent(project: Project, popupRef: Ref<LightCalloutPopup>, sequence: ColorExpParserSequence, sequenceManager: Ref<ColorExpSequenceManager>, writable: Boolean): JPanelBuilder {
        //TODO color selection
        return MonoColorJPanelBuilder(sequence, color.awtColor()) { colorConsumer ->
            sequenceManager.get().registerListener(this) {
                colorConsumer(this.color.awtColor())
            }
        }
    }

}

private class PsiFieldWithColor(val psiField: PsiField, val color: awtColor)
private class PsiFieldWithColorRenderer : ListCellRenderer<PsiFieldWithColor> {
    override fun getListCellRendererComponent(list: JList<out PsiFieldWithColor>, value: PsiFieldWithColor, index: Int, isSelected: Boolean, cellHasFocus: Boolean): Component {
        val formBuilder = FormBuilder.createFormBuilder()
//            .addLabeledComponent(ColorComponent(value.color, imageSize, imageSize),JBLabel())
            .addComponent(
                JBLabel(
                    "${(value.psiField.parent as PsiClass).name}.${value.psiField.name}",
                    JBUIScale.scaleIcon(ColorIcon(12, value.color)),
                    SwingConstants.LEFT
                )
            )
        return formBuilder.panel
    }

}