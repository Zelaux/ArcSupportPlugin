package com.zelaux.arcplugin.utils.cache;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.psi.PsiManager;

public class MyCacheValuesFactory {

    public final Project project;
    public final PsiManager manager;
    private static final Key<MyCacheValuesFactory> MY_KEY = Key.create(MyCacheValuesFactory.class.getCanonicalName());

    public MyCacheValuesFactory(Project project) {
        this.project = project;
        this.manager=PsiManager.getInstance(project);
    }

    public static MyCacheValuesFactory getInstance(Project project) {
        MyCacheValuesFactory factory = project.getUserData(MY_KEY);
        if (factory == null) {
            factory = new MyCacheValuesFactory(project);
            project.putUserData(MY_KEY, factory);
        }
        return factory;
    }

    public <T> MyCachedValue<T> createCachedValue(MyCachedValueImpl.MyCachedValueProvider<T> provider){
        return new MyCachedValueImpl<>(manager,provider);
    }
    public <T,P> MyParameterizedCachedValue<T,P> createParameterizedCachedValue(MyParameterizedCachedValueProvider<T,P> provider){
        return new MyParameterizedCachedValueImpl<>(manager,provider);
    }

}
