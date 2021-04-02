package com.avito.android.plugin.artifactory

import com.android.build.gradle.internal.tasks.factory.dependsOn
import com.avito.cd.buildOutput
import com.avito.kotlin.dsl.getMandatoryStringProperty
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin
import org.gradle.api.publish.maven.tasks.PublishToMavenRepository
import org.gradle.kotlin.dsl.getByType
import java.net.URI

class ArtifactoryAppBackupPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        project.extensions.create(
            ArtifactoryBackupExtension::class.java,
            artifactoryBackupExtensionName,
            DefaultArtifactoryAppBackupExtension::class.java,
            project.objects
        )

        project.extensions.configure<DefaultArtifactoryAppBackupExtension>(artifactoryBackupExtensionName) {
            it.backups.all { backup ->
                require(project.plugins.hasPlugin(MavenPublishPlugin::class.java)) {
                    "'maven-publish' plugin should be applied to use artifactory app backup plugin"
                }
                configureMavenPublishPlugin(project)
                project.createBackupPublication(backup)
                val publishTask = project.findMavenPublishTask(backup)
                publishTask.addSetArtifactsBuildOutputAction()
                @Suppress("DEPRECATION")
                project.tasks.artifactoryAppBackupTask().dependsOn(publishTask)
            }
        }
    }

    private fun Project.findMavenPublishTask(
        backup: Backup
    ): PublishToMavenRepository {
        return tasks.withType(PublishToMavenRepository::class.java).matching {
            it.repository.name == artifactoryRepositoryName &&
                it.publication.name == backup.name
        }.first()
    }

    private fun Project.createBackupPublication(backup: Backup) {
        val publishing = extensions.getByType<PublishingExtension>()
        val publication =
            publishing.publications.create(backup.name, MavenPublication::class.java) { publication ->
                publication.groupId = backup.name
                publication.artifactId = backup.type
                publication.version = backup.version
            }

        backup.artifacts.forEach { (id, path) ->
            publication.artifact(path) { it.classifier = id }
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
        project.tasks.register(artifactoryAppBackupTaskName)
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
