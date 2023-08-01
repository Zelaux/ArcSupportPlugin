package com.zelaux.arcplugin.parsers.interp

import arc.math.Interp
import arc.struct.Seq
import arc.util.Reflect
import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.command.CommandProcessor
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.util.Ref
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementFactory
import com.intellij.psi.PsiField
import com.intellij.psi.util.PsiUtil
import com.intellij.ui.colorpicker.LightCalloutPopup
import com.intellij.ui.components.JBLabel
import com.intellij.ui.scale.JBUIScale
import com.intellij.util.ui.FormBuilder
import com.zelaux.arcplugin.PluginIcons
import com.zelaux.arcplugin.parsers.ExpressionParser
import com.zelaux.arcplugin.parsers.ExpressionParserSequence
import com.zelaux.arcplugin.parsers.ExpressionParserWrapper
import com.zelaux.arcplugin.parsers.ITargetableColorExpParser
import com.zelaux.arcplugin.ui.JPanelBuilder
import com.zelaux.arcplugin.ui.components.InterpGraphComponent
import com.zelaux.arcplugin.ui.picker.popup.SimplePopup
import com.zelaux.arcplugin.utils.RelodableLazy
import com.zelaux.arcplugin.utils.relodableLazy
import com.zelaux.arcplugin.utils.resolveElement
import com.zelaux.arcplugin.utils.resolveRecursiveField
import com.zelaux.arcplugin.utils.ui.MultiIcon
import java.awt.Component
import java.awt.Dimension
import java.awt.Graphics
import javax.swing.*

class InterpParser(element: PsiElement, override var tabTitle: String, override var targetSelector: InterpParser.(PsiElement) -> PsiElement) :
    ExpressionParser<InterpParser, Unit, InterpParser>(element),
    ITargetableColorExpParser<InterpParser>,
    ExpressionParserSequence<Unit>,
    ExpressionParserWrapper {


    override fun wrap(tabTitle: String): InterpParser {
        this.tabTitle = tabTitle;
        return this;
    }

    public val fieldsAmount get() = fields.size
    private val fields: Array<PsiInterpField> by lazy {
        if (interpolation == null) return@lazy emptyArray()
        val currentField = targetExpression.resolveRecursiveField() ?: return@lazy emptyArray()
        val seq = Seq<PsiInterpField>()

        for (field in (currentField.containingClass ?: return@lazy emptyArray()).fields) {
            val initializer = field.initializer ?: continue
            val interp = Reflect.get(Interp::class.java.getDeclaredField(field.name)) as Interp
            seq.add(PsiInterpField(field, interp))
        }
        seq.toArray(PsiInterpField::class.java)
//        TODO()
    }

    override fun asSequence() = this;

    @Suppress("MemberVisibilityCanBePrivate")
    val interpolation: Interp? by relodableLazy {
        val psiExpression = targetExpression

        val currentField = psiExpression.resolveRecursiveField() ?: return@relodableLazy null;
        if (currentField.parent is PsiClass && (currentField.parent as PsiClass).qualifiedName.equals(Interp::class.java.name)) {

            Reflect.get(Interp::class.java.getDeclaredField(currentField.name)) as Interp
        } else {
            null
        }

    }

    override fun validate(): Boolean {
        return interpolation != null
    }

    override fun getTabComponent(project: Project, popupRef: Ref<LightCalloutPopup>, sequence: InterpParser, sequenceManager: Ref<Unit>, writable: Boolean): JPanelBuilder {
        val width = JBUIScale.scale(300)
        val height = width


        return JPanelBuilder(true).apply {
            val component = InterpGraphComponent(interpolation, width, height).apply {
                fontSize = 10f;
                size = Dimension(width, height)
                minimumSize = Dimension(width, height)
            }
            addComponent(component)
            addSeparator()
            if (writable && fields.size > 1) {

                val psiExpression = targetExpression
                val isFieldSetter = psiExpression.resolveElement() is PsiField
                val currentField = psiExpression.resolveRecursiveField()!!
                if (isFieldSetter) {
                    addComponent(ComboBox(fields).also {
                        it.setMinimumAndPreferredWidth(width)
//                    it.size=
                        it.selectedIndex = fields.indexOfFirst { field -> field.psiField.name == currentField.name };
                        it.renderer = PsiInterpFieldRenderer()
                        it.addItemListener { event ->
                            if (event.stateChange != 1) return@addItemListener
                            val item = event.item as PsiInterpField
                            val factory: PsiElementFactory = PsiElementFactory.getInstance(project)
                            val targetExpression = targetExpression
                            val newExp = factory.createExpressionFromText(PsiUtil.getMemberQualifiedName(item.psiField)!!, targetExpression.parent)

                            writeInterpAction(project) {
                                replaceTarget(targetExpression, newExp)

                                RelodableLazy.resetValue(this@InterpParser, this@InterpParser::interpolation)
                                component.function = interpolation
                            }
//                        item.psiField.name
                        }
                    })
                }
            }
        }
    }

    override val icon: Icon
        get() = PluginIcons.Graph
    override val textRange: TextRange
        get() = element.textRange

    override fun showPopup(project: Project, editor: Editor, writable: Boolean) {
        SimplePopup.lambdaPopup.show(project, editor) { getTabComponent(project, it, this, Ref.create(), writable) }
    }

    override fun mergeIcons(list: Array<out ExpressionParserSequence<Unit>>): Icon {
//        val array = list.map { it as InterpParser }.toTypedArray()
        return icon;

//        return JBUIScale.scaleIcon(MultiIcon(12, *array.map { it.icon }.toTypedArray()))
    }
}

internal inline fun writeInterpAction(project: Project, crossinline block: () -> Unit) {
    WriteAction.run<RuntimeException> {
        CommandProcessor.getInstance().executeCommand(project, {
            block()
        }, /*JavaBundle.message("change.color.command.text")*/"Change Interp", null)
    }
}

private class PsiInterpField(val psiField: PsiField, val interp: Interp)
private class PsiInterpFieldRenderer : ListCellRenderer<PsiInterpField> {
    override fun getListCellRendererComponent(list: JList<out PsiInterpField>, value: PsiInterpField, index: Int, isSelected: Boolean, cellHasFocus: Boolean): Component {
        val iconsize = 64;
        val formBuilder = FormBuilder.createFormBuilder()
            .addLabeledComponent(
                InterpGraphComponent(value.interp, iconsize, iconsize).apply {
//                    this.stroke = 1f;
                    fontSize = 5f
                    setWindowCenter(-0.0, -0.0, 2.1, 2.1);
                    repaint()
                },
                JBLabel(
                    "${(value.psiField.parent as PsiClass).name}.${value.psiField.name}",
                    SwingConstants.LEFT
                )
            )
        /*.addComponent(
            JBLabel("${(value.psiField.parent as PsiClass).name}.${value.psiField.name}",
            IconWrapper(InterpGraphComponent(value.interp,32,32),32,32),
                SwingConstants.LEFT))*/
        return formBuilder.panel
    }

}

private class IconWrapper(val component: JComponent, val width: Int, val height: Int) : Icon {
    init {
        component.size = Dimension(width, height)
    }

    override fun paintIcon(c: Component, g: Graphics, x: Int, y: Int) {
        g.translate(x, y)
        component.paint(g)
        g.translate(-x, -y)
    }

    override fun getIconWidth() = width

    override fun getIconHeight() = height

}