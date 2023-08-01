package com.zelaux.arcplugin.parsers.colorexpr

import arc.math.Mathf
import com.intellij.java.JavaBundle
import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.command.CommandProcessor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiCallExpression
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiIdentifier
import com.intellij.psi.PsiReferenceExpression
import com.zelaux.arcplugin.ui.JPanelBuilder
import com.zelaux.arcplugin.ui.components.ColorComponent
import javax.swing.JComponent
import javax.swing.plaf.ComponentUI


internal val PsiCallExpression.reciver get() = firstChild?.firstChild
internal val PsiCallExpression.methodName get() = firstChild.lastChild as PsiIdentifier
internal val PsiElement.parentCall get() = (parent as? PsiReferenceExpression)?.parent as? PsiCallExpression

internal inline operator fun <reified T> T.times(int: Int) = Array<T>(int) { this }
private val `JComponent_setUI` = JComponent::class.java.declaredMethods.find { it.name.equals("setUI") }!!.apply { isAccessible = true }

internal fun JComponent.setUI_(componentUI: ComponentUI) {
    JComponent_setUI(this, componentUI)
}

internal fun swapComponent(from: JComponent, to: JComponent) {
    val parent = from.parent;
    var i = 0;
    while (parent.getComponent(i) !== from) {
        i++;
    }
    parent.remove(i)
    parent.add(to, i)

    parent.repaint()
}
internal typealias awtColor = java.awt.Color;

internal typealias arcColor = arc.graphics.Color;

internal fun MonoColorJPanelBuilder(sequence: ColorExpParserSequence, awtColor: awtColor, runnableListener: ((awtColor) -> Unit) -> Unit) = JPanelBuilder().apply {
    addComponent(ColorComponent(awtColor).apply gradient@{
        size=sequence.sliderSize
        runnableListener.invoke { newColor ->
            this@gradient.color = newColor
        }
    })
}

fun arcColor.awtColor(): awtColor = awtColor(Mathf.clamp(r), Mathf.clamp(g), Mathf.clamp(b), Mathf.clamp(a))
fun awtColor.arcColor(): arcColor = arcColor(red / 255f, green / 255f, blue / 255f, alpha / 255f)
internal fun arcColor.set(awtColor: awtColor) = set(awtColor.red / 255f, awtColor.green / 255f, awtColor.blue / 255f, awtColor.alpha / 255f)

internal inline fun writeColorAction(project: Project, crossinline block: () -> Unit) {
    WriteAction.run<RuntimeException> {
        CommandProcessor.getInstance().executeCommand(project, {
            block()
        }, JavaBundle.message("change.color.command.text"), null)
    }
}
/*

* */