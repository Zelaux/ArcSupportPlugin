@file:JvmName("UExpressionUtils")

package com.zelaux.arcplugin.utils

import com.intellij.psi.PsiClass
import org.jetbrains.annotations.Contract
import org.jetbrains.uast.UCallExpression
import org.jetbrains.uast.UExpression
import org.jetbrains.uast.UastCallKind

@Contract("null, _, _ -> false")
fun UCallExpression?.isStaticMethod( classPath: String, name: String): Boolean {
    if (this == null) return false;
//        if (!this.isString())return ProblemDescriptor.EMPTY_ARRAY;
//    val receiver = this.receiver
//    val uastParent = this.uastParent
    val resolve=this.resolve();
    val resolveParent=resolve?.parent;
    if (this.kind == UastCallKind.METHOD_CALL
        && name == this.methodName
        && ( PsiUtils.isType(classPath, resolveParent as? PsiClass))
    ) {
        return true;
    }
    return false
}
fun UExpression.tryResolveWithReference() = sourcePsi?.reference?.resolve();