package com.zelaux.arcplugin.utils

import com.intellij.psi.PsiClass
import com.intellij.psi.PsiType
import com.intellij.psi.util.PsiTypesUtil
import org.jetbrains.uast.UExpression

object PsiUtils {
    @JvmStatic
    fun isType(classPath: String, type: PsiType?): Boolean {
        if (type == null) return false
        val aClass = PsiTypesUtil.getPsiClass(type)
        if (isType(classPath, aClass)) return true
        return false
    }

    @JvmStatic
    fun isType(classPath: String, aClass: PsiClass?): Boolean {
        if (aClass == null) return false
        val fqn = aClass.qualifiedName
        return classPath == fqn
    }
@JvmStatic
    fun isType(classPath: String, expression: UExpression?): Boolean {
        if (expression == null) return false
        val sourcePsi = expression.sourcePsi
        for (reference in sourcePsi!!.references) {
            if (reference == null) continue
            val resolve = reference.resolve() as? PsiClass ?: continue
            if (isType(classPath, resolve)) return true
        }
        return false
    }
}