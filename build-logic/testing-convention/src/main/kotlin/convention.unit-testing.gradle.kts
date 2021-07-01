import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinBasePluginWrapper

plugins {
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
    jvmArgs(
        listOf(
            "--add-opens",
            "java.base/java.lang=ALL-UNNAMED",
            "--add-opens",
            "java.base/java.lang.invoke=ALL-UNNAMED",
            "--add-opens",
            "java.base/java.util=ALL-UNNAMED"
        )
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

val junit5Version = "5.7.2"
val junit5PlatformVersion = "1.7.2"

plugins.withType<KotlinBasePluginWrapper> {
    dependencies {
        add("testImplementation", "org.junit.jupiter:junit-jupiter-api:$junit5Version")
        add("testImplementation", "com.google.truth:truth:1.0")

        add("testRuntimeOnly", "org.junit.jupiter:junit-jupiter-engine:$junit5Version")
        add("testRuntimeOnly", "org.junit.platform:junit-platform-runner:$junit5PlatformVersion")
        add("testRuntimeOnly", "org.junit.platform:junit-platform-launcher:$junit5PlatformVersion")
    }
}

plugins.withType<JavaTestFixturesPlugin> {

    configure<KotlinJvmProjectExtension> {
        target.compilations.getByName("test")
            .associateWith(target.compilations.getByName("testFixtures"))
    }
}
