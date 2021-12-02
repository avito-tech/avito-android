package com.avito.plugin

import com.avito.android.signer.SignServicePlugin
import com.avito.android.signer.signedApkDir
import com.avito.android.withAndroidApp
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.register

public class QAppsPlugin : Plugin<Project> {

    override fun apply(project: Project) {

        val extension = project.extensions.create<QAppsExtension>("qapps")

        project.withAndroidApp { appExtension ->
            appExtension.applicationVariants.all { variant ->

                /**
                 * apk is set in UploadToQapps.kt build step
                 * todo remove after migration to new tasks
                 */
                project.tasks.register<QAppsUploadTask>(legacyQappsUploadTaskName(variant.name)) {
                    description = "Upload ${variant.name} to qapps, legacy variant, should not be used"
                    configure(extension, variant)
                }

                project.tasks.register<QAppsUploadTask>(qappsUploadUnsignedTaskName(variant.name)) {
                    description = "Upload unsigned ${variant.name} to qapps"
                    configure(extension, variant)

                    val packageTaskProvider = variant.packageApplicationProvider

                    val apkProvider = packageTaskProvider.flatMap { it.outputDirectory }

                    apkDirectory.set(apkProvider)

                    // todo remove, somehow implicit dependency not working
                    dependsOn(packageTaskProvider)
                }

                if (project.plugins.hasPlugin(SignServicePlugin::class.java)) {

                    val signedApkDir = project.tasks.signedApkDir(variant.name)

                    if (signedApkDir.isPresent) {
                        project.tasks.register<QAppsUploadTask>(qappsUploadSignedTaskName(variant.name)) {
                            description = "Upload signed ${variant.name} to qapps"
                            configure(extension, variant)
                            apkDirectory.set(signedApkDir)
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

        versionName.set(variant.versionName)
        versionCode.set(variant.versionCode.toString())
        packageName.set(variant.applicationId)

        host.set(extension.serviceUrl)
        comment.set(extension.comment)
        branch.set(extension.branchName)
    }
}
