package com.avito

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.component.AdhocComponentWithVariants
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension

class TestFixturesPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        with(project) {
            plugins.apply("java-test-fixtures")
            extensions.configure(KotlinJvmProjectExtension::class.java) {
                /**
                 * Workaround to access internal classes from testFixtures
                 * till https://youtrack.jetbrains.com/issue/KT-34901 resolved
                 */
                it.target.compilations.getByName("testFixtures")
                    .associateWith(it.target.compilations.getByName("main"))
            }
            (components.getByName("java") as AdhocComponentWithVariants).skipTestFixturesPublication(project)
        }
    }

    /**
     * from: https://docs.gradle.org/current/userguide/java_testing.html#publishing_test_fixtures
     */
    private fun AdhocComponentWithVariants.skipTestFixturesPublication(project: Project) {
        withVariantsFromConfiguration(project.configurations.getByName("testFixturesApiElements")) { it.skip() }
        withVariantsFromConfiguration(project.configurations.getByName("testFixturesRuntimeElements")) { it.skip() }
    }
}
