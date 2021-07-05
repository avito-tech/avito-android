import gradle.kotlin.dsl.accessors._aefff6f539276b188c30d8843d1e61e9.testImplementation
import gradle.kotlin.dsl.accessors._aefff6f539276b188c30d8843d1e61e9.testRuntimeOnly
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

// workaround for https://github.com/gradle/gradle/issues/15383
if (project.name != "gradle-kotlin-dsl-accessors") {

    val libs = the<org.gradle.accessors.dm.LibrariesForLibs>()

    plugins.withType<KotlinBasePluginWrapper> {
        dependencies {
            testImplementation(libs.junitJupiterApi)
            testImplementation(libs.truth)

            testRuntimeOnly(libs.junitJupiterEngine)
            testRuntimeOnly(libs.junitPlatformRunner)
            testRuntimeOnly(libs.junitPlatformLauncher)
        }
    }
}

plugins.withType<JavaTestFixturesPlugin> {

    configure<KotlinJvmProjectExtension> {
        target.compilations.getByName("test")
            .associateWith(target.compilations.getByName("testFixtures"))
    }
}
