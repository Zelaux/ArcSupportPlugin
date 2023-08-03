package com.zelaux.arcplugin.utils.cache;

import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.ParameterizedCachedValueProvider;
import org.jetbrains.annotations.Nullable;

public abstract class MyParameterizedCachedValueProvider<T,P> implements ParameterizedCachedValueProvider<T, P> {
    @Override
    public final  CachedValueProvider.Result<T> compute(P param) {
        return CachedValueProvider.Result.create(computeValue(param), dependencies());
    }

    public abstract T computeValue(P param);

    public abstract Object[] dependencies();
}
