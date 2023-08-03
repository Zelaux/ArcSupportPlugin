package com.zelaux.arcplugin.utils.cache;

import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.PsiModificationTracker;
import com.intellij.util.ArrayUtil;
import com.intellij.util.CachedValueImpl;
import com.intellij.util.ObjectUtils;
import com.intellij.util.containers.NotNullList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MyCachedValueImpl<T> extends CachedValueImpl<T> implements MyCachedValue<T> {
    final Object[] inferredDependencies;
    private final PsiManager myManager;
    int inGetValue;

    public MyCachedValueImpl(PsiManager manager, @NotNull MyCachedValueProvider<T> provider) {
        super(provider);
        myManager = manager;
        inferredDependencies = normalizeDependencies(provider.dependencies());
    }

    private static void collectDependencies(@NotNull List<Object> resultingDeps, Object @NotNull [] dependencies) {
        for (Object dependency : dependencies) {
            if (dependency == ObjectUtils.NULL) continue;
            if (dependency instanceof Object[]) {
                collectDependencies(resultingDeps, (Object[]) dependency);
            } else {
                resultingDeps.add(dependency);
            }
        }
    }

    @Override
    public T getValue() {
        inGetValue++;
        T value = super.getValue();
        inGetValue--;
        return value;
    }

    @Override
    protected boolean isUpToDate(@NotNull Data<T> data) {
        if (inGetValue > 0) return false;
        return super.isUpToDate(data);
    }

    protected Object @NotNull [] normalizeDependencies(Object[] rawDependencies) {
        List<Object> flattened = new NotNullList<>(rawDependencies.length);
        collectDependencies(flattened, rawDependencies);
        return ArrayUtil.toObjectArray(flattened);
    }


    @Override
    public long[] currentValidTimeStamps() {
        long[] inferredTimeStamps = new long[inferredDependencies.length];
        for (int i = 0; i < inferredDependencies.length; i++) {
            inferredTimeStamps[i] = getTimeStamp(inferredDependencies[i]);
        }
        return inferredTimeStamps;
    }

    @Override
    public boolean isUpToDate(long[] timeStamps) {
        for (int i = 0; i < inferredDependencies.length; i++) {
            Object dependency = inferredDependencies[i];
            if (isDependencyOutOfDate(dependency, timeStamps[i])) return false;
        }

        return true;
    }

    @Override
    protected long getTimeStamp(@NotNull Object dependency) {
        if (dependency instanceof PsiDirectory) {
            return myManager.getModificationTracker().getModificationCount();
        }

        if (dependency instanceof PsiElement) {
            PsiElement element = (PsiElement) dependency;
            if (!element.isValid()) return -1;
            PsiFile containingFile = element.getContainingFile();
            if (containingFile != null) return containingFile.getModificationStamp();
        }

        if (dependency == PsiModificationTracker.MODIFICATION_COUNT) {
            return myManager.getModificationTracker().getModificationCount();
        }

        return super.getTimeStamp(dependency);
    }

    public static abstract class MyCachedValueProvider<T> implements CachedValueProvider<T> {
        @Override
        public final @Nullable Result<T> compute() {
            return Result.create(computeValue(), dependencies());
        }

        public abstract T computeValue();

        public abstract Object[] dependencies();
    }
}
