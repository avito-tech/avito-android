package com.avito

import com.avito.android.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.internal.classpath.ModuleRegistry
import org.gradle.api.plugins.JavaTestFixturesPlugin
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.testing.Test
import org.gradle.configurationcache.extensions.serviceOf
import org.gradle.plugin.devel.GradlePluginDevelopmentExtension
import org.gradle.plugins.ide.idea.model.IdeaModel
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.jetbrains.kotlin.gradle.plugin.getKotlinPluginVersion
import org.jetbrains.kotlin.gradle.utils.addExtendsFromRelation

class GradleTestingPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        with(project) {
            plugins.apply("kotlin")
            plugins.apply("idea")
            val sourceSets = extensions.getByType(SourceSetContainer::class.java)
            val gradleTest = sourceSets.create("gradleTest") {
                addExtendsFromRelation("gradleTestImplementation", "testImplementation")
                addExtendsFromRelation("gradleTestRuntimeOnly", "testRuntimeOnly")
            }
            val gradleTestJarTask = tasks.register(gradleTest.jarTaskName, Jar::class.java) {
                it.archiveClassifier.set("gradle-tests")
                it.from(gradleTest.output)
            }
            val gradleTestTask = registerGradleTestTask(gradleTest, gradleTestJarTask)

            // workaround for https://github.com/gradle/gradle/issues/16774
            dependencies.add(
                "gradleTestRuntimeOnly",
                files(
                    serviceOf<ModuleRegistry>().getModule("gradle-tooling-api-builders").classpath.asFiles.first()
                )
            )
            plugins.withId("java-gradle-plugin") {
                extensions.getByType(GradlePluginDevelopmentExtension::class.java).testSourceSets(gradleTest)
            }
            // make idea to treat gradleTest as test sources
            extensions.configure(IdeaModel::class.java) {
                with(it) {
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
            }
            tasks.named("check").configure {
                it.dependsOn(gradleTestTask)
            }

            extensions.configure(KotlinJvmProjectExtension::class.java) {
                with(it) {
                    target.compilations.getByName("gradleTest")
                        .associateWith(target.compilations.getByName("main"))

                    plugins.withType(JavaTestFixturesPlugin::class.java) {
                        target.compilations.getByName("gradleTest")
                            .associateWith(target.compilations.getByName("testFixtures"))
                    }
                }
            }
        }
    }

    private fun Project.registerGradleTestTask(
        gradleTest: SourceSet,
        gradleTestJarTask: TaskProvider<Jar>,
    ): TaskProvider<Test> {
        val testTimeoutSeconds = 600
        val artifactoryUrl: Provider<String> = providers.gradleProperty("artifactoryUrl")
        return tasks.register("gradleTest", Test::class.java) {
            it.apply {
                description = "Runs gradle test kit tests"
                group = "verification"

                testClassesDirs = gradleTest.output.classesDirs
                classpath =
                    configurations.getByName(gradleTest.runtimeClasspathConfigurationName) + files(gradleTestJarTask)

                /**
                 * The only reason to have more forks is faster test suite because of parallel execution
                 * Additional forks requires more resources and should be faster
                 * Tests on powerful machine with a lot of resources to spare proves that actually 1 is the fastest value,
                 * at least for our project
                 *
                 * `make benchmark_gradle_test` used for tests (see gradle/performance.scenarios)
                 * forks median value:
                 *   1    4min 11sec
                 *   2    4min 39sec
                 */
                maxParallelForks = 1

                /**
                 * usually there is a small amount of test classes per module, and tests are not so memory hungry
                 */
                setForkEvery(null)

                jvmArgs(
                    listOf(
                        "-XX:+UseGCOverheadLimit",
                        "-XX:GCTimeLimit=10"
                    )
                )

                minHeapSize = "64m"
                maxHeapSize = "128m"

                systemProperty("rootDir", "${project.rootDir}")
                systemProperty("buildDir", "$buildDir")
                systemProperty("kotlinVersion", project.getKotlinPluginVersion())

                systemProperty("artifactoryUrl", artifactoryUrl.getOrElse(""))
                systemProperty("isTest", true)

                systemProperty("junit.jupiter.execution.timeout.default", testTimeoutSeconds)
                systemProperty("compileSdkVersion", libs.versions.compileSdk.get().toInt())
                systemProperty("buildToolsVersion", libs.versions.buildTools.get())
                systemProperty("targetSdk", libs.versions.targetSdk.get().toInt())
                systemProperty("minSdk", libs.versions.minSdk.get().toInt())
            }
        }
    }
}
