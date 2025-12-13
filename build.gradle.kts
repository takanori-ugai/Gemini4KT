import io.gitlab.arturbosch.detekt.Detekt
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jlleitschuh.gradle.ktlint.reporter.ReporterType

plugins {
    kotlin("multiplatform") version "2.2.21"
    kotlin("plugin.serialization") version "2.2.21"
    id("com.android.library") version "8.12.0"
    id("org.jetbrains.dokka") version "2.1.0"
    id("org.jetbrains.dokka-javadoc") version "2.1.0"
    id("io.gitlab.arturbosch.detekt") version "1.23.8"
    id("com.github.jk1.dependency-license-report") version "3.0.1"
    id("com.github.spotbugs") version "6.4.8"
    id("com.diffplug.spotless") version "8.1.0"
    id("org.jlleitschuh.gradle.ktlint") version "14.0.1"
    jacoco
    // id("net.thebugmc.gradle.sonatype-central-portal-publisher") version "1.2.4"
    `maven-publish`
}

group = "io.github.ugaikit"
version = "0.7.0"

repositories {
    google()
    mavenCentral()
}

kotlin {
    applyDefaultHierarchyTemplate()

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
    @OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)
    wasmJs {
        binaries.executable()
        nodejs {}
    }
    androidTarget {
        publishLibraryVariants("release")
        compilations.all {
            compileTaskProvider.configure {
                compilerOptions {
                    jvmTarget.set(JvmTarget.JVM_11)
                }
            }
        }
    }
    js {
        binaries.executable()
        nodejs {}
    }

    mingwX64 {
        binaries {
            executable() // Generates an .exe file
        }
    }
    iosX64()
    iosArm64()
    iosSimulatorArm64()

    linuxX64()

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.0")
                implementation("io.github.oshai:kotlin-logging:7.0.3")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.1")
                implementation("io.ktor:ktor-client-core:3.0.3")
                implementation("io.ktor:ktor-client-content-negotiation:3.0.3")
                implementation("io.ktor:ktor-serialization-kotlinx-json:3.0.3")
                implementation("io.ktor:ktor-client-logging:3.0.3")
                implementation("io.ktor:ktor-client-websockets:3.0.3")
                implementation("org.jetbrains.kotlinx:kotlinx-io-core:0.8.2")
            }
        }
        val jvmCommonMain by creating {
            dependsOn(commonMain)
            dependencies {
                implementation("org.jetbrains.kotlin:kotlin-reflect:2.1.0")
            }
        }
        val nativeMain by getting
        val commonTest by getting {
            dependencies {
                implementation("org.jetbrains.kotlin:kotlin-test")
                implementation("io.ktor:ktor-client-mock:3.0.3")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.1")
            }
        }
        val jvmMain by getting {
            dependsOn(jvmCommonMain)
            dependencies {
                runtimeOnly("ch.qos.logback:logback-classic:1.5.16")
                implementation("io.ktor:ktor-client-cio:3.0.3")
            }
        }
        val jvmTest by getting {
            dependencies {
                implementation("io.mockk:mockk:1.13.16")
            }
        }
        val wasmJsMain by getting {
            dependencies {
                // implementation("io.ktor:ktor-client-core:3.0.3") // Already in commonMain
            }
        }
        val androidMain by getting {
            dependsOn(jvmCommonMain)
            dependencies {
                implementation("io.ktor:ktor-client-android:3.0.3")
            }
        }
        val linuxX64Main by getting {
            // dependsOn(nativeMain) // Already depends on nativeMain via default hierarchy
            dependencies {
                implementation("io.ktor:ktor-client-cio:3.0.3")
            }
        }
        val iosMain by getting {
            // dependsOn(nativeMain) // Already depends on nativeMain via default hierarchy
            dependencies {
                implementation("io.ktor:ktor-client-darwin:3.0.3")
            }
        }

        val mingwX64Main by getting
        val mingwX64Test by getting

        val jsMain by getting
        val jsTest by getting
    }
}

// Tasks configuration
tasks {
    "wrapper"(Wrapper::class) {
        distributionType = Wrapper.DistributionType.ALL
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

android {
    namespace = "io.github.ugaikit.gemini4kt"
    compileSdk = 34
    defaultConfig {
        minSdk = 21
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}
