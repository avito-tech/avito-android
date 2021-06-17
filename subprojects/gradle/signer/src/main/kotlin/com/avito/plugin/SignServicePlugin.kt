package com.avito.plugin

import com.android.build.api.artifact.ArtifactType
import com.android.build.gradle.api.ApplicationVariant
import com.avito.android.androidCommonExtension
import com.avito.android.bundleTaskProvider
import com.avito.android.withAndroidApp
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.create

@Suppress("UnstableApiUsage")
class SignServicePlugin : Plugin<Project> {

    override fun apply(target: Project) {

        val signExtension = target.extensions.create<SignExtension>("signService")

        target.withAndroidApp { appExtension ->

            target.androidCommonExtension.onVariants {
                val variant = this

                registerTask<SignApkTask>(
                    tasks = target.tasks,
                    variant = this,
                    taskName = signApkTaskName(variant),
                    signTokensMap = signExtension.apkSignTokens,
                    extension = signExtension,
                )

                registerTask<SignBundleTask>(
                    tasks = target.tasks,
                    variant = this,
                    taskName = signBundleTaskName(variant),
                    signTokensMap = signExtension.bundleSignTokens,
                    extension = signExtension,
                )
            }

            target.androidCommonExtension.onVariantProperties {

                artifacts.use(target.tasks.signedApkTaskProvider(this))
                    .wiredWithDirectories(
                        taskInput = SignApkTask::unsignedDirProperty,
                        taskOutput = SignApkTask::signedDirProperty
                    )
                    .toTransform(ArtifactType.APK)

                artifacts.use(target.tasks.signedBundleTaskProvider(this))
                    .wiredWithFiles(
                        taskInput = SignBundleTask::unsignedFileProperty,
                        taskOutput = SignBundleTask::signedFileProperty
                    )
                    .toTransform(ArtifactType.BUNDLE)
            }

            appExtension.applicationVariants.all { variant: ApplicationVariant ->

                val buildTypeName = variant.buildType.name
                val apkToken: String? = signExtension.apkSignTokens[buildTypeName]
                val bundleToken: String? = signExtension.bundleSignTokens[buildTypeName]

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
