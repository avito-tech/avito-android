import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.plugin.KotlinPluginWrapper

plugins {
    id("convention.libraries")
}

// todo more precise configuration for gradle plugins, no need for gradle testing in common kotlin modules

dependencies {
    add("testImplementation", gradleTestKit())
}

val kotlinVersion = plugins.getPlugin(KotlinPluginWrapper::class.java).kotlinPluginVersion

val testTimeoutSeconds = 600

tasks.withType<Test>().configureEach {
    systemProperty("kotlinVersion", kotlinVersion)
    systemProperty("compileSdkVersion", libs.compileSdkVersion)
    systemProperty("buildToolsVersion", libs.buildToolsVersion)
    systemProperty("androidGradlePluginVersion", libs.androidGradlePluginVersion)

    /**
     * IDEA добавляет специальный init script, по нему понимаем что запустили в IDE
     * используется в `:test-project`
     */
    systemProperty(
        "isInvokedFromIde",
        gradle.startParameter.allInitScripts.find { it.name.contains("ijtestinit") } != null
    )

    systemProperty("isTest", true)

    systemProperty("junit.jupiter.execution.timeout.default", testTimeoutSeconds)
}
