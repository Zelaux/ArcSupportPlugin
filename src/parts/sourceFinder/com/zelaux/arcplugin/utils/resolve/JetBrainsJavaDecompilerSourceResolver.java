package com.zelaux.arcplugin.utils.resolve;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiFile;
import com.intellij.psi.impl.compiled.ClsClassImpl;

public class JetBrainsJavaDecompilerSourceResolver implements SourceResolver{
//    private
    @Override
    public PsiClass resolveClassInner(PsiClass clazz) {
        if (clazz instanceof ClsClassImpl clsClass)
            if (clsClass.getSourceMirrorClass() != null) {
                return clsClass.getSourceMirrorClass();
             } else if(clsClass.getMirror()!=null)return (PsiClass) clsClass.getMirror();
return null;
/*
        PsiFile file = clazz.getContainingFile();
        if(file==null)return null;
        VirtualFile virtualFile = file.getVirtualFile();
        if(virtualFile==null)return null;
//        new IdeaDecompiler().getText()
        return SimpleMirrorResolver.instance.resolveClassInner(clazz);*/
    }
}
