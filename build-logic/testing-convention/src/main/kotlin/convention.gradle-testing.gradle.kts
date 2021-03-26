@file:Suppress("UnstableApiUsage")

import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinPluginWrapper

plugins {
    id("kotlin")
    id("convention.libraries")
    id("java-gradle-plugin")
    idea
}

val gradleTest: SourceSet by sourceSets.creating

val gradleTestJarTask = tasks.register<Jar>(gradleTest.jarTaskName) {
    archiveClassifier.set("gradle-tests")
    from(gradleTest.output)
}

val kotlinVersion = plugins.getPlugin(KotlinPluginWrapper::class.java).kotlinPluginVersion

val testTimeoutSeconds = 600

val artifactoryUrl: Provider<String> = providers.gradleProperty("artifactoryUrl").forUseAtConfigurationTime()

val gradleTestTask = tasks.register<Test>("gradleTest") {
    description = "Runs gradle test kit tests"
    group = "verification"

    testClassesDirs = gradleTest.output.classesDirs
    classpath = configurations[gradleTest.runtimeClasspathConfigurationName] + files(gradleTestJarTask)

    useJUnitPlatform()

    maxParallelForks = 1

    failFast = false

    systemProperty("rootDir", "${project.rootDir}")
    systemProperty("kotlinVersion", kotlinVersion)
    systemProperty("compileSdkVersion", libs.compileSdkVersion)
    systemProperty("buildToolsVersion", libs.buildToolsVersion)
    systemProperty("androidGradlePluginVersion", libs.androidGradlePluginVersion)
    systemProperty("artifactoryUrl", artifactoryUrl.getOrElse(""))
    systemProperty("isTest", true)

    systemProperty("junit.jupiter.execution.timeout.default", testTimeoutSeconds)
}

dependencies {
    "gradleTestImplementation"(gradleTestKit())
    "gradleTestImplementation"(libs.junitJupiterApi)
    "gradleTestImplementation"(libs.truth)
    "gradleTestRuntimeOnly"(libs.junitJupiterEngine)
    "gradleTestRuntimeOnly"(libs.junitPlatformRunner)
    "gradleTestRuntimeOnly"(libs.junitPlatformLauncher)
}

gradlePlugin {
    testSourceSets(gradleTest)
}

// make idea to treat gradleTest as test sources
idea {
    gradleTest.allSource.srcDirs.forEach { srcDir ->
        module.testSourceDirs = module.testSourceDirs + srcDir
    }

    module.scopes["TEST"]?.get("plus")?.plusAssign(
        listOf(
            configurations.getByName(gradleTest.compileClasspathConfigurationName),
            configurations.getByName(gradleTest.runtimeClasspathConfigurationName)
        )
    )
}

tasks.check.configure {
    dependsOn(gradleTestTask)
}

configure<KotlinJvmProjectExtension> {

    target.compilations.getByName("gradleTest")
        .associateWith(target.compilations.getByName("main"))

    plugins.withType<JavaTestFixturesPlugin>() {
        target.compilations.getByName("gradleTest")
            .associateWith(target.compilations.getByName("testFixtures"))
    }
}
