@file:Suppress("UnstableApiUsage")

package com.avito.android.plugin.artifactory

import com.avito.cd.BuildVariant
import com.avito.kotlin.dsl.ProjectProperty
import com.avito.kotlin.dsl.getMandatoryStringProperty
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.provider.MapProperty
import org.gradle.api.tasks.TaskContainer
import org.gradle.api.tasks.TaskProvider

internal const val artifactoryBackupExtensionName = "artifactoryBackups"

internal const val artifactoryRepositoryName = "appsBackup"

const val artifactoryAppBackupTaskName = "appBackup"

val Project.appBackupExtension: ArtifactoryBackupExtension
    get() = extensions.getByType(ArtifactoryBackupExtension::class.java)

fun TaskContainer.artifactoryAppBackupTask(): TaskProvider<Task> =
    named(artifactoryAppBackupTaskName)

val Project.artifactoryAndroidArtifactsBuildVariants: MapProperty<String, BuildVariant>
    by ProjectProperty.lazy { project ->
        project.objects.mapProperty(String::class.java, BuildVariant::class.java)
    }

const val artifactoryUserParameterName = "artifactory_deployer"

const val artifactoryPasswordParameterName = "artifactory_deployer_password"

val Project.artifactoryUser
    get() = getMandatoryStringProperty(artifactoryUserParameterName)

val Project.artifactoryPassword
    get() = getMandatoryStringProperty(artifactoryPasswordParameterName)
