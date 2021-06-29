@file:Suppress("UnstableApiUsage")

import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinBasePluginWrapper

plugins {
    id("convention.libraries")
    id("org.gradle.test-retry")
}

val isCi: Provider<Boolean> = providers.gradleProperty("ci")
    .forUseAtConfigurationTime()
    .map { it.toBoolean() }

tasks.withType<Test>().configureEach {
    useJUnitPlatform()

    val halfOfAvailableProcessors = (Runtime.getRuntime().availableProcessors() / 2).takeIf { it > 0 } ?: 1
    maxParallelForks = halfOfAvailableProcessors

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
    if (isCi.getOrElse(false)) {
        retry {
            maxRetries.set(1)
            filter {
                includeAnnotationClasses.add("com.avito.test.Flaky")
            }
        }
    }
}

plugins.withType<KotlinBasePluginWrapper> {
    dependencies {
        add("testImplementation", libs.junitJupiterApi)
        add("testImplementation", libs.truth)

        add("testRuntimeOnly", libs.junitJupiterEngine)
        add("testRuntimeOnly", libs.junitPlatformRunner)
        add("testRuntimeOnly", libs.junitPlatformLauncher)
    }
}

plugins.withType<JavaTestFixturesPlugin> {

    configure<KotlinJvmProjectExtension> {
        target.compilations.getByName("test")
            .associateWith(target.compilations.getByName("testFixtures"))
    }
}
