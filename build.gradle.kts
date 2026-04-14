import io.gitlab.arturbosch.detekt.Detekt
import org.gradle.jvm.tasks.Jar
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinAndroidTarget
import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsExec
import org.jlleitschuh.gradle.ktlint.reporter.ReporterType

plugins {
    kotlin("multiplatform") version "2.3.20"
    kotlin("plugin.serialization") version "2.3.20"
    id("org.jetbrains.dokka") version "2.2.0"
//    id("org.jetbrains.dokka-javadoc") version "2.1.0"
    id("com.android.kotlin.multiplatform.library") version "9.1.1"
    id("io.gitlab.arturbosch.detekt") version "1.23.8"
    id("com.github.jk1.dependency-license-report") version "3.1.2"
    id("com.github.spotbugs") version "6.4.8"
    id("com.diffplug.spotless") version "8.4.0"
    id("org.jlleitschuh.gradle.ktlint") version "14.2.0"
    id("com.github.gmazzo.buildconfig") version "6.0.9"
    jacoco
    // id("net.thebugmc.gradle.sonatype-central-portal-publisher") version "1.2.4"
    id("com.vanniktech.maven.publish") version "0.36.0"
}

group = "io.github.ugaikit"
version = "0.8.0"

repositories {
    google()
    mavenCentral()
}

kotlin {
    applyDefaultHierarchyTemplate()

    targets.configureEach {
        compilations.configureEach {
            compileTaskProvider.configure {
                compilerOptions {
                    freeCompilerArgs.add("-Xexpect-actual-classes")
                }
            }
        }
    }

    jvm {
        compilations.all {
            compileTaskProvider.configure {
                compilerOptions {
                    jvmTarget.set(JvmTarget.JVM_11)
                }
            }
        }
        testRuns.named("test") {
            executionTask.configure {
                useJUnitPlatform()
            }
        }
        @OptIn(org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi::class)
        mainRun {
            mainClass.set("io.github.ugaikit.gemini4kt.ITTestKt")
        }
    }
//    @OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)
//    wasmJs {
//        binaries.executable()
//        nodejs {}
//    }

    js(IR) {
        binaries.library()
        binaries.executable()
        generateTypeScriptDefinitions()
        nodejs {}
        compilerOptions {
            freeCompilerArgs.add("-Xenable-suspend-function-exporting")
        }
    }

    linuxX64 {
        binaries {
            executable {
                entryPoint = "io.github.ugaikit.gemini4kt.samples.main"
            }
        }
    }
    mingwX64 {
        binaries {
            executable {
                entryPoint = "io.github.ugaikit.gemini4kt.samples.main"
            }
        }
    }
    iosX64 {
        binaries {
            executable {
                entryPoint = "io.github.ugaikit.gemini4kt.samples.main"
            }
        }
    }
    iosArm64()
    iosSimulatorArm64()

    @OptIn(org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi::class)
    android {
        namespace = "io.github.ugaikit.gemini4kt"
        compileSdk = 33
        minSdk = 24

        withJava() // enable java compilation support
        withHostTestBuilder {}.configure {}
        withDeviceTestBuilder {
            sourceSetTreeName = "test"
        }
    }
    targets.withType<KotlinAndroidTarget>().configureEach {
        publishLibraryVariants("release")
    }
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.10.0")
                implementation("io.github.oshai:kotlin-logging:8.0.01")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
                implementation("io.ktor:ktor-client-core:3.4.2")
                implementation("io.ktor:ktor-client-content-negotiation:3.4.2")
                implementation("io.ktor:ktor-serialization-kotlinx-json:3.4.2")
                implementation("io.ktor:ktor-client-logging:3.4.2")
                implementation("io.ktor:ktor-client-websockets:3.4.2")
                implementation("org.jetbrains.kotlinx:kotlinx-io-core:0.9.0")
            }
            buildConfig {
                packageName("io.github.ugaikit.gemini4kt")
                useKotlinOutput {
                    internalVisibility = true
                    topLevelConstants = true
                }
                buildConfigField("String", "GEMINI4KT_VERSION", "\"${project.version}\"")
            }
        }
        val jvmCommonMain by creating {
            dependsOn(commonMain)
            dependencies {
                implementation("org.jetbrains.kotlin:kotlin-reflect:2.3.20")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation("org.jetbrains.kotlin:kotlin-test")
                implementation("io.ktor:ktor-client-mock:3.4.2")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2")
            }
        }
        val jvmMain by getting {
            dependsOn(jvmCommonMain)
            dependencies {
                runtimeOnly("ch.qos.logback:logback-classic:1.5.32")
                implementation("io.ktor:ktor-client-cio:3.4.2")
            }
        }
        val jvmTest by getting {
            dependencies {
                implementation("io.mockk:mockk:1.14.9")
            }
        }
//        val wasmJsMain by getting {
//            dependencies {
//                // implementation("io.ktor:ktor-client-core:3.0.3") // Already in commonMain
//            }
//        }

        val jsMain by getting
        val jsTest by getting
        val iosMain by getting {
            dependencies {
                implementation("io.ktor:ktor-client-darwin:3.4.2")
            }
        }
        val mingwX64Main by getting {
            dependencies {
                implementation("io.ktor:ktor-client-curl:3.4.2")
            }
        }
        val linuxX64Main by getting {
            dependencies {
                implementation("io.ktor:ktor-client-curl:3.4.2")
            }
        }
        val androidMain by getting {
            dependsOn(jvmCommonMain)
            dependencies {
                implementation("io.ktor:ktor-client-android:3.4.2")
            }
        }
    }
}

