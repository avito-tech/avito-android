package com.avito.plugin

import com.android.build.api.artifact.SingleArtifact
import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import com.android.build.api.variant.ApplicationVariant
import com.android.build.api.variant.Variant
import com.avito.android.Problem
import com.avito.android.asRuntimeException
import com.avito.android.withAndroidApp
import com.avito.plugin.internal.LegacyTokensResolver
import com.avito.plugin.internal.UrlResolver
import com.avito.plugin.internal.hasContent
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.TaskContainer
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.register

public class SignServicePlugin : Plugin<Project> {

    override fun apply(target: Project) {

        val extension = target.extensions.create<SignExtension>("signService")

        // todo remove after migration to new tasks
        target.withAndroidApp { appExtension ->

            @Suppress("DEPRECATION")
            appExtension.applicationVariants.all { variant: com.android.build.gradle.api.ApplicationVariant ->

                val buildTypeName = variant.buildType.name
                val apkToken: String? = extension.apkSignTokens[buildTypeName]?.orNull
                val bundleToken: String? = extension.bundleSignTokens[buildTypeName]?.orNull

                variant.outputsAreSigned = apkToken.hasContent() || bundleToken.hasContent()
            }
        }

        target.extensions.getByType<ApplicationAndroidComponentsExtension>().run {

            onVariants { variant: ApplicationVariant ->

                val urlResolver = UrlResolver(extension)

                val buildTypeName = variant.buildType

                val apkToken = extension.apkSignTokens[buildTypeName]?.orNull

                registerTask<LegacySignApkTask>(
                    tasks = target.tasks,
                    variant = variant,
                    taskName = legacySignApkTaskName(variant.name),
                    extension = extension,
                    signingResolver = LegacyTokensResolver(
                        extension = extension,
                        variant = variant,
                        signTokensMap = extension.apkSignTokens
                    ),
                    urlResolver = urlResolver
                )

                if (apkToken.hasContent()) {
                    variant.artifacts.use(target.tasks.legacySignedApkTaskProvider(variant))
                        .wiredWithDirectories(
                            taskInput = LegacySignApkTask::unsignedDirProperty,
                            taskOutput = LegacySignApkTask::signedDirProperty
                        )
                        .toTransform(SingleArtifact.APK)
                }

                val bundleToken = extension.bundleSignTokens[buildTypeName]?.orNull

                registerTask<LegacySignBundleTask>(
                    tasks = target.tasks,
                    variant = variant,
                    taskName = legacySignBundleTaskName(variant.name),
                    extension = extension,
                    signingResolver = LegacyTokensResolver(
                        extension = extension,
                        variant = variant,
                        signTokensMap = extension.bundleSignTokens
                    ),
                    urlResolver = urlResolver
                )

                if (bundleToken.hasContent()) {
                    variant.artifacts.use(target.tasks.legacySignedBundleTaskProvider(variant))
                        .wiredWithFiles(
                            taskInput = LegacySignBundleTask::unsignedFileProperty,
                            taskOutput = LegacySignBundleTask::signedFileProperty
                        )
                        .toTransform(SingleArtifact.BUNDLE)
                }
            }
        }
    }

    private inline fun <reified T : LegacySignArtifactTask> registerTask(
        tasks: TaskContainer,
        variant: Variant,
        taskName: String,
        extension: SignExtension,
        signingResolver: LegacyTokensResolver,
        urlResolver: UrlResolver,
    ): TaskProvider<T> {
        return tasks.register<T>(taskName) {
            group = CI_TASK_GROUP
            description = "Sign ${variant.name} with in-house service"

            serviceUrl.set(urlResolver.resolveServiceUrl { throwable ->
                failOnConfiguration(
                    taskName = taskName,
                    throwable = throwable
                )
            })
            tokenProperty.set(signingResolver.resolveToken { throwable ->
                failOnConfiguration(
                    taskName = taskName,
                    throwable = throwable
                )
            })
            readWriteTimeoutSec.set(extension.readWriteTimeoutSec.convention(DEFAULT_TIMEOUT_SEC))

            onlyIf { signingResolver.isCustomSigningEnabled }
        }
    }

    private fun failOnConfiguration(taskName: String, throwable: Throwable): Nothing {
        throw Problem(
            shortDescription = throwable.localizedMessage,
            context = "Configuring '$taskName' task",
            because = "Plugin is not properly configured",
            possibleSolutions = emptyList(),
            documentedAt = "https://avito-tech.github.io/avito-android/projects/internal/Signer/",
            throwable = throwable,
        ).asRuntimeException()
    }
}

private const val DEFAULT_TIMEOUT_SEC = 40L

private const val CI_TASK_GROUP = "ci"
