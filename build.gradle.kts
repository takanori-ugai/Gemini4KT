import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import io.gitlab.arturbosch.detekt.Detekt
import net.thebugmc.gradle.sonatypepublisher.PublishingType.USER_MANAGED
import org.jlleitschuh.gradle.ktlint.reporter.ReporterType

plugins {
    kotlin("jvm") version "1.9.24"
    kotlin("plugin.serialization") version "1.9.24"
    application
    id("org.jetbrains.dokka") version "1.9.20"
    id("io.gitlab.arturbosch.detekt") version "1.23.6"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("com.github.jk1.dependency-license-report") version "2.7"
    id("com.github.spotbugs") version "6.0.14"
    id("com.diffplug.spotless") version "6.25.0"
    id("org.jlleitschuh.gradle.ktlint") version "12.1.1"
    jacoco
    id("net.thebugmc.gradle.sonatype-central-portal-publisher") version "1.2.3"
}

group = "io.github.ugaikit"
version = "0.4.0"

repositories {
    mavenCentral()
}

java {
    withJavadocJar()
    withSourcesJar()
}

centralPortal {
//    version = "0.2.0"
    username = project.property("sonataUID") as String
    password = project.property("sonataPWD") as String
    publishingType = USER_MANAGED
    pom {
        name = "gemini4kt"
        description = "A lightweight Kotlin library for the Gemini API."
        url = "https://github.com/takanori-ugai/Gemini4KT"
        properties =
            mapOf(
                "myProp" to "value",
                "prop.with.dots" to "anotherValue",
            )
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

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
    implementation("io.github.oshai:kotlin-logging-jvm:6.0.9")
    runtimeOnly("ch.qos.logback:logback-classic:1.5.6")
    testImplementation("org.jetbrains.kotlin:kotlin-test")
}

application {
    mainClass.set("io.github.ugaikit.gemini4kt.ITTestKt")
}

tasks {
    "wrapper"(Wrapper::class) {
        distributionType = Wrapper.DistributionType.ALL
    }

    withType<Jar> {
        manifest {
            attributes(mapOf("Main-Class" to application.mainClass))
        }
    }

    compileKotlin {
        kotlinOptions.jvmTarget = "11"
        doLast { println("Finished compiling Kotlin source code") }
    }

    compileTestKotlin {
        kotlinOptions.jvmTarget = "11"
        doLast { println("Finished compiling Kotlin Test source code") }
    }

    compileJava {
        options.encoding = "UTF-8"
        options.compilerArgs.addAll(listOf("-Xlint:deprecation"))
        sourceCompatibility = "11"
        targetCompatibility = "11"
    }

    compileTestJava {
        options.encoding = "UTF-8"
        options.compilerArgs.addAll(listOf("-Xlint:deprecation"))
        sourceCompatibility = "11"
        targetCompatibility = "11"
    }

    jacocoTestReport {
        reports {
            xml.required.set(true)
            csv.required.set(true)
            html.outputLocation.set(layout.buildDirectory.dir("jacocoHtml"))
        }
    }

    withType<JacocoReport> {
        dependsOn("test")
        executionData(withType<Test>())
        classDirectories.setFrom(files(listOf("build/classes/kotlin/main")))
        //  sourceDirectories = files(listOf("src/main/java", "src/main/kotlin"))
        sourceDirectories.setFrom(files(listOf("src/main/java", "src/main/kotlin")))
    }

    dokkaHtml.configure {
        dokkaSourceSets {
            configureEach {
                jdkVersion.set(11)
                noStdlibLink.set(true)
            }
        }
    }

    dokkaJavadoc.configure {
        dokkaSourceSets {
            configureEach {
                jdkVersion.set(11)
                noStdlibLink.set(true)
            }
        }
    }

    test {
        testLogging {
//            exceptionFormat = TestExceptionFormat.FULL
            showStandardStreams = true
        }
        useJUnitPlatform()
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
            attributes["Main-Class"] = application.mainClass
        }
    }
}

ktlint {
//    setVersion("0.2.1")
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
    toolVersion = "0.8.11"
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
        googleJavaFormat("1.21.0") // has its own section below
        formatAnnotations() // fixes formatting of type annotations, see below
    }
}

kotlin {
    jvmToolchain(17)
}
