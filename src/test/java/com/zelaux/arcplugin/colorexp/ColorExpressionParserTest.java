package com.zelaux.arcplugin.colorexp;

import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.codeInsight.daemon.LineMarkerProviders;
import com.intellij.codeInsight.daemon.impl.HighlightInfo;
import com.intellij.codeInsight.daemon.impl.LineMarkersPass;
import com.intellij.jarRepository.JarRepositoryManager;
import com.intellij.jarRepository.RemoteRepositoryDescription;
import com.intellij.jarRepository.RepositoryLibraryType;
import com.intellij.lang.java.JavaLanguage;
import com.intellij.openapi.roots.DependencyScope;
import com.intellij.openapi.roots.LibraryOrderEntry;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ModuleRootModificationUtil;
import com.intellij.openapi.roots.impl.libraries.LibraryEx;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.roots.libraries.LibraryTable;
import com.intellij.openapi.roots.libraries.ui.OrderRoot;
import com.intellij.project.IntelliJProjectConfiguration;
import com.intellij.testFramework.PsiTestUtil;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixture4TestCase;
import com.intellij.testFramework.fixtures.MavenDependencyUtil;
import com.intellij.util.containers.ContainerUtil;
import org.apache.maven.repository.internal.MavenRepositorySystemUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.idea.maven.utils.library.RepositoryLibraryProperties;
import org.jetbrains.jps.model.jarRepository.JpsRemoteRepositoryDescription;
import org.junit.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class ColorExpressionParserTest extends LightPlatformCodeInsightFixture4TestCase {
    @Test
    public void test(){
        ModuleRootModificationUtil.updateModel(getModule(), model ->
                addFromMaven(model,"com.github.Anuken.Arc:arc-core:v145",false,
                        DependencyScope.COMPILE,
                        List.of(
                                new RemoteRepositoryDescription("zelauxGithub","ZelauxGithub","https://raw.githubusercontent.com/Zelaux/MindustryRepo/master/repository")
                        )

                        )
                );
        myFixture.configureByFile("Test.java");
        List<LineMarkerProvider> lineMarkerProviders = LineMarkerProviders.getInstance().allForLanguage(JavaLanguage.INSTANCE);
        Collection<LineMarkerInfo<?>> markerInfos = LineMarkersPass.queryLineMarkers(myFixture.getFile(), myFixture.getDocument(myFixture.getFile()));
        List<HighlightInfo> infos = myFixture.doHighlighting();
        System.out.println("it");
        for (HighlightInfo info : infos) {

        }

    }
    public static void addFromMaven(@NotNull ModifiableRootModel model,
                                    String mavenCoordinates,
                                    boolean includeTransitiveDependencies,
                                    DependencyScope dependencyScope,
                                    List<RemoteRepositoryDescription> remoteRepositoryDescriptions) {
        RepositoryLibraryProperties libraryProperties = new RepositoryLibraryProperties(mavenCoordinates, includeTransitiveDependencies);
        Collection<OrderRoot> roots =
                JarRepositoryManager.loadDependenciesModal(model.getProject(), libraryProperties, false, false, null, remoteRepositoryDescriptions);
        LibraryTable.ModifiableModel tableModel = model.getModuleLibraryTable().getModifiableModel();
        Library library = tableModel.createLibrary(mavenCoordinates, RepositoryLibraryType.REPOSITORY_LIBRARY_KIND);
        Library.ModifiableModel libraryModel = library.getModifiableModel();
        if (roots.isEmpty()) {
            throw new IllegalStateException(String.format("No roots for '%s'", mavenCoordinates));
        }

        for (OrderRoot root : roots) {
            libraryModel.addRoot(root.getFile(), root.getType());
        }
        ((LibraryEx.ModifiableModelEx) libraryModel).setProperties(libraryProperties);

        LibraryOrderEntry libraryOrderEntry = model.findLibraryOrderEntry(library);
        if (libraryOrderEntry == null) {
            throw new IllegalStateException("Unable to find registered library " + mavenCoordinates);
        }
        libraryOrderEntry.setScope(dependencyScope);

        libraryModel.commit();
        tableModel.commit();
    }
    @Override
    protected String getTestDataPath() {
        return "src/test/testData/colorexp";
    }
}
