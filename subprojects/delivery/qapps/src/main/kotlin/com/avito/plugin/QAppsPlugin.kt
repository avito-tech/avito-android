package com.avito.plugin

import com.android.build.api.artifact.SingleArtifact
import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import com.avito.android.signer.SignServicePlugin
import com.avito.android.signer.signedApkDir
import com.avito.android.withAndroidApp
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.Directory
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.register

public class QAppsPlugin : Plugin<Project> {

    override fun apply(project: Project) {

        val extension = project.extensions.create<QAppsExtension>("qapps")

        project.withAndroidApp { appExtension ->
            val finalApkDirs = mutableMapOf<String, Provider<Directory>>()
            project.extensions.getByType<ApplicationAndroidComponentsExtension>().onVariants { variant ->
                finalApkDirs[variant.name] = variant.artifacts.get(SingleArtifact.APK)
            }

            appExtension.applicationVariants.all { variant ->

                project.tasks.register<QAppsUploadTask>(
                    qappsUploadUnsignedTaskName(variant.name),
                ) {
                    description = "Upload unsigned ${variant.name} to qapps"
                    configure(extension, variant)
                    apkDirectory.convention(
                        requireNotNull(finalApkDirs[variant.name]) {
                            "No AGP variant '${variant.name}' to resolve the final APK from"
                        }
                    )
                }

                if (project.plugins.hasPlugin(SignServicePlugin::class.java)) {

                    val signedApkDir = project.tasks.signedApkDir(variant.name)

                    if (signedApkDir.isPresent) {
                        project.tasks.register<QAppsUploadTask>(
                            qappsUploadSignedTaskName(variant.name),
                        ) {
                            description = "Upload signed ${variant.name} to qapps"
                            configure(extension, variant)
                            apkDirectory.convention(signedApkDir)
                        }
                    }
                }
            }
        }
    }

    // todo use new AGP API
    private fun QAppsUploadTask.configure(
        extension: QAppsExtension,
        @Suppress("DEPRECATION") variant: com.android.build.gradle.api.ApplicationVariant
    ) {
        group = "ci"

        variantName.set(variant.name)
        versionName.set(variant.versionName)
        versionCode.set(variant.versionCode.toString())
        packageName.set(variant.applicationId)
        host.set(extension.serviceUrl)
        comment.set(extension.comment)
        branch.set(extension.branchName)
        releaseBuildVariants.set(extension.releaseBuildVariants)
    }
}
