package com.avito.android.signer

import com.android.build.api.artifact.SingleArtifact
import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import com.android.build.api.variant.ApplicationVariant
import com.avito.android.Problem
import com.avito.android.asRuntimeException
import com.avito.android.signer.internal.TaskConfigurator
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.register

public class SignServicePlugin : Plugin<Project> {

    override fun apply(target: Project) {

        val extension = target.extensions.create<SignExtension>("signer")

        val taskConfigurator = TaskConfigurator(extension)

        val outputsLocation = "outputs/signService"

        target.extensions.getByType<ApplicationAndroidComponentsExtension>().run {

            onVariants { variant: ApplicationVariant ->

                val applicationId = variant.applicationId.get()

                val apkToken = extension.apkSignTokens.getting(applicationId).orNull

                if (!apkToken.isNullOrBlank()) {

                    checkSigningConfig(variant)

                    target.tasks.register<SignApkTask>(signApkTaskName(variant.name)) {
                        description = "Signs ${variant.name} apk with in-house service"
                        apkDirectory.set(variant.artifacts.get(SingleArtifact.APK))
                        signedArtifactDirectory.set(
                            project.layout.buildDirectory.dir("$outputsLocation/apk/${variant.name}")
                        )
                        taskConfigurator.configure(
                            task = this,
                            token = apkToken
                        )
                    }
                }

                val bundleToken = extension.bundleSignTokens.getting(applicationId).orNull

                if (!bundleToken.isNullOrBlank()) {

                    checkSigningConfig(variant)

                    target.tasks.register<SignBundleTask>(signBundleTaskName(variant.name)) {
                        description = "Signs ${variant.name} bundle with in-house service"
                        bundleFile.set(variant.artifacts.get(SingleArtifact.BUNDLE))
                        signedArtifactDirectory.set(
                            project.layout.buildDirectory.dir("$outputsLocation/bundle/${variant.name}")
                        )
                        taskConfigurator.configure(
                            task = this,
                            token = bundleToken
                        )
                    }
                }
            }
        }
    }

    private fun checkSigningConfig(variant: ApplicationVariant) {
        require(variant.signingConfig == null) {
            throw Problem(
                shortDescription = "Can't create sign task for variant '${variant.name}'",
                context = "Configuration of '$PLUGIN_ID' plugin",
                because = "AGP signingConfig configured. Custom signing via service is incompatible with AGP signing",
                possibleSolutions = listOf(
                    "Set `signingConfig = null` for variant: '${variant.name}'",
                    "Create new special variant for custom signing, " +
                        "probably you want it to be inherited from '${variant.name}'"
                ),
                documentedAt = DOCUMENTATION_URL
            ).asRuntimeException()
        }
    }
}
