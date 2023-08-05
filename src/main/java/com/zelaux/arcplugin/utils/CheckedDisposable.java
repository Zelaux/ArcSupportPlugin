package com.zelaux.arcplugin.utils;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.util.Disposer;

public class CheckedDisposable implements Disposable {
    public Disposable parent;
    volatile boolean isDisposed;

    public CheckedDisposable() {
    }

    public CheckedDisposable(Disposable parent) {
        this.parent = parent;
    }

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

    public void register() {
        register(parent);
    }

    @Override
    public String toString() {
        return "CheckedDisposable{isDisposed=" + isDisposed + "} " + super.toString();
    }
}