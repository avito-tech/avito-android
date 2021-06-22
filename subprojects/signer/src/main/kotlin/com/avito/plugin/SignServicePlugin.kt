package com.avito.plugin

import com.android.build.api.artifact.ArtifactType
import com.android.build.api.variant.Variant
import com.avito.android.Problem
import com.avito.android.androidCommonExtension
import com.avito.android.asRuntimeException
import com.avito.android.withAndroidApp
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.TaskContainer
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.register

@Suppress("UnstableApiUsage")
public class SignServicePlugin : Plugin<Project> {

    override fun apply(target: Project) {

        val extension = target.extensions.create<SignExtension>("signService")

        // AGP 4.2 variant
        // target.extensions.getByType<ApplicationAndroidComponentsExtension>().run {

        target.withAndroidApp { appExtension ->

            appExtension.applicationVariants.all { variant: com.android.build.gradle.api.ApplicationVariant ->

                val buildTypeName = variant.buildType.name
                val apkToken: String? = extension.apkSignTokens[buildTypeName]
                val bundleToken: String? = extension.bundleSignTokens[buildTypeName]

                variant.outputsAreSigned = apkToken.hasContent() || bundleToken.hasContent()
            }

            // AGP 4.2 variant
            // onVariants { variant: ApplicationVariant ->
            target.androidCommonExtension.onVariants {

                val variant = this

                val urlResolver = UrlResolver(extension)

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
            }

            target.androidCommonExtension.onVariantProperties {

                val buildTypeName = buildType

                val apkToken: String? = extension.apkSignTokens[buildTypeName]

                if (apkToken.hasContent()) {
                    artifacts.use(target.tasks.signedApkTaskProvider(this))
                        .wiredWithDirectories(
                            taskInput = SignApkTask::unsignedDirProperty,
                            taskOutput = SignApkTask::signedDirProperty
                        )
                        .toTransform(ArtifactType.APK)
                }

                val bundleToken: String? = extension.bundleSignTokens[buildTypeName]

                if (bundleToken.hasContent()) {
                    artifacts.use(target.tasks.signedBundleTaskProvider(this))
                        .wiredWithFiles(
                            taskInput = SignBundleTask::unsignedFileProperty,
                            taskOutput = SignBundleTask::signedFileProperty
                        )
                        .toTransform(ArtifactType.BUNDLE)
                }
            }
        }
    }

    @Suppress("UnstableApiUsage")
    private inline fun <reified T : SignArtifactTask> registerTask(
        tasks: TaskContainer,
        variant: Variant<*>, // AGP 4.2 remove <*>
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
