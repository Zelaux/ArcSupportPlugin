package com.zelaux.arcplugin.psi.impl.source.resolve.reference.impl;

import arc.util.Reflect;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.patterns.ElementPattern;
import com.intellij.patterns.PsiJavaElementPattern;
import com.intellij.patterns.PsiJavaPatterns;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.resolve.reference.impl.JavaLangClassMemberReference;
import com.intellij.psi.impl.source.resolve.reference.impl.providers.JavaClassReferenceProvider;
import com.intellij.psi.impl.source.resolve.reference.impl.providers.JavaClassReferenceSet;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.intellij.patterns.PsiJavaPatterns.psiLiteral;
import static com.intellij.patterns.PsiJavaPatterns.psiMethod;
import static com.intellij.patterns.StandardPatterns.or;
import static com.intellij.patterns.StandardPatterns.string;
import static com.intellij.psi.CommonClassNames.JAVA_LANG_CLASS;
import static com.intellij.psi.impl.source.resolve.reference.impl.JavaReflectionReferenceUtil.*;

public class ArcReflectionReferenceContributor extends PsiReferenceContributor {
    static final class Holder {
        public static final PsiJavaElementPattern.Capture<PsiLiteral> FIELD_1 =
                psiLiteral().methodCallParameter(1,
                                psiMethod()
                                        .withName(ArcReflectionReferenceUtil.GET,ArcReflectionReferenceUtil.SET)
                                        .withParameters(Class.class.getName(), String.class.getName(),"..")
                                        .definedInClass(Reflect.class.getCanonicalName())
                );
        public static final PsiJavaElementPattern.Capture<PsiLiteral> FIELD_2=
                psiLiteral().methodCallParameter(2,
                                psiMethod()
                                        .withName(ArcReflectionReferenceUtil.GET,ArcReflectionReferenceUtil.SET)
                                        .withParameters(Class.class.getName(),"?", String.class.getName())
                                        .definedInClass(Reflect.class.getCanonicalName())
                );
        public static final PsiJavaElementPattern.Capture<PsiLiteral> METHOD_1 =
                psiLiteral()
                        .methodCallParameter(1,
                                psiMethod().withName(ArcReflectionReferenceUtil.INVOKE)
                                        .withParameters(Class.class.getName(),String.class.getName(),"..")
                        );
        public static final PsiJavaElementPattern.Capture<PsiLiteral> METHOD_2 =
                psiLiteral()
                        .methodCallParameter(2,
                                psiMethod()
                                        .withName(ArcReflectionReferenceUtil.INVOKE)
                                        .withParameters(Class.class.getName(),"?", String.class.getName(),"?","?")
                        );

        static final PsiJavaElementPattern.Capture<PsiLiteral> CLASS_PATTERN =
                psiLiteral().methodCallParameter(0,
                        psiMethod().withName(ArcReflectionReferenceUtil.MAKE).definedInClass(Reflect.class.getCanonicalName())
                );


    }

    @Override
    public void registerReferenceProviders(@NotNull PsiReferenceRegistrar registrar) {
        registrar.registerReferenceProvider(Holder.FIELD_1, getJavaReflectionReferenceProvider());
        registrar.registerReferenceProvider(Holder.FIELD_2, getJavaReflectionReferenceProvider());
        registrar.registerReferenceProvider(Holder.METHOD_1, getJavaReflectionReferenceProvider());
        registrar.registerReferenceProvider(Holder.METHOD_2, getJavaReflectionReferenceProvider());
        registrar.registerReferenceProvider(Holder.CLASS_PATTERN, new ReflectionClassNameReferenceProvider());


    }

    @NotNull
    private static JavaReflectionReferenceProvider getJavaReflectionReferenceProvider() {
        return new JavaReflectionReferenceProvider() {
            @Override
            protected PsiReference @Nullable [] getReferencesByMethod(@NotNull PsiLiteralExpression literalArgument,
                                                                      @NotNull PsiReferenceExpression methodReference,
                                                                      @NotNull ProcessingContext context) {
                PsiElement parent = methodReference.getParent();
                if (!(parent instanceof PsiCallExpression)) return null;
                PsiExpressionList argumentList = ((PsiCallExpression) parent).getArgumentList();
                if (argumentList == null) return null;
                final PsiExpression qualifier = argumentList.getExpressions()[0];
                return qualifier != null ? new PsiReference[]{new ArcClassMemberReference(literalArgument, qualifier)} : null;
            }
        };
    }

    private static class ReflectionClassNameReferenceProvider extends JavaClassReferenceProvider {

        ReflectionClassNameReferenceProvider() {
            setOption(JavaClassReferenceProvider.ALLOW_DOLLAR_NAMES, Boolean.TRUE);
            setOption(JavaClassReferenceProvider.JVM_FORMAT, Boolean.TRUE);
        }

        @Override
        public PsiReference @NotNull [] getReferencesByString(String str, @NotNull PsiElement position, int offsetInPosition) {
            if (StringUtil.isEmpty(str)) {
                return PsiReference.EMPTY_ARRAY;
            }
            return new JavaClassReferenceSet(str, position, offsetInPosition, true, this) {
                @Override
                public boolean isAllowSpaces() {
                    return false;
                }

                @Override
                public boolean isAllowDollarInNames() {
                    return true;
                }
            }.getAllReferences();
        }
    }
}
