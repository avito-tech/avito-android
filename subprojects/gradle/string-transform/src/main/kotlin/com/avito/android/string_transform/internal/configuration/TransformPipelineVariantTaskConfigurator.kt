package com.avito.android.string_transform.internal.configuration

import Slf4jGradleLoggerFactory
import com.android.build.api.artifact.SingleArtifact
import com.android.build.api.variant.ApplicationVariant
import com.avito.android.signer.AbstractSignTask
import com.avito.android.signer.SignExtension
import com.avito.android.string_transform.TransformPipelineSpec
import com.avito.android.string_transform.internal.rules.DeclaredRule
import com.avito.android.string_transform.internal.rules.TransformRulesNormalizer
import com.avito.android.string_transform.internal.task.TransformVariantAabTask
import com.avito.android.string_transform.internal.task.TransformVariantApkTask
import com.avito.android.string_transform.internal.task.TransformVariantMappingTask
import com.avito.android.string_transform.internal.task.signing.SignTransformedApkTask
import com.avito.android.string_transform.internal.task.signing.SignTransformedBundleTask
import com.avito.android.string_transform.internal.task.signing.ValidateSigningIntegrationTask
import com.avito.android.tls.TlsConfigurationPlugin
import com.avito.capitalize
import com.avito.logger.create
import org.gradle.api.NamedDomainObjectProvider
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.ResolvableConfiguration
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.TaskProvider
import org.gradle.jvm.toolchain.JavaLauncher
import org.gradle.kotlin.dsl.getByType
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
        val signingEnabled = pipeline.integrations.signing.enabled.getOrElse(false)

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
        }

        if (signingEnabled) {
            configureSigningIntegration(
                apkTaskProvider = apkTaskProvider,
                aabTaskProvider = aabTaskProvider,
            )
        }
    }

    private fun configureSigningIntegration(
        apkTaskProvider: TaskProvider<TransformVariantApkTask>,
        aabTaskProvider: TaskProvider<TransformVariantAabTask>,
    ) {
        val validateSigningIntegrationTaskProvider = project.tasks.register<ValidateSigningIntegrationTask>(
            validateSigningIntegrationTaskName(pipeline.name, variant.name),
        ) {
            group = "string transform"
            description = "Validates signing integration for ${pipeline.name} pipeline on ${variant.name} variant"

            signServicePluginApplied.convention(false)
            failureMessage.set(
                "String-transform pipeline '${pipeline.name}' enables signing adapter without SignServicePlugin"
            )
        }

        rootTask.configure { task ->
            task.dependsOn(validateSigningIntegrationTaskProvider)
        }

        project.pluginManager.withPlugin("com.avito.android.sign-service") {
            validateSigningIntegrationTaskProvider.configure { task ->
                task.signServicePluginApplied.set(true)
            }

            val signerExtension = project.extensions.getByType<SignExtension>()
            val applicationId = variant.applicationId.get()
            // Keep token resolution aligned with sign-service: fail fast during task graph materialization
            // after signer extension configuration has completed, instead of deferring missing-token errors to execution.
            val validatedSigningTokens = SigningCapabilityValidator(
                projectPath = project.path,
                pipelineName = pipeline.name,
                variantName = variant.name,
            ).validate(
                applicationId = applicationId,
                apkToken = signerExtension.apkSignTokens.getting(applicationId).orNull,
                bundleToken = signerExtension.bundleSignTokens.getting(applicationId).orNull,
            )

            val signApkTaskProvider = project.tasks.register<SignTransformedApkTask>(
                signApkTaskName(pipeline.name, variant.name),
            ) {
                group = "string transform"
                description = "Signs transformed APK for ${pipeline.name} pipeline on ${variant.name} variant"

                unsignedApkFile.set(apkTaskProvider.flatMap { it.outputApkFile })
                signedArtifactDirectory.set(project.layout.buildDirectory.dir(signedApkDirectoryPath()))
                configureSigningTask(
                    task = this,
                    signerExtension = signerExtension,
                    token = validatedSigningTokens.apkToken,
                )
            }

            val signBundleTaskProvider = project.tasks.register<SignTransformedBundleTask>(
                signBundleTaskName(pipeline.name, variant.name),
            ) {
                group = "string transform"
                description = "Signs transformed AAB for ${pipeline.name} pipeline on ${variant.name} variant"

                unsignedBundleFile.set(aabTaskProvider.flatMap { it.outputAabFile })
                signedArtifactDirectory.set(project.layout.buildDirectory.dir(signedBundleDirectoryPath()))
                configureSigningTask(
                    task = this,
                    signerExtension = signerExtension,
                    token = validatedSigningTokens.bundleToken,
                )
            }

            rootTask.configure { task ->
                task.dependsOn(signApkTaskProvider)
                task.dependsOn(signBundleTaskProvider)
            }
        }
    }

    private fun configureSigningTask(
        task: AbstractSignTask,
        signerExtension: SignExtension,
        token: String,
    ) {
        task.serviceUrl.set(signerExtension.serviceUrl)
        task.tokenProperty.set(token)
        task.readWriteTimeoutSec.set(signerExtension.readWriteTimeoutSec.orElse(40L))
        task.useTls.set(signerExtension.useTls)

        if (signerExtension.useTls.getOrElse(true)) {
            val tlsCredentialsService = TlsConfigurationPlugin.provideCredentialsService(project)
            task.tlsCredentialsService.set(tlsCredentialsService)
            task.usesService(tlsCredentialsService)
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

    private fun signApkTaskName(pipelineName: String, variantName: String): String {
        return "signTransformStrings${pipelineName.capitalize()}${variantName.capitalize()}ApkViaService"
    }

    private fun signBundleTaskName(pipelineName: String, variantName: String): String {
        return "signTransformStrings${pipelineName.capitalize()}${variantName.capitalize()}BundleViaService"
    }

    private fun validateSigningIntegrationTaskName(pipelineName: String, variantName: String): String {
        return "validateTransformStrings${pipelineName.capitalize()}${variantName.capitalize()}SigningIntegration"
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

    private fun signedApkDirectoryPath(): String {
        return "outputs/signService/transformStrings/${pipeline.name}/${variant.name}/apk"
    }

    private fun signedBundleDirectoryPath(): String {
        return "outputs/signService/transformStrings/${pipeline.name}/${variant.name}/bundle"
    }
}
