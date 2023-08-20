package com.zelaux.arcplugin.utils.resolve;

import com.intellij.openapi.extensions.ExtensionPointName;
import com.intellij.psi.PsiClass;
import com.intellij.psi.impl.compiled.ClsClassImpl;

public interface SourceResolver {
    ExtensionPointName<SourceResolver> EP_NAME = ExtensionPointName.create("com.zelaux.arcplugin.sourceResolver");

    static PsiClass resolveClass(PsiClass clazz) {

        for (SourceResolver resolver : EP_NAME.getExtensionList()) {
            PsiClass resolved = resolver.resolveClassInner(clazz);
            if (resolved != null) return resolved;
        }
        return SourceMirrorResolver.instance.resolveClassInner(clazz);
    }

    PsiClass resolveClassInner(PsiClass clazz);

    class SourceMirrorResolver implements SourceResolver {
        private static final SourceMirrorResolver instance = new SourceMirrorResolver();

        @Override
        public PsiClass resolveClassInner(PsiClass clazz) {

            if (clazz instanceof ClsClassImpl) {
                return ((ClsClassImpl) clazz).getSourceMirrorClass();
            }
            return null;
        }
    }
    class SimpleMirrorResolver implements SourceResolver {
        private static final SimpleMirrorResolver instance = new SimpleMirrorResolver();

        @Override
        public PsiClass resolveClassInner(PsiClass clazz) {

            if (clazz instanceof ClsClassImpl) {
                return (PsiClass) ((ClsClassImpl) clazz).getMirror();
            }
            return null;
        }
    }
}
