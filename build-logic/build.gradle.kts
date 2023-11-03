import io.gitlab.arturbosch.detekt.Detekt

plugins {
    // accessing version catalog here is blocked by IDE false-positive error
    // https://youtrack.jetbrains.com/issue/KTIJ-19369
    base
    // # Update detekt version in gradle/libs.versions.toml
    id("io.gitlab.arturbosch.detekt") version "1.18.1"
}

buildscript {

    /**
     *  workaround to load plugin classes once:
     *  https://youtrack.jetbrains.com/issue/KT-31643#focus=Comments-27-3510019.0-0
     *
     *  Causes this instead:
     *  An exception occurred applying plugin request [id: 'org.gradle.kotlin.kotlin-dsl', version: '2.1.7']
     * > Failed to apply plugin class 'org.jetbrains.kotlin.gradle.plugin.KotlinPluginWrapper'.
     * > Could not create an instance of type org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension.
     * > Companion
     */
    dependencies {
        classpath(libs.kotlinGradle)
    }
}

val detekt = tasks.named<Detekt>("detekt") {
    description = "Runs over whole code base without the starting overhead for each module."
    parallel = true
    setSource(files(projectDir))

    /**
     * About config:
     * yaml is a copy of https://github.com/detekt/detekt/blob/master/detekt-core/src/main/resources/default-detekt-config.yml
     * all rules are disabled by default, enabled one by one
     */
    config.setFrom(files(project.rootDir.resolve("../conf/detekt.yml")))
    buildUponDefaultConfig = true

    include("**/*.kt")
    include("**/*.kts")
    exclude("**/resources/**")
    exclude("**/build/**")
    reports {
        xml.enabled = false
        html.enabled = false
    }
}

tasks.named("check") {
    dependsOn(detekt)
}