// Tasks configuration
tasks {
    "wrapper"(Wrapper::class) {
        distributionType = Wrapper.DistributionType.ALL
    }

    named("jsNodeProductionLibraryDistribution") {
        dependsOn("jsProductionExecutableCompileSync")
    }

    register<JavaExec>("jvmRunFunctionExample3") {
        group = "application"
        description = "Run FunctionExample3Runner on the JVM target"
        dependsOn("jvmJar")
        mainClass.set("io.github.ugaikit.gemini4kt.samples.FunctionExample3Runner")
        val jvmJar = named<Jar>("jvmJar")
        classpath =
            files(
                jvmJar.flatMap { jar -> jar.archiveFile },
                configurations.named("jvmRuntimeClasspath").get(),
            )
    }

    val jacocoTestReport =
        register<JacocoReport>("jacocoTestReport") {
            reports {
                xml.required.set(true)
                csv.required.set(true)
                // html.outputLocation.set(layout.buildDirectory.dir("jacocoHtml"))
            }
            dependsOn("jvmTest")
            // sourceSets(kotlin.sourceSets.jvmMain) // might need adjustment
            classDirectories.setFrom(files(layout.buildDirectory.dir("classes/kotlin/jvm/main")))
            sourceDirectories.setFrom(files("src/jvmMain/kotlin", "src/jvmCommonMain/kotlin", "src/commonMain/kotlin"))
            executionData.setFrom(layout.buildDirectory.file("jacoco/jvmTest.exec"))
        }

    // Configure JVM test task
    named<Test>("jvmTest") {
        testLogging {
//            exceptionFormat = TestExceptionFormat.FULL
            showStandardStreams = true
        }
        useJUnitPlatform()
        jvmArgs("-XX:+EnableDynamicAgentLoading")
        finalizedBy(jacocoTestReport)
    }

    withType<Detekt>().configureEach {
        // Target version of the generated JVM bytecode. It is used for type resolution.
        jvmTarget = "11"
        reports {
            // observe findings in your browser with structure and code snippets
            html.required.set(true)
            // checkstyle like format mainly for integrations like Jenkins
            xml.required.set(true)
            // similar to the console output, contains issue signature to manually edit baseline files
            txt.required.set(true)
            // standardized SARIF format (https://sarifweb.azurewebsites.net/) to support integrations
            // with Github Code Scanning
            sarif.required.set(true)
        }
    }
}

dokka.dokkaSourceSets {
    configureEach {
        jdkVersion.set(11)
        enableJdkDocumentationLink.set(false)
        enableKotlinStdLibDocumentationLink.set(false)
    }
}

ktlint {
    verbose.set(true)
    outputToConsole.set(true)
    coloredOutput.set(true)
    reporters {
        reporter(ReporterType.CHECKSTYLE)
        reporter(ReporterType.JSON)
        reporter(ReporterType.HTML)
    }
    filter {
        exclude("**/style-violations.kt")
    }
}

detekt {
    source.from(files("src/**/kotlin"))
    buildUponDefaultConfig = true // preconfigure defaults
    allRules = false // activate all available (even unstable) rules.
    source.setFrom(
        files(
            "src/commonMain/kotlin",
            "src/commonTest/kotlin",
            "src/linuxX86Main/kotlin",
            "src/jvmMain/kotlin",
            "src/jvmCommonMain/kotlin",
            "src/jvmTest/kotlin",
            "src/androidDeviceTest/kotlin",
            "src/androidMain/kotlin",
            "src/jsTest/kotlin",
            "src/nativeMain/kotlin",
            "src/wasmJsMain/kotlin",
            "src/androidHostTest/kotlin",
            "src/jsMain/kotlin",
            "src/nativeTest/kotlin",
            "src/wasmJsTest/kotlin",
        ),
    )
    // point to your custom config defining rules to run, overwriting default behavior
    config.from(files("$projectDir/config/detekt/detekt.yml"))
//    baseline = file("$projectDir/config/baseline.xml") // a way of suppressing issues before introducing detekt
}

jacoco {
    toolVersion = "0.8.13"
}

spotbugs {
    ignoreFailures.set(true)
}

spotless {
    java {
        target("src/*/java/**/*.java")
        targetExclude("src/jte-classes/**/*.java", "jte-classes/**/*.java")
        // Use the default importOrder configuration
        importOrder()
        removeUnusedImports()

        // Choose one of these formatters.
        googleJavaFormat("1.32.0") // has its own section below
        formatAnnotations() // fixes formatting of type annotations, see below
    }
}

tasks.named<NodeJsExec>("jsNodeProductionRun") {
    val cliArgs = providers.gradleProperty("cliArgs").orNull

    if (!cliArgs.isNullOrBlank()) {
        println("Gradle: 'CLI_ARGS' is '$cliArgs'")
        environment("CLI_ARGS", cliArgs)
    }
}

mavenPublishing {
    // Maven Central に公開する場合の設定
    publishToMavenCentral()

    signAllPublications()

    coordinates("io.github.ugaikit", "gemini4kt", version.toString())

    pom {
        name = "gemini4kt"
        description = "A lightweight Kotlin library for the Gemini API."
        url = "https://github.com/takanori-ugai/Gemini4KT"
        inceptionYear.set("2025")
        licenses {
            license {
                name = "The Apache License, Version 2.0"
                url = "http://www.apache.org/licenses/LICENSE-2.0.txt"
            }
        }
        developers {
            developer {
                id = "takanori-ugai"
                name = "Takanori Ugai"
                email = "ugai@fujitsu.com"
            }
        }
        scm {
            connection = "scm:https://github.com/takanori-ugai/Gemini4KT.git"
            developerConnection = "scm:https://github.com/takanori-ugai/Gemini4KT.git"
            url = "https://github.com/takanori-ugai/Gemini4KT"
        }
    }
}
