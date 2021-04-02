@file:Suppress("UnstableApiUsage")

import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension

plugins {
    id("convention.junit5")
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()

    @Suppress("MagicNumber")
    maxParallelForks = 8

    failFast = false

    /**
     * fix for multiple `WARNING: Illegal reflective access`
     */
    jvmArgs = listOf(
        "--add-opens",
        "java.base/java.lang=ALL-UNNAMED",
        "--add-opens",
        "java.base/java.lang.invoke=ALL-UNNAMED",
        "--add-opens",
        "java.base/java.util=ALL-UNNAMED"
    )

    /**
     * IDEA adds an init script, using it to define if it is an IDE run
     * used in `:test-project`
     */
    systemProperty(
        "isInvokedFromIde",
        gradle.startParameter.allInitScripts.find { it.name.contains("ijtestinit") } != null
    )
}

plugins.withType<JavaTestFixturesPlugin> {

    configure<KotlinJvmProjectExtension> {
        target.compilations.getByName("test")
            .associateWith(target.compilations.getByName("testFixtures"))
    }
}
