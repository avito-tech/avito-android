package com.avito.deeplink_generator

import com.android.build.api.artifact.SingleArtifact
import com.android.build.api.variant.LibraryAndroidComponentsExtension
import com.android.build.api.variant.LibraryVariant
import com.avito.android.isAndroidLibrary
import com.avito.deeplink_generator.internal.merge.MergeDeeplinkManifestTask
import com.avito.kotlin.dsl.getBooleanProperty
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.create

public class DeeplinkGeneratorPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        require(target.isAndroidLibrary()) {
            "DeeplinkGeneratorPlugin must be applied only to android library modules."
        }

        val deeplinkGeneratorExtension =
            target.extensions.create<DeeplinkGeneratorExtension>("deeplinkGenerator")

        val androidComponents = target.extensions.getByType(LibraryAndroidComponentsExtension::class.java)
        androidComponents.onVariants { variant ->
            configureMergeDeeplinkManifestTask(target, variant, deeplinkGeneratorExtension)
            configureValidatePublicDeepLinksTask(target, variant, deeplinkGeneratorExtension)
        }
    }

    private fun configureValidatePublicDeepLinksTask(
        project: Project,
        variant: LibraryVariant,
        ext: DeeplinkGeneratorExtension
    ) {
        project.tasks.register(
            ValidatePublicDeeplinksTask.taskName(variant.name),
            ValidatePublicDeeplinksTask::class.java
        ) { task ->
            task.defaultScheme.set(ext.defaultScheme)
            task.publicDeeplinksFromBuildScript.set(ext.publicDeeplinks)
            task.codeFixHint.set(ext.validationCodeFixHint)
            task.validationResult.set(project.layout.buildDirectory.file("deeplinks/public-deeplinks-validation.out"))
        }
    }

    private fun configureMergeDeeplinkManifestTask(
        project: Project,
        variant: LibraryVariant,
        ext: DeeplinkGeneratorExtension
    ) {
        if (ext.publicDeeplinks.get().isEmpty()) return
        val deeplinksFilterEnabled =
            project.getBooleanProperty("avito.deeplinks.filter.enabled", false)
        if (deeplinksFilterEnabled) {
            val mergeManifestTask =
                project.tasks.register(
                    MergePublicDeeplinkManifestTask.taskName(variant.name),
                    MergePublicDeeplinkManifestTask::class.java
                ) { task ->
                    task.activityIntentFilterClass.set(ext.activityIntentFilterClass)
                }

            variant.artifacts.use(mergeManifestTask)
                .wiredWithFiles(
                    MergePublicDeeplinkManifestTask::inputManifest,
                    MergePublicDeeplinkManifestTask::outputManifest
                )
                .toTransform(SingleArtifact.MERGED_MANIFEST)
        } else {
            val mergeManifestTask =
                project.tasks.register(
                    MergeDeeplinkManifestTask.taskName(variant.name),
                    MergeDeeplinkManifestTask::class.java
                ) { task ->
                    task.publicLinks.set(ext.publicDeeplinks)
                    task.activityIntentFilterClass.set(ext.activityIntentFilterClass)
                }

            variant.artifacts.use(mergeManifestTask)
                .wiredWithFiles(
                    MergeDeeplinkManifestTask::inputManifest,
                    MergeDeeplinkManifestTask::outputManifest
                )
                .toTransform(SingleArtifact.MERGED_MANIFEST)
        }
    }
}
