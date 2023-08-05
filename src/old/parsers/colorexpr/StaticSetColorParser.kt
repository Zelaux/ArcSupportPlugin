package com.zelaux.arcplugin.parsers.colorexpr

import arc.graphics.Color
import arc.struct.Seq
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.util.Ref
import com.intellij.psi.*
import com.intellij.ui.colorpicker.LightCalloutPopup
import com.intellij.ui.components.JBLabel
import com.intellij.ui.scale.JBUIScale
import com.intellij.util.ui.ColorIcon
import com.intellij.util.ui.FormBuilder
//import com.zelaux.arcplugin.ArcColorUtils
import com.zelaux.arcplugin.colorExpression.ColorExpressionResolver
import com.zelaux.arcplugin.ui.JPanelBuilder
import com.zelaux.arcplugin.utils.CustomUastTreeUtil
import com.zelaux.arcplugin.utils.RelodableLazy
import com.zelaux.arcplugin.utils.relodableLazy
import org.jetbrains.uast.*
import java.awt.Component
import javax.swing.JList
import javax.swing.ListCellRenderer
import javax.swing.SwingConstants

class StaticSetColorParser(expression: UElement, targetSelector: StaticSetColorParser.(UElement) -> UElement) : TargetableColorExpParser<StaticSetColorParser>(expression, targetSelector), OffsetableParser, StartPointParser {

    constructor(callExpression: UCallExpression) : this(callExpression, {
        (it as UCallExpression).valueArguments[offset]
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
        val uExpression = targetExpression as? UExpression ?: return@relodableLazy null
        arcColor().set(ColorExpressionResolver.resolveColor(uExpression) ?: return@relodableLazy null)
    }

    override fun apply(color: arcColor): Boolean {
        if (this.color == null) return false
        color.set(this.color)
        return true
    }

    public val fieldsAmount get() = fields.size
    private val fields: Array<PsiFieldWithColor> by lazy {
        if (color == null) return@lazy emptyArray()
        val currentField = CustomUastTreeUtil.resolveRecursiveField(targetExpression) ?: return@lazy emptyArray()
        val seq = Seq<PsiFieldWithColor>()
        val containingClass= CustomUastTreeUtil.getContainingClass(currentField)?:return@lazy emptyArray()
        for (field in containingClass.fields) {
            val initializer = field.uastInitializer ?: continue
            val resolvedColor = ColorExpressionResolver.resolveColor(initializer) ?: continue
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
            val isFieldSetter = CustomUastTreeUtil.resolveElement(psiExpression) is UField

            if (isFieldSetter) return JPanelBuilder().apply {
                val currentField = CustomUastTreeUtil.resolveRecursiveField(psiExpression)!!
//                addComponent(RecentColorsPalette())
                addComponent(ComboBox(fields).also {
                    it.setMinimumAndPreferredWidth(sequence.sliderSize.width)
//                    it.size=
                    it.selectedIndex = fields.indexOfFirst { field -> field.field.name == currentField.name };
                    it.renderer = PsiFieldWithColorRenderer()
                    it.addItemListener { event ->
                        if (event.stateChange!=1)return@addItemListener
                        val item = event.item as PsiFieldWithColor
                        val targetExpression = targetExpression
                        val newText = CustomUastTreeUtil.getFullName(item.field)

                        writeColorAction(project) {
                            replaceTarget(targetExpression,newText)

                            RelodableLazy.resetValue(this@StaticSetColorParser, this@StaticSetColorParser::color)
                            ApplicationManager.getApplication().invokeLater {
                                sequenceManager.get().fireUpdate(this@StaticSetColorParser)//TODO move to another place
                            }
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

    override fun getElement(): UElement {
        return element;
    }

    override fun setElement(element: UElement) {
        this.element=element;
    }

}

class StaticColorParser(val color: arcColor, expression: UExpression) : ColorExpressionParser<UExpression>(expression,UExpression::class.java) {
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

private class PsiFieldWithColor(val field: UField, val color: awtColor)
private class PsiFieldWithColorRenderer : ListCellRenderer<PsiFieldWithColor> {
    override fun getListCellRendererComponent(list: JList<out PsiFieldWithColor>, value: PsiFieldWithColor, index: Int, isSelected: Boolean, cellHasFocus: Boolean): Component {
        val formBuilder = FormBuilder.createFormBuilder()
//            .addLabeledComponent(ColorComponent(value.color, imageSize, imageSize),JBLabel())
            .addComponent(
                JBLabel(
                    "${CustomUastTreeUtil.getFullName(value.field)}",
                    JBUIScale.scaleIcon(ColorIcon(12, value.color)),
                    SwingConstants.LEFT
                )
            )
        return formBuilder.panel
    }

}