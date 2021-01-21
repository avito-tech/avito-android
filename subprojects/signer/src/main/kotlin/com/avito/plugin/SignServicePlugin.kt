package com.avito.plugin

import com.android.build.api.artifact.ArtifactType
import com.android.build.api.extension.ApplicationAndroidComponentsExtension
import com.android.build.api.variant.Variant
import com.android.build.gradle.api.ApplicationVariant
import com.avito.android.Problem
import com.avito.android.asRuntimeException
import com.avito.android.bundleTaskProvider
import com.avito.android.withAndroidApp
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.TaskContainer
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.register

@Suppress("UnstableApiUsage")
public class SignServicePlugin : Plugin<Project> {

    override fun apply(target: Project) {

        val extension = target.extensions.create<SignExtension>("signService")

        target.extensions.getByType<ApplicationAndroidComponentsExtension>().run {

            onVariants { variant ->

                val urlResolver = UrlResolver(extension)

                registerTask<SignApkTask>(
                    tasks = target.tasks,
                    variant = variant,
                    taskName = signApkTaskName(variant),
                    extension = extension,
                    signingResolver = SigningResolver(
                        project = target,
                        extension = extension,
                        variant = variant,
                        signTokensMap = extension.apkSignTokens
                    ),
                    urlResolver = urlResolver
                )

                registerTask<SignBundleTask>(
                    tasks = target.tasks,
                    variant = variant,
                    taskName = signBundleTaskName(variant),
                    extension = extension,
                    signingResolver = SigningResolver(
                        project = target,
                        extension = extension,
                        variant = variant,
                        signTokensMap = extension.bundleSignTokens
                    ),
                    urlResolver = urlResolver
                )

                variant.artifacts.use(target.tasks.signedApkTaskProvider(variant))
                    .wiredWithDirectories(
                        taskInput = SignApkTask::unsignedDirProperty,
                        taskOutput = SignApkTask::signedDirProperty
                    )
                    .toTransform(ArtifactType.APK)

                variant.artifacts.use(target.tasks.signedBundleTaskProvider(variant))
                    .wiredWithFiles(
                        taskInput = SignBundleTask::unsignedFileProperty,
                        taskOutput = SignBundleTask::signedFileProperty
                    )
                    .toTransform(ArtifactType.BUNDLE)
            }

            target.withAndroidApp { appExtension ->
                appExtension.applicationVariants.all { variant: ApplicationVariant ->

                    val buildTypeName = variant.buildType.name
                    val apkToken: String? = extension.apkSignTokens[buildTypeName]
                    val bundleToken: String? = extension.bundleSignTokens[buildTypeName]

                    variant.outputsAreSigned = apkToken.hasContent() || bundleToken.hasContent()

                    target.tasks.signedApkTaskProvider(variant.name).configure { signApkTask ->
                        signApkTask.dependsOn(variant.packageApplicationProvider)
                    }

                    target.tasks.signedBundleTaskProvider(variant.name).configure { signBundleTask ->
                        signBundleTask.dependsOn(target.tasks.bundleTaskProvider(variant))
                    }
                }
            }
        }
    }

    @Suppress("UnstableApiUsage")
    private inline fun <reified T : SignArtifactTask> registerTask(
        tasks: TaskContainer,
        variant: Variant,
        taskName: String,
        extension: SignExtension,
        signingResolver: SigningResolver,
        urlResolver: UrlResolver,
    ) {
        tasks.register<T>(taskName) {
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
