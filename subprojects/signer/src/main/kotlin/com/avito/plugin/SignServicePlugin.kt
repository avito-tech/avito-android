package com.avito.plugin

import com.android.build.api.artifact.SingleArtifact
import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import com.android.build.api.variant.ApplicationVariant
import com.android.build.api.variant.Variant
import com.avito.android.Problem
import com.avito.android.asRuntimeException
import com.avito.android.withAndroidApp
import com.avito.logger.GradleLoggerFactory
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

        target.withAndroidApp { appExtension ->

            // todo rewrite signer to use new api
            @Suppress("DEPRECATION")
            appExtension.applicationVariants.all { variant: com.android.build.gradle.api.ApplicationVariant ->

                val buildTypeName = variant.buildType.name
                val apkToken: String? = extension.apkSignTokens[buildTypeName]
                val bundleToken: String? = extension.bundleSignTokens[buildTypeName]

                variant.outputsAreSigned = apkToken.hasContent() || bundleToken.hasContent()
            }
        }

        target.extensions.getByType<ApplicationAndroidComponentsExtension>().run {

            onVariants { variant: ApplicationVariant ->

                val urlResolver = UrlResolver(extension)

                val buildTypeName = variant.buildType

                registerTask<SignApkTask>(
                    tasks = target.tasks,
                    variant = variant,
                    taskName = signApkTaskName(variant.name),
                    extension = extension,
                    signingResolver = SigningResolver(
                        extension = extension,
                        variant = variant,
                        signTokensMap = extension.apkSignTokens
                    ),
                    urlResolver = urlResolver
                )

                val apkToken: String? = extension.apkSignTokens[buildTypeName]

                if (apkToken.hasContent()) {
                    variant.artifacts.use(target.tasks.signedApkTaskProvider(variant))
                        .wiredWithDirectories(
                            taskInput = SignApkTask::unsignedDirProperty,
                            taskOutput = SignApkTask::signedDirProperty
                        )
                        .toTransform(SingleArtifact.APK)
                }

                registerTask<SignBundleTask>(
                    tasks = target.tasks,
                    variant = variant,
                    taskName = signBundleTaskName(variant.name),
                    extension = extension,
                    signingResolver = SigningResolver(
                        extension = extension,
                        variant = variant,
                        signTokensMap = extension.bundleSignTokens
                    ),
                    urlResolver = urlResolver
                )

                val bundleToken: String? = extension.bundleSignTokens[buildTypeName]

                if (bundleToken.hasContent()) {
                    variant.artifacts.use(target.tasks.signedBundleTaskProvider(variant))
                        .wiredWithFiles(
                            taskInput = SignBundleTask::unsignedFileProperty,
                            taskOutput = SignBundleTask::signedFileProperty
                        )
                        .toTransform(SingleArtifact.BUNDLE)
                }
            }
        }
    }

    private inline fun <reified T : SignArtifactTask> registerTask(
        tasks: TaskContainer,
        variant: Variant,
        taskName: String,
        extension: SignExtension,
        signingResolver: SigningResolver,
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

            loggerFactory.set(
                GradleLoggerFactory.fromTask(
                    project = project,
                    taskName = this.name,
                    plugin = this@SignServicePlugin
                )
            )

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
