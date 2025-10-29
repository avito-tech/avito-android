package com.avito.deeplink_generator

import com.android.build.api.artifact.SingleArtifact
import com.android.build.api.variant.AndroidComponentsExtension
import com.avito.android.isAndroidApp
import com.avito.deeplink_generator.internal.filter.DeeplinkManifestFilterTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.create

public class DeeplinkManifestFilterPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        require(project.isAndroidApp()) {
            "DeeplinkManifestFilterPlugin must be applied only to android app modules."
        }

        val androidComponents = project.extensions.getByType(AndroidComponentsExtension::class.java)
        val deeplinkManifestFilterExtension =
            project.extensions.create<DeeplinkManifestFilterExtension>("deeplinkManifestFilter")

        androidComponents.onVariants { variant ->
            val variantToForbiddenSchemes = deeplinkManifestFilterExtension.variantToForbiddenSchemes.get()

            val filterManifestTask = project.tasks.register(
                DeeplinkManifestFilterTask.taskName(variant.name),
                DeeplinkManifestFilterTask::class.java
            ) {
                it.forbiddenSchemes.set(variantToForbiddenSchemes[variant.name])
            }

            variant.artifacts.use(filterManifestTask)
                .wiredWithFiles(
                    DeeplinkManifestFilterTask::inputManifest,
                    DeeplinkManifestFilterTask::outputManifest
                )
                .toTransform(SingleArtifact.MERGED_MANIFEST)
        }
    }
}
