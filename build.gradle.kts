import com.google.common.io.Files
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
    }
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = "11"
    }
    this.getByName("setupInstrumentCode").actions.clear();
    this.getByName("instrumentCode").actions.clear();
    this.getByName("postInstrumentCode").actions.clear();
    patchPluginXml {
        sinceBuild.set("222.3345.118")
        untilBuild.set("232")
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
val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    languageVersion = "1.7"
}
// Generated content
sourceSets {
    main {
        java {

            file("src/parts").listFiles()?.toMutableList().let{it?: mutableListOf() }.also {
                it.add(file("src/main/gen"))
                it+=srcDirs;
                java.srcDirs(it)
            }
            println(srcDirs)
        }
    }
}