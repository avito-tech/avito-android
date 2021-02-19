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

val gradleTestTask = tasks.register<Test>("gradleTest") {
    description = "Runs gradle test kit tests"
    group = "verification"

    testClassesDirs = gradleTest.output.classesDirs
    classpath = configurations[gradleTest.runtimeClasspathConfigurationName] + files(gradleTestJarTask)

    useJUnitPlatform()

    @Suppress("MagicNumber")
    maxParallelForks = 8

    failFast = true

    systemProperty("rootDir", "${project.rootDir}")
    systemProperty("kotlinVersion", kotlinVersion)
    systemProperty("compileSdkVersion", libs.compileSdkVersion)
    systemProperty("buildToolsVersion", libs.buildToolsVersion)
    systemProperty("androidGradlePluginVersion", libs.androidGradlePluginVersion)

    /**
     * IDEA adds an init script, using it to define if it is an IDE run
     * used in `:test-project`
     */
    systemProperty(
        "isInvokedFromIde",
        gradle.startParameter.allInitScripts.find { it.name.contains("ijtestinit") } != null
    )

    systemProperty("isTest", true)

    systemProperty("junit.jupiter.execution.timeout.default", testTimeoutSeconds)
}

dependencies {
    "gradleTestImplementation"(gradleTestKit())
    "gradleTestImplementation"(testFixtures(project(":subprojects:common:logger")))
    "gradleTestImplementation"(project(":subprojects:common:truth-extensions"))
    "gradleTestImplementation"(project(":subprojects:gradle:git"))
    "gradleTestImplementation"(project(":subprojects:gradle:test-project"))
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
