@file:Suppress("UnstableApiUsage")

import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinBasePluginWrapper

plugins {
    id("convention.libraries")
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()

    @Suppress("MagicNumber")
    maxParallelForks = 8

    failFast = false

    /**
     * fix for retrofit `WARNING: Illegal reflective access by retrofit2.Platform`
     * see square/retrofit/issues/3341
     */
    jvmArgs = listOf("--add-opens", "java.base/java.lang.invoke=ALL-UNNAMED")
}

plugins.withType<KotlinBasePluginWrapper>() {
    dependencies {
        add("testImplementation", libs.junitJupiterApi)
        add("testImplementation", libs.truth)

        add("testRuntimeOnly", libs.junitJupiterEngine)
        add("testRuntimeOnly", libs.junitPlatformRunner)
        add("testRuntimeOnly", libs.junitPlatformLauncher)
    }
}

plugins.withType<JavaTestFixturesPlugin>() {

    configure<KotlinJvmProjectExtension> {
        target.compilations.getByName("test")
            .associateWith(target.compilations.getByName("testFixtures"))
    }
}
