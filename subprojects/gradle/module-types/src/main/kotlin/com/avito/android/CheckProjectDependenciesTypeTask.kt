package com.avito.android

import com.avito.impact.configuration.ImplementationConfiguration
import com.avito.impact.configuration.internalModule
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

abstract class CheckProjectDependenciesTypeTask : DefaultTask() {

    @TaskAction
    fun checkProjectDependenciesType() {
        val moduleType = project.extensions.moduleType?.type

        val testDependenciesInImplementation = implementationsByType(ModuleType.TEST_LIB)

        val androidTestDependenciesInImplementation = implementationsByType(ModuleType.ANDROID_TEST_LIB)

        val componentTestDependenciesInImplementation = implementationsByType(ModuleType.COMPONENT_TEST)

        val androidTestDependenciesInTest = testImplementationsByType(ModuleType.ANDROID_TEST_LIB)

        val componentTestDependenciesInTest = testImplementationsByType(ModuleType.COMPONENT_TEST)

        val componentTestDependenciesInAndroidTest = androidTestImplementationsByType(ModuleType.COMPONENT_TEST)

        val wrongDependenciesInImplementationConfiguration = when (moduleType) {
            ModuleType.TEST_LIB ->
                androidTestDependenciesInImplementation + componentTestDependenciesInImplementation

            ModuleType.ANDROID_TEST_LIB ->
                componentTestDependenciesInImplementation

            ModuleType.IMPLEMENTATION ->
                testDependenciesInImplementation +
                    androidTestDependenciesInImplementation +
                    componentTestDependenciesInImplementation

            ModuleType.COMPONENT_TEST -> emptyList()

            null ->
                testDependenciesInImplementation +
                    androidTestDependenciesInImplementation +
                    componentTestDependenciesInImplementation
        }

        @Suppress("UnnecessaryVariable")
        val wrongDependenciesInTestConfiguration = androidTestDependenciesInTest +
            componentTestDependenciesInTest

        @Suppress("UnnecessaryVariable")
        val wrongDependenciesInAndroidTestConfiguration = componentTestDependenciesInAndroidTest

        if (wrongDependenciesInImplementationConfiguration.isNotEmpty() ||
            wrongDependenciesInTestConfiguration.isNotEmpty() ||
            wrongDependenciesInAndroidTestConfiguration.isNotEmpty()
        ) {
            throw IllegalStateException(
                buildWrongDependenciesTypeString(
                    wrongDependenciesInImplementationConfiguration,
                    wrongDependenciesInTestConfiguration,
                    wrongDependenciesInAndroidTestConfiguration
                )
            )
        }
    }

    private fun buildWrongDependenciesTypeString(
        wrongDependenciesInImplementation: List<ImplementationConfiguration>,
        wrongDependenciesInTest: List<ImplementationConfiguration>,
        wrongDependenciesInAndroidTest: List<ImplementationConfiguration>
    ): String {
        val message = StringBuilder()
        message.appendLine("${project.path} has wrong dependencies:")
        if (wrongDependenciesInImplementation.isNotEmpty()) {
            message.appendLine(
                "'implementation' configuration contains the following non-implementation dependencies: " +
                        wrongDependenciesInImplementation.joinToString { it.module.project.path }
            )
        }
        if (wrongDependenciesInTest.isNotEmpty()) {
            message.appendLine(
                "'testImplementation' configuration contains the following non-test dependencies: " +
                        wrongDependenciesInTest.joinToString { it.module.project.path }
            )
        }
        if (wrongDependenciesInAndroidTest.isNotEmpty()) {
            message.appendLine(
                "'androidTestImplementation' configuration contains the following non-android-test dependencies: " +
                        wrongDependenciesInAndroidTest.joinToString { it.module.project.path }
            )
        }
        return message.toString()
    }

    private fun implementationsByType(type: ModuleType) =
        project.internalModule.implementationConfiguration.dependencies.filter {
            it.module.project.extensions.moduleType?.type?.equals(type) ?: false
        }

    private fun testImplementationsByType(type: ModuleType) =
        project.internalModule.testConfiguration.dependencies.filter {
            it.module.project.extensions.moduleType?.type?.equals(type) ?: false
        }

    private fun androidTestImplementationsByType(type: ModuleType) =
        project.internalModule.androidTestConfiguration.dependencies.filter {
            it.module.project.extensions.moduleType?.type?.equals(type) ?: false
        }
}
