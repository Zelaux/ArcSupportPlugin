package com.zelaux.arcplugin.events;

import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.uast.UCallExpression;
import org.jetbrains.uast.UExpression;
import org.jetbrains.uast.UQualifiedReferenceExpression;
import org.jetbrains.uast.UResolvable;

import java.util.Objects;

public class EventType {
    public static final EventType nothing = new EventType() {
        @Override
        public String toString() {
            return "EventType#nothing";
        }
    };

    private EventType() {

    }

    public final boolean exists() {
        return this != nothing;
    }

    public final boolean isAssignableFrom(EventType other) {
        if (!exists() || !other.exists()) return false;
        return this.equals(other);
    }

    protected boolean isAssignableFrom_(EventType other) {
        return false;
    }

    public static class SimpleType extends EventType {
        @NotNull PsiType psiType;

        public SimpleType(@NotNull PsiType psiType) {
            this.psiType = psiType;
        }

        public static EventType create(UCallExpression expression, UExpression typeExpression) {
            PsiType expressionType = typeExpression.getExpressionType();
            if (!expression.getMethodName().equals("fire")) {
                if (expressionType instanceof PsiClassType && ((PsiClassType) expressionType).rawType().getCanonicalText().equals(Class.class.getCanonicalName())) {
                    PsiType[] parameters = ((PsiClassType) expressionType).getParameters();
                    if(parameters.length==1) return new SimpleType(parameters[0]);
                    return nothing;
                }

            }
            if (expressionType instanceof PsiClassType) {
                return new SimpleType(((PsiClassType) expressionType).rawType());
            }
            if(expressionType==null)return nothing;
            return new SimpleType(expressionType);


        }

        private static PsiType unwrap(PsiType psiType) {
            return psiType instanceof PsiClassType ? ((PsiClassType) psiType).rawType() : psiType;
        }

        @Override
        protected boolean isAssignableFrom_(EventType other) {
            if (other instanceof SimpleType) {
                SimpleType that = (SimpleType) other;
                return psiType.isAssignableFrom(that.psiType);
            }
            return false;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            SimpleType that = (SimpleType) o;
            return Objects.equals(unwrap(psiType).getCanonicalText(), unwrap(that.psiType).getCanonicalText());
        }

        @Override
        public int hashCode() {
            return Objects.hash(unwrap(psiType).getCanonicalText());
        }
    }

    public static class EnumType extends EventType {
        public PsiClass enumClass;
        public String enumConstant;

        private EnumType(PsiClass enumClass, String enumConstant) {
            this.enumClass = enumClass;
            this.enumConstant = enumConstant;
        }

        public static EventType tryMakeEnumType(UExpression expression) {
            if (!(expression instanceof UQualifiedReferenceExpression)) return nothing;
            UQualifiedReferenceExpression referenceExpression = (UQualifiedReferenceExpression) expression;

            UExpression receiver = referenceExpression.getReceiver();
            if (!(receiver instanceof UResolvable)) return nothing;

            PsiElement resolvedReceiver = ((UResolvable) receiver).resolve();
            if (!(resolvedReceiver instanceof PsiClass)) return nothing;
            PsiClass clazz = (PsiClass) resolvedReceiver;
            String canonicalText = expression.getExpressionType().getCanonicalText();
            if (!clazz.getQualifiedName().equals(canonicalText))
                return nothing;
            return new EnumType(clazz, referenceExpression.getSelector().asSourceString());
        }

        @Override
        protected boolean isAssignableFrom_(EventType other) {
            return equals(other);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof EnumType) {
                EnumType other = (EnumType) obj;
                return
                        Objects.equals(other.enumConstant, enumConstant) &&
                                Objects.equals(other.enumClass.getQualifiedName(), enumClass.getQualifiedName())
                        ;
            }
            return false;
        }

        @Override
        public int hashCode() {
            return Objects.hash(enumClass.getQualifiedName(), enumConstant);
        }
    }

}
