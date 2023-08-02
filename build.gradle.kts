import org.gradle.api.internal.file.copy.DefaultCopySpec
import org.gradle.internal.FileUtils
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.6.20"
    id("org.jetbrains.intellij") version "1.8.0"
}
val arcVersion = "v141.1"

group = "com.github.Zelaux"
version = "0.5"

repositories {
    mavenCentral()
    maven { url = uri("https://jitpack.io") }
}
dependencies {
    implementation("com.github.Anuken.Arc:arc-core:$arcVersion")
//    implementation("edu.brown.cs.burlap:arc-burlap:3.0.1")
}

// Configure Gradle IntelliJ Plugin
// Read more: https://plugins.jetbrains.com/docs/intellij/tools-gradle-intellij-plugin.html
intellij {
    version.set("2022.2.3")
//    type.set("IC") // Target IDE Platform
    type.set("IC") // Target IDE Platform

//    plugins.set(listOf(/* Plugin Dependencies */))
    plugins.set(listOf("java", "properties", /*"DevKit",*/"org.intellij.intelliLang"))
}
sourceSets {
    main {
        java {
            srcDirs += arrayOf("src/main/java").map { file(it) }
            srcDirs += arrayOf("src/main/kotlin").map { file(it) }
        }
    }
}
tasks.register("copyPlugin") {
    dependsOn("prepareSandbox")
    doFirst {
//        delete(file("D:\\my apps\\IntelliJ IDEA Community Edition 2022.2\\plugins\\ZelauxArcPlugin"))
    }
    doLast {
//        FileUtils
        copy {
            from(File("build/idea-sandbox/ZelauxArcPlugin")) {
                include("/**")
            }
            into(File("D:/my apps/IntelliJ IDEA Community Edition 2022.2/plugins/ZelauxArcPlugin"))
            println(file("D:\\my apps\\IntelliJ IDEA Community Edition 2022.2\\plugins\\ZelauxArcPlugin").absolutePath)
        }
    }
}
tasks {


    // Set the JVM compatibility versions
    withType<JavaCompile> {
        sourceCompatibility = "11"
        targetCompatibility = "11"


        val compilerArgs = options.compilerArgs
        compilerArgs.addAll(listOf("--add-exports", "java.desktop/sun.awt.image=ALL-UNNAMED"))
//        compilerArgs.addAll(listOf("--add-exports/java.desktop/sun.awt.image=ALL-UNNAMED"))
//        compilerArgs.addAll(listOf("--add-exports java.desktop/sun.awt.image=ALL-UNNAMED"))
    }
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = "11"

//        compilerArgs.addAll(listOf("-opt-in","kotlin.RequiresOptIn"))
    }
    this.getByName("setupInstrumentCode").actions.clear();
    this.getByName("instrumentCode").actions.clear();
    this.getByName("postInstrumentCode").actions.clear();
    val t_tasks = this;
    this.getByName("setupDependencies").doFirst {
        t_tasks.jar {
//            from {
            /*configurations["compileClasspath"].map { if (it.isDirectory) it else zipTree(it) }
                .forEach { from(it) }*/
//            }
        }
//    this.getByName("jar").dependsOn.clear()
    };
    patchPluginXml {
        sinceBuild.set("213")
        untilBuild.set("223.*")
    }

    signPlugin {
        certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
        privateKey.set(System.getenv("PRIVATE_KEY"))
        password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
    }

    publishPlugin {
        token.set(System.getenv("PUBLISH_TOKEN"))
    }
}
project(rootProject.path) {

    tasks.jar {
        /*var clazz: Class<out Any?> = javaClass;
        println(clazz)
        while (clazz.superclass != null && clazz.simpleName != "AbstractCopyTask") {
            clazz = clazz.superclass
            println(clazz)
        }
//    println(javaClass)
//    println(javaClass.superclass)
//    println(javaClass.superclass.superclass)
//    println(javaClass.superclass.superclass.superclass)
//    println(javaClass.superclass.superclass.superclass.superclass)
//    println(javaClass.superclass.superclass.superclass.superclass.superclass)
//    println("declaredFields: " + clazz.declaredFields.joinToString { it.name })
//    println("fields: " + clazz.fields.joinToString { it.name })
//    println("declaredMethods: " + clazz.declaredMethods.joinToString { it.name })
//    println("methods: " + clazz.methods.joinToString { it.name })
        val field = clazz.getDeclaredField("mainSpec")
        field.isAccessible = true;
//    getMainSpec()
//    val mainSpec=field[this] as org.gradle.api.internal.file.copy.CopySpecInternal;
        val mainSpec = field[this] as DefaultCopySpec;
        println("mainSpec: " + mainSpec)
        println("mainSpec: " + mainSpec.javaClass)
        val sourcePathsField = DefaultCopySpec::class.java.getDeclaredField("sourcePaths")
        sourcePathsField.isAccessible=true
        val sourcePaths = sourcePathsField[mainSpec]  as org.gradle.api.internal.file.collections.DefaultConfigurableFileCollection

        println("mainSpec.sourcePaths: " + mainSpec.sourcePaths)
        println("mainSpec.sourcePaths.clazz: " + mainSpec.sourcePaths.javaClass)
        println("mainSpec.sourcePaths2: " + sourcePaths)
        println("mainSpec.sourcePaths2.clazz: " + sourcePaths.javaClass)*/
        /*   val idea = rootProject.configurations["idea"]
           val ideaPlugins = rootProject.configurations["ideaPlugins"]
           rootProject.configurations.remove(ideaPlugins)
           configurations["compileClasspath"].map { if (it.isDirectory) it else zipTree(it) }.forEach {
               from(it)
   //        mainSpec.from(it)
           }
           rootProject.configurations.add(ideaPlugins)*/
    }
}
/*    from {

//            configurations.getAt("compile"){
//                this.
//                collect { it.isDirectory() ? it : zipTree(it) }
//            }
    }*/
/*jar {
    from {
        configurations.compile.collect { it.isDirectory() ? it : zipTree(it) }
    }
}*/
val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    languageVersion = "1.7"
}
// Generated content
sourceSets.main{

}
sourceSets {
    main {
        java {

            (srcDirs+setOf(file("src/main/gen"))).let{
                java.srcDirs(it)
                println(it)
//                srcDirs.addAll(it)
            }
            println(srcDirs)
        }
    }
}