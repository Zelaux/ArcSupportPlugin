package com.zelaux.arcplugin.utils.cache;

import com.intellij.psi.util.CachedValue;
import com.intellij.psi.util.ParameterizedCachedValue;

public interface MyParameterizedCachedValue<T,P> extends ParameterizedCachedValue<T,P> {
//    boolean needToUpdate();
    long[] currentValidTimeStamps();

    public boolean isUpToDate(long[] timeStamps);
}
