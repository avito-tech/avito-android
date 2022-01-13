package com.avito.android.plugin.artifactory

import com.avito.android.Problem
import com.avito.android.asRuntimeException
import com.avito.cd.buildOutput
import com.avito.kotlin.dsl.getMandatoryStringProperty
import com.avito.kotlin.dsl.withType
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin
import org.gradle.api.publish.maven.tasks.PublishToMavenRepository
import org.gradle.kotlin.dsl.findByType
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.withType
import java.net.URI

public class ArtifactoryAppBackupPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        project.extensions.create(
            ArtifactoryBackupExtension::class.java,
            artifactoryBackupExtensionName,
            DefaultArtifactoryAppBackupExtension::class.java,
            project.objects
        )

        project.plugins.withType<MavenPublishPlugin> {
            configureMavenPublishPlugin(project)

            val backupTask = project.tasks.register(artifactoryAppBackupTaskName)

            project.extensions.configure<DefaultArtifactoryAppBackupExtension>(artifactoryBackupExtensionName) {
                it.backups.all { backup ->
                    project.createBackupPublication(backup)
                    val publishTask = project.findMavenPublishTask(backup)
                    if (publishTask != null) {
                        publishTask.addSetArtifactsBuildOutputAction()
                        backupTask.configure {
                            it.dependsOn(publishTask)
                        }
                    } else {
                        throw Problem(
                            shortDescription = "Can't apply `com.avito.android.artifactory-app-backup` plugin",
                            context = "ArtifactoryAppBackupPlugin modifying publication task",
                            because = "This could happen if `maven-publish` plugin has breaking API changes, " +
                                "or because of an error in `artifactory-app-backup` plugin itself",
                            possibleSolutions = listOf(
                                "Check `maven-publish` incompatible API changes " +
                                    "and apply needed changes in ArtifactoryAppBackupPlugin",
                                "Check recent changes in `artifactory-app-backup`"
                            ),
                            documentedAt = "https://docs.gradle.org/current/userguide/publishing_maven.html"
                        ).asRuntimeException()
                    }
                }
            }
        }

        project.afterEvaluate {
            if (!project.plugins.hasPlugin(MavenPublishPlugin::class.java)) {
                throw Problem(
                    shortDescription = "Can't apply `com.avito.android.artifactory-app-backup` plugin",
                    context = "ArtifactoryAppBackupPlugin applying",
                    because = "artifactory-app-backup has precondition: maven-publish plugin should be applied",
                    possibleSolutions = listOf("Add `maven-publish` plugin to ${project.path}")
                ).asRuntimeException()
            }
        }
    }

    private fun Project.findMavenPublishTask(backup: Backup): PublishToMavenRepository? {
        return tasks.withType<PublishToMavenRepository>()
            .matching {
                it.repository.name == artifactoryRepositoryName && it.publication.name == backup.name
            }
            .firstOrNull()
    }

    private fun Project.createBackupPublication(backup: Backup) {
        val publishing = extensions.findByType<PublishingExtension>()

        if (publishing != null) {
            val publication = publishing.publications.create(backup.name, MavenPublication::class.java) { publication ->
                publication.groupId = backup.name
                publication.artifactId = backup.type
                publication.version = backup.version
            }

            backup.artifacts.forEach { (id, path) ->
                publication.artifact(path) { it.classifier = id }
            }
        }
    }

    private fun configureMavenPublishPlugin(project: Project) {
        val publishing = project.extensions.getByType<PublishingExtension>()
        val artifactoryUrl = project.getMandatoryStringProperty("artifactoryUrl").removeSuffix("/")
        val backupUrl = URI.create("$artifactoryUrl/apps-release-local/")
        publishing.repositories.maven { repo ->
            repo.name = artifactoryRepositoryName
            repo.url = backupUrl
            repo.credentials.username = project.artifactoryUser
            repo.credentials.password = project.artifactoryPassword
            repo.isAllowInsecureProtocol = true
        }
    }

    private fun PublishToMavenRepository.addSetArtifactsBuildOutputAction() {
        doFirst {
            val action = MavenArtifactsToCdBuildResultArtifactMapper(
                artifactoryUri = this.repository.url,
                groupId = this.publication.groupId,
                artifactId = this.publication.artifactId,
                version = this.publication.version,
                buildVariantByClassifier = project.artifactoryAndroidArtifactsBuildVariants.get()
            )
            val buildOutput = project.buildOutput.get()
            buildOutput.artifacts = action.mapToCdBuildResultArtifacts(publication.artifacts)
        }
    }
}
