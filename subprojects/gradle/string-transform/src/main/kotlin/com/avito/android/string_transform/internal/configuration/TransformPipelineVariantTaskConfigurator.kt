package com.avito.android.string_transform.internal.configuration

import Slf4jGradleLoggerFactory
import com.android.build.api.artifact.SingleArtifact
import com.android.build.api.variant.ApplicationVariant
import com.avito.android.string_transform.TransformPipelineSpec
import com.avito.android.string_transform.internal.rules.DeclaredRule
import com.avito.android.string_transform.internal.rules.TransformRulesNormalizer
import com.avito.android.string_transform.internal.task.TransformVariantApkTask
import com.avito.capitalize
import com.avito.logger.create
import org.gradle.api.NamedDomainObjectProvider
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.ResolvableConfiguration
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.TaskProvider
import org.gradle.jvm.toolchain.JavaLauncher
import org.gradle.kotlin.dsl.register

internal class TransformPipelineVariantTaskConfigurator(
    private val project: Project,
    private val rootTask: TaskProvider<Task>,
    private val pipeline: TransformPipelineSpec,
    private val variant: ApplicationVariant,
    private val apktoolConfiguration: NamedDomainObjectProvider<ResolvableConfiguration>,
    private val defaultJavaLauncher: Provider<JavaLauncher>,
) {

    private val logger = Slf4jGradleLoggerFactory.create<TransformPipelineVariantTaskConfigurator>()

    fun configure() {
        val normalizedRules = TransformRulesNormalizer.normalize(pipeline)
        val declaredRules = pipeline.rules.declaredRules.get()

        normalizedRules.warnings.forEach { warning ->
            logger.warn("Pipeline '${pipeline.name}' for variant '${variant.name}': $warning")
        }

        val taskProvider = project.tasks.register<TransformVariantApkTask>(
            variantTaskName(pipeline.name, variant.name),
        ) {
            group = "string transform"
            description = "Transforms APK strings for ${pipeline.name} pipeline on ${variant.name} variant"

            modulePath.set(project.path)
            pipelineName.set(pipeline.name)
            variantName.set(variant.name)
            totalRuleCount.set(normalizedRules.rules.size)
            exactRuleCount.set(declaredRules.count { rule -> rule is DeclaredRule.Exact })
            caseExpandedRuleCount.set(declaredRules.count { rule -> rule is DeclaredRule.CaseExpanded })
            configurationWarnings.set(normalizedRules.warnings)
            rules.set(normalizedRules.rules)
            apktoolClasspath.from(apktoolConfiguration)
            javaLauncher.convention(defaultJavaLauncher)
            apkDirectory.set(variant.artifacts.get(SingleArtifact.APK))
            localStateDirectory.set(project.layout.buildDirectory.dir(localStatePath()))
            outputApkFile.set(project.layout.buildDirectory.file(outputApkPath()))
            reportFile.set(project.layout.buildDirectory.file(reportPath()))
        }

        rootTask.configure { task ->
            task.dependsOn(taskProvider)
        }
    }

    private fun variantTaskName(pipelineName: String, variantName: String): String {
        return "transformStrings${pipelineName.capitalize()}${variantName.capitalize()}"
    }

    private fun localStatePath(): String {
        return "tmp/transformStrings/${pipeline.name}/${variant.name}/local-state"
    }

    private fun outputApkPath(): String {
        return "outputs/transformStrings/${pipeline.name}/${variant.name}/apk/transformed-unsigned.apk"
    }

    private fun reportPath(): String {
        return "outputs/transformStrings/${pipeline.name}/${variant.name}/report/transform-report.json"
    }
}
