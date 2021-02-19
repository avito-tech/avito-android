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

    failFast = true

    /**
     * fix for retrofit `WARNING: Illegal reflective access by retrofit2.Platform`
     * see square/retrofit/issues/3341
     */
    jvmArgs = listOf("--add-opens", "java.base/java.lang.invoke=ALL-UNNAMED")

    systemProperty("rootDir", "${project.rootDir}")

    val testProperties = listOf(
        "kubernetesUrl",
        "kubernetesToken",
        "kubernetesCaCertData",
        "kubernetesNamespace",
        "avito.slack.test.channel",
        "avito.slack.test.token",
        "avito.slack.test.workspace",
        "avito.elastic.endpoint",
        "avito.elastic.indexpattern",
        "teamcityBuildId"
    )
    testProperties.forEach { key ->
        val property = if (project.hasProperty(key)) {
            project.property(key)!!.toString()
        } else {
            ""
        }
        systemProperty(key, property)
    }
}

plugins.withType<KotlinBasePluginWrapper>() {
    dependencies {
        add("testImplementation", libs.junitJupiterApi)
        add("testImplementation", libs.truth)

        add("testRuntimeOnly", libs.junitJupiterEngine)
        add("testRuntimeOnly", libs.junitPlatformRunner)
        add("testRuntimeOnly", libs.junitPlatformLauncher)

        if (onlyInSubprojects() && project.name != "truth-extensions") {
            add("testImplementation", project(":subprojects:common:truth-extensions"))
        }
        add("testImplementation", testFixtures(project(":subprojects:common:logger")))
    }
}

/**
 * Workaround for:
 * Failed to generate type-safe Gradle model accessors for the following precompiled script plugins:
 * src/main/kotlin/convention.kotlin-android-app.gradle.kts
 *
 * Without this check generation will fail with UnknownProjectException,
 * because it's invoked on conventions module where `truth-extensions` not exists and check was not strong enough
 */
fun onlyInSubprojects(): Boolean {
    return project.path.contains("subprojects")
}

plugins.withType<JavaTestFixturesPlugin>() {

    configure<KotlinJvmProjectExtension> {
        target.compilations.getByName("test")
            .associateWith(target.compilations.getByName("testFixtures"))
    }
}
