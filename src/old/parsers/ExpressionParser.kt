package com.zelaux.arcplugin.parsers

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Ref
import com.intellij.openapi.util.TextRange
import com.intellij.psi.SmartPointerManager
import org.jetbrains.uast.findContaining
import com.intellij.ui.colorpicker.LightCalloutPopup
import com.zelaux.arcplugin.parsers.colorexpr.StartPointParser
import com.zelaux.arcplugin.ui.JPanelBuilder
import org.jetbrains.uast.UElement
import org.jetbrains.uast.toUElement
import java.awt.Color
import javax.swing.Icon

abstract class ExpressionParser<
        SEQUENCE_TYPE : ExpressionParserSequence<SEQUENCE_MANAGER_TYPE>,
        SEQUENCE_MANAGER_TYPE,
        WRAPPER_TYPE,
        UAST_TYPE :  UElement
        >(element: UElement, val clazz: Class<UAST_TYPE>)
        where WRAPPER_TYPE : ExpressionParser<SEQUENCE_TYPE, SEQUENCE_MANAGER_TYPE, WRAPPER_TYPE, UElement>, WRAPPER_TYPE
: ExpressionParserWrapper {
    constructor(element: UAST_TYPE) : this(element, element.javaClass)

    private val psiElement = SmartPointerManager.createPointer(element.sourcePsi!!)

    @get:JvmName("getElement_")
    @set:JvmName("setElement_")
    var element = element
        get() {
            if (field.sourcePsi != psiElement.element) {
                field = psiElement.element.findContaining(clazz)!!
            }
            return field;
        }

    protected fun invalidateElement() {
        element = psiElement.element.findContaining(clazz)!!
    }

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

interface ExpressionParserSequence<MANAGER_TYPE> {
    val iconColor: Color get() = java.awt.Color.white
    val icon: Icon
    val textRange: TextRange
    fun isEmpty(): Boolean
    fun showPopup(project: Project, editor: Editor, writable: Boolean)
    fun mergeIcons(list: Array<out ExpressionParserSequence<MANAGER_TYPE>>): Icon
}