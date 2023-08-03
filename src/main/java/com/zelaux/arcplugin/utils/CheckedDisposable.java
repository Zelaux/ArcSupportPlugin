package com.zelaux.arcplugin.utils;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.util.Disposer;

public class CheckedDisposable implements Disposable {
    volatile boolean isDisposed;

    public boolean isDisposed() {
        return isDisposed;
    }

    @Override
    public void dispose() {
        isDisposed = true;
    }

    public void register(Disposable parent) {
        isDisposed = false;
        Disposer.register(parent, this);
    }

    @Override
    public String toString() {
        return "CheckedDisposable{isDisposed=" + isDisposed + "} " + super.toString();
    }
}