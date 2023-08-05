package com.zelaux.arcplugin.expressions.resolve;

public interface ArcImportableColorExpressionRenderer {
    default boolean canBeImport(){
        return true;
    }
}
