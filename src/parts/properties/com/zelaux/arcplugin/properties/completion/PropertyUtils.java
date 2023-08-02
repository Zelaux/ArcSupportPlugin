package com.zelaux.arcplugin.properties.completion;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.lang.properties.IProperty;
import com.intellij.lang.properties.PropertiesReferenceManager;
import com.intellij.lang.properties.psi.PropertiesFile;
import com.intellij.lang.properties.references.PropertyReference;
import com.intellij.lang.properties.references.PropertyReferenceBase;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.util.ArrayUtil;
import com.intellij.util.PlatformIcons;
import com.intellij.util.containers.ContainerUtil;
import it.unimi.dsi.fastutil.Hash;
import it.unimi.dsi.fastutil.objects.ObjectOpenCustomHashSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static com.intellij.lang.properties.references.PropertiesCompletionContributor.LOOKUP_ELEMENT_RENDERER;
import static com.intellij.lang.properties.references.PropertiesPsiCompletionUtil.addVariantsFromFile;

public class PropertyUtils {
    /*private static final
    MethodHandle getPropertiesFiles;*/

    static {
        try {
//            getPropertiesFiles = MethodHandles.lookup().unreflect(PropertyReferenceBase.class.getDeclaredMethod("getPropertiesFiles"));
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static PropertyReference getPropertyReference(@NotNull CompletionParameters parameters) {
        PsiElement position = parameters.getPosition();
        PsiElement parent = position.getParent();
        PsiReference[] references = parent == null ? position.getReferences() : ArrayUtil.mergeArrays(position.getReferences(), parent.getReferences());
        PropertyReference propertyReference = ContainerUtil.findInstance(references, PropertyReference.class);
        return propertyReference;
    }

    @Nullable
    public static Stream<PropertyInfo> getPropertiesKeys(Project project) {

        final Set<Object> variants = getVariants();

        PropertiesReferenceManager instance = PropertiesReferenceManager
                .getInstance(project);
        instance.processAllPropertiesFiles((baseName, propertiesFile) -> {
            addVariantsFromFile(propertiesFile, variants);
            return true;
        });
        return variants.stream().map(o -> {
                    if (o instanceof String)
                        return new PropertyInfo(null, (String) o);

                    IProperty property = (IProperty) o;
                    return property.getKey() == null ? null : new PropertyInfo(property, property.getKey());
                })
                .filter(Objects::nonNull);
    }

    public static void addVariantsFromFile(
            final PropertiesFile propertiesFile,
            final Set<Object> variants) {
        if (propertiesFile == null) return;
        VirtualFile virtualFile = propertiesFile.getVirtualFile();
        if (virtualFile == null || !ProjectRootManager.getInstance(propertiesFile.getProject()).getFileIndex().isInContent(virtualFile))
            return;
        List<? extends IProperty> properties = propertiesFile.getProperties();
        variants.addAll(properties);
    }

    static PropertyInfo findKey(final Project project, Predicate<IProperty> filter) {
        PropertiesReferenceManager instance = PropertiesReferenceManager.getInstance(project);
        IProperty[] found = {null};
        instance.processAllPropertiesFiles((baseName, propertiesFile) -> {
            found[0] = findVariantsFromFile(propertiesFile, filter);
            return found[0] == null;
        });
        if (found[0] != null && found[0].getKey() != null) return new PropertyInfo(found[0], found[0].getKey());
        return null;
    }

    @Nullable
    public static IProperty findVariantsFromFile(
            final PropertiesFile propertiesFile,
            final Predicate<IProperty> filter) {
        if (propertiesFile == null) return null;
        VirtualFile virtualFile = propertiesFile.getVirtualFile();
        if (virtualFile == null || !ProjectRootManager.getInstance(propertiesFile.getProject()).getFileIndex().isInContent(virtualFile))
            return null;
        List<? extends IProperty> properties = propertiesFile.getProperties();
        for (IProperty property : properties) {
            if (filter.test(property)) return property;
        }
        return null;
    }

    @NotNull
    private static Set<Object> getVariants() {
        final Set<Object> variants = new ObjectOpenCustomHashSet<>(new Hash.Strategy<>() {
            @Override
            public int hashCode(@Nullable Object object) {
                if (object instanceof IProperty) {
                    String key = ((IProperty) object).getKey();
                    return key == null ? 0 : key.hashCode();
                } else {
                    return 0;
                }
            }

            @Override
            public boolean equals(@Nullable Object o1, @Nullable Object o2) {
                if (o1 == o2) {
                    return true;
                }
                return o1 instanceof IProperty && o2 instanceof IProperty &&
                        Objects.equals(((IProperty) o1).getKey(), ((IProperty) o2).getKey());
            }
        });
        return variants;
    }

    public static class PropertyInfo {
        @Nullable
        public IProperty property;
        public String text;

        public PropertyInfo(@Nullable IProperty property, String text) {
            this.property = property;
            this.text = text;
        }

        public LookupElement toLookup() {
            if (property != null) {
                return LookupElementBuilder.create(property, text).withRenderer(LOOKUP_ELEMENT_RENDERER);
            }
            return LookupElementBuilder.create(text).withIcon(PlatformIcons.PROPERTY_ICON);
        }
    }
}
