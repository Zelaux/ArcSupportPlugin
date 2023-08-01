package com.zelaux.arcplugin.parsers

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Ref
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.ui.colorpicker.LightCalloutPopup
import com.zelaux.arcplugin.parsers.colorexpr.StartPointParser
import com.zelaux.arcplugin.ui.JPanelBuilder
import java.awt.Color
import javax.swing.Icon

abstract class ExpressionParser<
        SEQUENCE_TYPE : ExpressionParserSequence<SEQUENCE_MANAGER_TYPE>,
        SEQUENCE_MANAGER_TYPE,
        WRAPPER_TYPE
        >(element: PsiElement)
        where WRAPPER_TYPE : ExpressionParser<SEQUENCE_TYPE, SEQUENCE_MANAGER_TYPE, WRAPPER_TYPE>, WRAPPER_TYPE
: ExpressionParserWrapper {
    var element: PsiElement = element

    var unpredictable: Boolean = false
    var pointless: Boolean = false

    @Suppress("PropertyName")
    internal var _P_R_I_V_A_T_E__O_F_F_S_E_T: Int = 0

    open val resetInner: Boolean get() = this is StartPointParser;
    open val isStatic: Boolean get() = false;

    abstract val tabTitle: String
    abstract fun wrap(tabTitle: String): WRAPPER_TYPE
    abstract fun asSequence(): SEQUENCE_TYPE
    abstract fun getTabComponent(
        project: Project,
        popupRef: Ref<LightCalloutPopup>,
        sequence: SEQUENCE_TYPE,
        sequenceManager: Ref<SEQUENCE_MANAGER_TYPE>,
        writable: Boolean
    ): JPanelBuilder

    open fun validate() = true
}

interface ExpressionParserWrapper
interface ITargetableColorExpParser<T : ITargetableColorExpParser<T>> {
    val targetSelector: T.(PsiElement) -> PsiElement
    var element: PsiElement
    fun replaceTarget(target: PsiElement, replacement: PsiElement) {
        val newTarget = target.replace(replacement);
        if (target == element) {
            element = newTarget;
        }
    }

    @Suppress("UNCHECKED_CAST")
    val targetExpression get() = (this as T).targetSelector(element);
}

interface ExpressionParserSequence<MANAGER_TYPE> {
    val iconColor: Color get() = java.awt.Color.white
    val icon: Icon
    val textRange: TextRange
    fun showPopup(project: Project, editor: Editor, writable: Boolean)
    fun mergeIcons(list: Array<out ExpressionParserSequence<MANAGER_TYPE>>):Icon
}