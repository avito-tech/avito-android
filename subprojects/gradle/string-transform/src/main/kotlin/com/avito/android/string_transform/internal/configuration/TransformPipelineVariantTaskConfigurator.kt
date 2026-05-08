package com.avito.android.string_transform.internal.configuration

import Slf4jGradleLoggerFactory
import com.android.build.api.artifact.SingleArtifact
import com.android.build.api.variant.ApplicationVariant
import com.avito.android.string_transform.TransformPipelineSpec
import com.avito.android.string_transform.internal.rules.DeclaredRule
import com.avito.android.string_transform.internal.rules.TransformRulesNormalizer
import com.avito.android.string_transform.task.TransformVariantAabTask
import com.avito.android.string_transform.task.TransformVariantApkTask
import com.avito.android.string_transform.task.TransformVariantMappingTask
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

@Suppress("UnstableApiUsage")
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

        val apkTaskProvider = project.tasks.register<TransformVariantApkTask>(
            apkTaskName(pipeline.name, variant.name),
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
            localStateDirectory.set(project.layout.buildDirectory.dir(apkLocalStatePath()))
            outputApkFile.set(project.layout.buildDirectory.file(outputApkPath()))
            reportFile.set(project.layout.buildDirectory.file(apkReportPath()))
        }

        val androidTestApkTaskProvider = variant.androidTest?.let { androidTestComponent ->
            project.tasks.register<TransformVariantApkTask>(
                androidTestApkTaskName(pipeline.name, variant.name),
            ) {
                group = "string transform"
                description =
                    "Transforms androidTest APK strings for ${pipeline.name} pipeline on ${variant.name} variant"

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
                apkDirectory.set(androidTestComponent.artifacts.get(SingleArtifact.APK))
                localStateDirectory.set(project.layout.buildDirectory.dir(androidTestApkLocalStatePath()))
                outputApkFile.set(project.layout.buildDirectory.file(outputAndroidTestApkPath()))
                reportFile.set(project.layout.buildDirectory.file(androidTestApkReportPath()))
            }
        }

        val aabTaskProvider = project.tasks.register<TransformVariantAabTask>(
            aabTaskName(pipeline.name, variant.name),
        ) {
            group = "string transform"
            description = "Transforms AAB strings for ${pipeline.name} pipeline on ${variant.name} variant"

            modulePath.set(project.path)
            pipelineName.set(pipeline.name)
            variantName.set(variant.name)
            totalRuleCount.set(normalizedRules.rules.size)
            exactRuleCount.set(declaredRules.count { rule -> rule is DeclaredRule.Exact })
            caseExpandedRuleCount.set(declaredRules.count { rule -> rule is DeclaredRule.CaseExpanded })
            configurationWarnings.set(normalizedRules.warnings)
            rules.set(normalizedRules.rules)
            inputAabFile.set(variant.artifacts.get(SingleArtifact.BUNDLE))
            localStateDirectory.set(project.layout.buildDirectory.dir(aabLocalStatePath()))
            outputAabFile.set(project.layout.buildDirectory.file(outputAabPath()))
            reportFile.set(project.layout.buildDirectory.file(aabReportPath()))
        }

        val mappingTaskProvider = project.tasks.register<TransformVariantMappingTask>(
            mappingTaskName(pipeline.name, variant.name),
        ) {
            group = "string transform"
            description = "Transforms mapping strings for ${pipeline.name} pipeline on ${variant.name} variant"

            modulePath.set(project.path)
            pipelineName.set(pipeline.name)
            variantName.set(variant.name)
            totalRuleCount.set(normalizedRules.rules.size)
            exactRuleCount.set(declaredRules.count { rule -> rule is DeclaredRule.Exact })
            caseExpandedRuleCount.set(declaredRules.count { rule -> rule is DeclaredRule.CaseExpanded })
            configurationWarnings.set(normalizedRules.warnings)
            rules.set(normalizedRules.rules)
            inputMappingFile.set(variant.artifacts.get(SingleArtifact.OBFUSCATION_MAPPING_FILE))
            localStateDirectory.set(project.layout.buildDirectory.dir(mappingLocalStatePath()))
            outputMappingFile.set(project.layout.buildDirectory.file(outputMappingPath()))
            reportFile.set(project.layout.buildDirectory.file(mappingReportPath()))
            onlyIf("original mapping artifact exists") {
                inputMappingFile.orNull?.asFile?.exists() == true
            }
        }

        rootTask.configure { task ->
            task.dependsOn(apkTaskProvider)
            task.dependsOn(aabTaskProvider)
            task.dependsOn(mappingTaskProvider)
            if (androidTestApkTaskProvider != null) {
                task.dependsOn(androidTestApkTaskProvider)
            }
        }
    }

    private fun apkTaskName(pipelineName: String, variantName: String): String {
        return "transformStrings${pipelineName.capitalize()}${variantName.capitalize()}"
    }

    private fun aabTaskName(pipelineName: String, variantName: String): String {
        return "transformStrings${pipelineName.capitalize()}${variantName.capitalize()}Bundle"
    }

    private fun mappingTaskName(pipelineName: String, variantName: String): String {
        return "transformStrings${pipelineName.capitalize()}${variantName.capitalize()}Mapping"
    }

    private fun androidTestApkTaskName(pipelineName: String, variantName: String): String {
        return "transformStrings${pipelineName.capitalize()}${variantName.capitalize()}AndroidTest"
    }

    private fun apkLocalStatePath(): String {
        return "tmp/transformStrings/${pipeline.name}/${variant.name}/local-state"
    }

    private fun outputApkPath(): String {
        return "outputs/transformStrings/${pipeline.name}/${variant.name}/apk/transformed-unsigned.apk"
    }

    private fun apkReportPath(): String {
        return "outputs/transformStrings/${pipeline.name}/${variant.name}/report/transform-report.json"
    }

    private fun aabReportPath(): String {
        return "outputs/transformStrings/${pipeline.name}/${variant.name}/aab/report/transform-report.json"
    }

    private fun aabLocalStatePath(): String {
        return "tmp/transformStrings/${pipeline.name}/${variant.name}/aab-local-state"
    }

    private fun outputAabPath(): String {
        return "outputs/transformStrings/${pipeline.name}/${variant.name}/aab/transformed-unsigned.aab"
    }

    private fun mappingLocalStatePath(): String {
        return "tmp/transformStrings/${pipeline.name}/${variant.name}/mapping-local-state"
    }

    private fun outputMappingPath(): String {
        return "outputs/transformStrings/${pipeline.name}/${variant.name}/mapping/transformed-mapping.txt"
    }

    private fun mappingReportPath(): String {
        return "outputs/transformStrings/${pipeline.name}/${variant.name}/mapping/report/transform-report.json"
    }

    private fun androidTestApkLocalStatePath(): String {
        return "tmp/transformStrings/${pipeline.name}/${variant.name}/local-state-androidtest"
    }

    private fun outputAndroidTestApkPath(): String {
        return "outputs/transformStrings/${pipeline.name}/${variant.name}/apk-androidtest/transformed-unsigned.apk"
    }

    private fun androidTestApkReportPath(): String {
        return "outputs/transformStrings/${pipeline.name}/${variant.name}/report-androidtest/transform-report.json"
    }
}
