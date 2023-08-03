package com.zelaux.arcplugin.utils.cache;

import com.intellij.psi.util.CachedValue;

public interface MyCachedValue<T> extends CachedValue<T> {
//    boolean needToUpdate();
    long[] currentValidTimeStamps();

    public boolean isUpToDate(long[] timeStamps);
}
