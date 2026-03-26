package com.avito.android.string_transform

import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import com.avito.android.string_transform.internal.configuration.TransformPipelineVariantTaskConfigurator
import com.avito.android.string_transform.internal.configuration.TransformPipelinesValidator
import org.gradle.api.NamedDomainObjectProvider
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.ResolvableConfiguration
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.TaskProvider
import org.gradle.jvm.toolchain.JavaLauncher
import org.gradle.jvm.toolchain.JavaToolchainService
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.getByType

@Suppress("UnstableApiUsage")
public class StringTransformPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        val extension = project.extensions.create<TransformStringsExtension>(
            "transformStrings",
            project.path,
            project.objects,
        )

        project.pluginManager.withPlugin("com.android.application") {
            val apktoolConfiguration = registerApktoolConfiguration(project)
            val defaultJavaLauncher = defaultJavaLauncher(project)
            val rootTask = project.tasks.register("transformStrings") {
                it.group = "string transform"
                it.description = "Runs all registered string-transform pipelines for the module"
            }
            configureAndroidApplication(
                project = project,
                extension = extension,
                rootTask = rootTask,
                apktoolConfiguration = apktoolConfiguration,
                defaultJavaLauncher = defaultJavaLauncher,
            )
        }
    }

    private fun configureAndroidApplication(
        project: Project,
        extension: TransformStringsExtension,
        rootTask: TaskProvider<Task>,
        apktoolConfiguration: NamedDomainObjectProvider<ResolvableConfiguration>,
        defaultJavaLauncher: Provider<JavaLauncher>,
    ) {
        val androidComponents = project.extensions.getByType<ApplicationAndroidComponentsExtension>()
        val pipelines by lazy { validatedPipelines(project, extension) }

        androidComponents.onVariants { variant ->
            pipelines.forEach { pipeline ->
                if (pipeline.variant.orNull == variant.name) {
                    TransformPipelineVariantTaskConfigurator(
                        project = project,
                        rootTask = rootTask,
                        pipeline = pipeline,
                        variant = variant,
                        apktoolConfiguration = apktoolConfiguration,
                        defaultJavaLauncher = defaultJavaLauncher,
                    ).configure()
                }
            }
        }
    }

    private fun validatedPipelines(
        project: Project,
        extension: TransformStringsExtension,
    ): List<TransformPipelineSpec> {
        return extension.pipelines.toList().onEach { pipeline ->
            TransformPipelinesValidator.validate(project.path, pipeline)
        }
    }

    private fun registerApktoolConfiguration(
        project: Project,
    ): NamedDomainObjectProvider<ResolvableConfiguration> {
        val dependencyScope = project.configurations.dependencyScope(APKTOOL_DEPENDENCY_SCOPE_NAME) { configuration ->
            configuration.isTransitive = false
        }

        project.dependencies.add(APKTOOL_DEPENDENCY_SCOPE_NAME, APKTOOL_DEPENDENCY_NOTATION)

        return project.configurations.resolvable(APKTOOL_CONFIGURATION_NAME) { configuration ->
            configuration.isTransitive = false
            configuration.extendsFrom(dependencyScope.get())
        }
    }

    private fun defaultJavaLauncher(project: Project): Provider<JavaLauncher> {
        return project.extensions.getByType<JavaToolchainService>().launcherFor { }
    }

    private companion object {
        private const val APKTOOL_DEPENDENCY_SCOPE_NAME = "stringTransformApktoolDependencies"
        private const val APKTOOL_CONFIGURATION_NAME = "stringTransformApktool"
        private const val APKTOOL_DEPENDENCY_NOTATION = "org.apktool:apktool-cli:2.9.3:all@jar"
    }
}
