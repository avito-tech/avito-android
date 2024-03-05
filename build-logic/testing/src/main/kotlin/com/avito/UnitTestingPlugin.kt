package com.avito

import com.avito.android.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.ExternalModuleDependency
import org.gradle.api.artifacts.MinimalExternalModuleDependency
import org.gradle.api.plugins.JavaTestFixturesPlugin
import org.gradle.api.tasks.testing.Test
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinBasePluginWrapper

class UnitTestingPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        with(project) {
            tasks.withType(Test::class.java).configureEach {
                it.apply {
                    useJUnitPlatform()

                    val halfOfAvailableProcessors =
                        (Runtime.getRuntime().availableProcessors() / 2).takeIf { it > 0 } ?: 1
                    maxParallelForks = halfOfAvailableProcessors

                    failFast = false

                    minHeapSize = "64m"
                    maxHeapSize = "128m"

                    /**
                     * fix for multiple `WARNING: Illegal reflective access`
                     */
                    jvmArgs(
                        listOf(
                            // ParallelGC has less footprint than default GC (G1).
                            // Can't return committed memory to OS, but Gradle worker is shot-lived and this is ok.
                            // See also ci/gradle.properties
                            "-XX:+UseParallelGC",
                            "-XX:+UseGCOverheadLimit",
                            "-XX:GCTimeLimit=10",

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
                }
            }

            plugins.withType(KotlinBasePluginWrapper::class.java) {
                with(dependencies) {
                    // If we use Junit 4 annotations in tests, they won't run, and it may confuse developers.
                    // Our tests run only on Junit 5 jupiter, so we explicitly remove this transitive dependency.
                    addProvider<MinimalExternalModuleDependency, ExternalModuleDependency>(
                        "testImplementation",
                        libs.truth
                    ) {
                        it.exclude(mapOf("group" to "junit"))
                    }
                    add("testImplementation", libs.junitJupiterApi)

                    add("testRuntimeOnly", libs.junitJupiterEngine)
                    add("testRuntimeOnly", libs.junitPlatformRunner)
                    add("testRuntimeOnly", libs.junitPlatformLauncher)
                }
            }

            plugins.withType(JavaTestFixturesPlugin::class.java) {

                extensions.configure(KotlinJvmProjectExtension::class.java) {
                    it.target.compilations.getByName("test")
                        .associateWith(it.target.compilations.getByName("testFixtures"))
                }
            }
        }
    }
}
