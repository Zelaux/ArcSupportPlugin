package com.zelaux.arcplugin.psi;

import com.intellij.psi.PsiType;
import com.zelaux.arcplugin.utils.PsiUtils;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.intellij.psi.CommonClassNames.*;

public enum PrimitiveType {
    BOOLEAN("boolean", JAVA_LANG_BOOLEAN, "Z"),
    BYTE("byte", JAVA_LANG_BYTE, "B"),
    CHAR("char", JAVA_LANG_CHARACTER, "C"),
    DOUBLE("double", JAVA_LANG_DOUBLE, "D"),
    FLOAT("float", JAVA_LANG_FLOAT, "F"),
    INT("int", JAVA_LANG_INTEGER, "I"),
    LONG("long", JAVA_LANG_LONG, "J"),
    SHORT("short", JAVA_LANG_SHORT, "S"),
    VOID("void", JAVA_LANG_VOID, "V"),
    NULL("null", "null", "null"),
    ;
    private static final Map<String, PrimitiveType> ourNameToKind;
    private static final Map<String, PrimitiveType> ourFqnToKind;

    static {
        PrimitiveType[] values = {BOOLEAN, BYTE, CHAR, DOUBLE, FLOAT, INT, LONG, SHORT, VOID};
        Map<String, PrimitiveType> nameToKind = new HashMap<>(values.length);
        Map<String, PrimitiveType> fqnToKind = new HashMap<>(values.length);
        for (PrimitiveType kind : values) {
            nameToKind.put(kind.getName(), kind);
            fqnToKind.put(kind.getBoxedFqn(), kind);
        }
        ourNameToKind = nameToKind;
        ourFqnToKind = fqnToKind;
    }

    private final String myName;
    private final String myBoxedFqn;
    private final String myBinaryName;
    @NotNull
    public String getCanonicalText(){
        return myName;
    }

    private PrimitiveType(@NotNull String name, @NotNull String boxedFqn, @NotNull String binaryName) {
        myName = name;
        myBoxedFqn = boxedFqn;
        myBinaryName = binaryName;
    }

    @Contract(value = "null -> null", pure = true)
    @Nullable
    public static PrimitiveType getKindByName(@Nullable String name) {
        return ourNameToKind.get(name);
    }

    @Contract(value = "null -> null", pure = true)
    @Nullable
    public static PrimitiveType getKindByFqn(@Nullable String fqn) {
        return ourFqnToKind.get(fqn);
    }

    @Contract(pure = true)
    @NotNull
    public static Collection<String> getBoxedFqns() {
        return Collections.unmodifiableCollection(ourFqnToKind.keySet());
    }

    @Contract(pure = true)
    @NotNull
    public String getName() {
        return myName;
    }

    @Contract(pure = true)
    @NotNull
    public String getBoxedFqn() {
        return myBoxedFqn;
    }

    @Contract(pure = true)
    @NotNull
    public String getBinaryName() {
        return myBinaryName;
    }

    public boolean isEqual(@Nullable PsiType psiType) {
        return PsiUtils.isType(psiType, this);
    }
    public boolean equalsToText(@Nullable String string) {
        return myName.equals(string);
    }
}
