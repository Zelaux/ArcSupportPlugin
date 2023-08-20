package com.zelaux.arcplugin.utils.resolve;

import com.intellij.openapi.util.Key;
import com.intellij.psi.*;
import com.intellij.psi.util.CachedValue;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

public class StaticFieldResolver {
    private static final Key<CachedValue<HashMap<String, FieldData>>> DATA_KEY = Key.create(StaticFieldResolver.class.getCanonicalName());
    private static final HashMap<String, PsiField> tmpList = new HashMap<>();

    @Nullable
    private static HashMap<String, FieldData> resolve(PsiClass psiClass) {
        HashMap<String, FieldData> data = new HashMap<>();
        tmpList.clear();
        for (PsiField field : psiClass.getFields()) {
            PsiExpression initializer = field.getInitializer();
            if (isNotStatic(field)) continue;
            if (initializer == null) {
                tmpList.put(field.getName(), field);
                continue;
            }

            data.put(field.getName(), new FieldData(field.getName(), field, initializer));
        }
        if (tmpList.size() > 0) {
            for (PsiClassInitializer initializer : psiClass.getInitializers()) {
                if (isNotStatic(initializer)) continue;
                PsiCodeBlock body = initializer.getBody();
                for (PsiStatement statement : body.getStatements()) {
                    if (!(statement instanceof PsiExpressionStatement expressionStatement)
                            || !(expressionStatement.getExpression() instanceof PsiAssignmentExpression assignmentExpression)
                    ) continue;
                    String text = assignmentExpression.getLExpression().getText();
                    if (text.startsWith("this.")) text = text.substring("this.".length());
                    PsiField field = tmpList.remove(text);
                    if (field != null) {
                        data.put(field.getName(),
                                new FieldData(field.getName(), field, assignmentExpression.getRExpression())
                        );
                    }
                }

            }
        }
        tmpList.clear();
        return data;
    }

    private static boolean isNotStatic(PsiModifierListOwner field) {
        return noModifiersOrNotStart(field.getModifierList());
    }

    private static boolean noModifiersOrNotStart(PsiModifierList list) {
        return list == null || !list.hasExplicitModifier(PsiModifier.STATIC);
    }

    @Nullable
    @Contract("null -> null")
    public static PsiExpression resolveStaticInitializer(@Nullable PsiField field) {
        if (field == null) return null;
        PsiExpression initializer = field.getInitializer();
        if (initializer != null) return initializer;
        PsiClass clazz = field.getContainingClass();
        if (clazz == null) return null;

        FieldData fieldData = getFieldData(clazz, field.getName());
        return fieldData == null ? null : fieldData.initializer();
    }

    public static Stream<Map.Entry<String, FieldData>> getFields(PsiClass psiClass) {
        HashMap<String, FieldData> data = getData(psiClass);
        return data == null ? Stream.empty() : data.entrySet().stream();
    }

    @Nullable
    public static FieldData getFieldData(PsiClass psiClass, String fieldName) {
        HashMap<String, FieldData> data = getData(psiClass);
        if (data == null) return null;
        return data.get(fieldName);
    }

    @Nullable
    private static HashMap<String, FieldData> getData(PsiClass __psiClass) {
        PsiClass psiClass = Objects.requireNonNullElse(SourceResolver.resolveClass(__psiClass), __psiClass);
        return CachedValuesManager.getCachedValue(
                psiClass, DATA_KEY, () -> {
                    HashMap<String, FieldData> resolve = resolve(psiClass);
                    return CachedValueProvider.Result.create(resolve, psiClass);
                }
        );
    }
}
