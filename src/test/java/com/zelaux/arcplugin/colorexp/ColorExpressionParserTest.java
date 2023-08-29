package com.zelaux.arcplugin.colorexp;

import com.intellij.codeInsight.daemon.*;
import com.intellij.codeInsight.daemon.impl.*;
import com.intellij.jarRepository.*;
import com.intellij.lang.java.*;
import com.intellij.openapi.roots.*;
import com.intellij.openapi.roots.impl.libraries.*;
import com.intellij.openapi.roots.libraries.*;
import com.intellij.openapi.roots.libraries.ui.*;
import com.intellij.testFramework.fixtures.*;
import org.jetbrains.annotations.*;
import org.jetbrains.idea.maven.utils.library.*;
import org.junit.*;

import java.util.*;

public class ColorExpressionParserTest extends LightPlatformCodeInsightFixture4TestCase{
    public static void addFromMaven(@NotNull ModifiableRootModel model,
                                    String mavenCoordinates,
                                    boolean includeTransitiveDependencies,
                                    DependencyScope dependencyScope,
                                    List<RemoteRepositoryDescription> remoteRepositoryDescriptions){
        RepositoryLibraryProperties libraryProperties = new RepositoryLibraryProperties(mavenCoordinates, includeTransitiveDependencies);
        Collection<OrderRoot> roots =
        JarRepositoryManager.loadDependenciesModal(model.getProject(), libraryProperties, false, false, null, remoteRepositoryDescriptions);
        LibraryTable.ModifiableModel tableModel = model.getModuleLibraryTable().getModifiableModel();
        Library library = tableModel.createLibrary(mavenCoordinates, RepositoryLibraryType.REPOSITORY_LIBRARY_KIND);
        Library.ModifiableModel libraryModel = library.getModifiableModel();
        if(roots.isEmpty()){
            throw new IllegalStateException(String.format("No roots for '%s'", mavenCoordinates));
        }

        for(OrderRoot root : roots){
            libraryModel.addRoot(root.getFile(), root.getType());
        }
        ((LibraryEx.ModifiableModelEx)libraryModel).setProperties(libraryProperties);

        LibraryOrderEntry libraryOrderEntry = model.findLibraryOrderEntry(library);
        if(libraryOrderEntry == null){
            throw new IllegalStateException("Unable to find registered library " + mavenCoordinates);
        }
        libraryOrderEntry.setScope(dependencyScope);

        libraryModel.commit();
        tableModel.commit();
    }

    @Test
    public void testJava(){
        ModuleRootModificationUtil.updateModel(getModule(), model ->
        addFromMaven(model, "com.github.Anuken.Arc:arc-core:v145", false,
        DependencyScope.COMPILE,
        List.of(
        new RemoteRepositoryDescription("zelauxGithub", "ZelauxGithub", "https://raw.githubusercontent.com/Zelaux/MindustryRepo/master/repository")
        )

        )
        );
        myFixture.configureByFile("Test.java");
        List<LineMarkerProvider> lineMarkerProviders = LineMarkerProviders.getInstance().allForLanguage(JavaLanguage.INSTANCE);
        Collection<LineMarkerInfo<?>> markerInfos = LineMarkersPass.queryLineMarkers(myFixture.getFile(), myFixture.getDocument(myFixture.getFile()));
        List<HighlightInfo> infos = myFixture.doHighlighting();
        System.out.println("it");
        for(HighlightInfo info : infos){

        }

    }

    @Test
    public void testKotlin(){
        ModuleRootModificationUtil.updateModel(getModule(), model ->
        addFromMaven(model, "com.github.Anuken.Arc:arc-core:v145", false,
        DependencyScope.COMPILE,
        List.of(
        new RemoteRepositoryDescription("zelauxGithub", "ZelauxGithub", "https://raw.githubusercontent.com/Zelaux/MindustryRepo/master/repository")
        )

        )
        );
        myFixture.configureByFile("Test.kt");
//        List<LineMarkerProvider> lineMarkerProviders = LineMarkerProviders.getInstance().allForLanguage(JavaLanguage.INSTANCE);
        Collection<LineMarkerInfo<?>> markerInfos = LineMarkersPass.queryLineMarkers(myFixture.getFile(), myFixture.getDocument(myFixture.getFile()));
//        List<HighlightInfo> infos = myFixture.doHighlighting();
        System.out.println("{{{");
        for(LineMarkerInfo<?> info : markerInfos){
            System.out.print("\t");
            System.out.println(info);
        }
        System.out.println("}}}");
        Assert.assertEquals(3,markerInfos.size());

    }

    @Override
    protected String getTestDataPath(){
        return "src/test/testData/colorexp";
    }
}
