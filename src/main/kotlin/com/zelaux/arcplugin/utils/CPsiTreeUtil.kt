@file:JvmName("CustomPsiTreeUtil")

package com.zelaux.arcplugin.utils

import com.intellij.codeInsight.TargetElementUtil
import com.intellij.lang.LanguageUtil
import com.intellij.openapi.editor.EditorFactory
import com.intellij.psi.*
import com.intellij.psi.impl.compiled.ClsClassImpl
import com.intellij.psi.impl.compiled.ClsFieldImpl
import org.jetbrains.annotations.Contract
import java.util.*
import java.util.stream.Collectors

fun PsiElement.resolveRecursiveField(): PsiField? {
    return tryResolve()
}

private fun PsiElement.tryResolve(): PsiField? {
    val resolve =this.resolveElement()
    if (resolve !is PsiField) {
        if (resolve is PsiLocalVariable) {
            return resolve.initializer?.tryResolve()
        }
        return null
    }

    if (resolve.containingFile?.virtualFile?.let { LanguageUtil.getLanguageForPsi(project, it, it.fileType) != null } == true) {
        return resolve
    }
//    val resolveDocument = PsiDocumentManager.getInstance(project).getDocument()
//    if (resolveDocument?.isWritable == true) {
//        return resolve;
//    }
    if (resolve !is ClsFieldImpl) return null
    val parent = resolve.getParent() as ClsClassImpl
    val sourceMirrorClass = parent.sourceMirrorClass?:return null
    val collect = Arrays.stream(sourceMirrorClass.allFields)
            .filter { it: PsiField -> it.name == resolve.name }.collect(Collectors.toList())
    if (collect.size == 1 && collect[0].hasInitializer()) return collect[0]
    return null
}

fun PsiElement.resolveElement(): PsiElement? {
    val currentDocument = PsiDocumentManager.getInstance(project).getDocument(containingFile) ?: return null
    val editor = EditorFactory.getInstance().getEditors(currentDocument, project).takeIf { it.isNotEmpty() }?.let { it[0] } ?: return null
    val reference = TargetElementUtil.findReference(editor, textOffset) ?: return null
    return reference.resolve()
}


@Suppress("NAME_SHADOWING")
@Contract("null, _, _, _ -> null")
@JvmOverloads
fun <T : PsiElement?> getParentOfType(element: PsiElement?, strict: Boolean = true, minStartOffset: Int = -1, vararg types: Class<out T>): T? {
    var element: PsiElement? = element ?: return null
    if (strict) {
        if (element is PsiFile) {
            return null
        }
        element = element?.parent
    }
    while (element != null && (minStartOffset == -1 || element.node.startOffset >= minStartOffset)) {
        for (type in types) {
            if (type.isInstance(element)) {
                return type.cast(element)
            }
        }
        if (element is PsiFile) {
            return null
        }
        element = element.parent
    }
    return null
}

