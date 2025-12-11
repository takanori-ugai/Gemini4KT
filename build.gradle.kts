import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import io.gitlab.arturbosch.detekt.Detekt
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jlleitschuh.gradle.ktlint.reporter.ReporterType

plugins {
    kotlin("multiplatform") version "2.2.21"
    kotlin("plugin.serialization") version "2.2.21"
    id("org.jetbrains.dokka") version "2.1.0"
    id("org.jetbrains.dokka-javadoc") version "2.1.0"
    id("io.gitlab.arturbosch.detekt") version "1.23.8"
    id("com.gradleup.shadow") version "9.3.0"
    id("com.github.jk1.dependency-license-report") version "3.0.1"
    id("com.github.spotbugs") version "6.4.7"
    id("com.diffplug.spotless") version "8.1.0"
    id("org.jlleitschuh.gradle.ktlint") version "14.0.1"
    jacoco
    // id("net.thebugmc.gradle.sonatype-central-portal-publisher") version "1.2.4"
    `maven-publish`
}

group = "io.github.ugaikit"
version = "0.7.0"

repositories {
    mavenCentral()
}

kotlin {
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

    sourceSets {
        commonMain.dependencies {
            implementation("org.jetbrains.kotlin:kotlin-reflect:2.2.21")
            implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")
            implementation("io.github.oshai:kotlin-logging:7.0.13")
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
        }
        commonTest.dependencies {
            implementation("org.jetbrains.kotlin:kotlin-test")
            implementation("io.mockk:mockk:1.14.7")
        }
        val jvmMain by getting {
            dependencies {
                runtimeOnly("ch.qos.logback:logback-classic:1.5.21")
            }
        }
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
            sourceDirectories.setFrom(files("src/jvmMain/kotlin", "src/commonMain/kotlin"))
            executionData.setFrom(layout.buildDirectory.file("jacoco/jvmTest.exec"))
        }

    // Configure JVM test task
    named<Test>("jvmTest") {
        testLogging {
//            exceptionFormat = TestExceptionFormat.FULL
            showStandardStreams = true
        }
        useJUnitPlatform()
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

    withType<ShadowJar> {
        manifest {
            attributes["Main-Class"] = "io.github.ugaikit.gemini4kt.ITTestKt"
        }
        minimize()
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
