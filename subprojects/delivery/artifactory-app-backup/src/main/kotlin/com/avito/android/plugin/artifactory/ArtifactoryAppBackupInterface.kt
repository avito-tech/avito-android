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

public const val artifactoryAppBackupTaskName: String = "appBackup"

public val Project.appBackupExtension: ArtifactoryBackupExtension
    get() = extensions.getByType(ArtifactoryBackupExtension::class.java)

public fun TaskContainer.artifactoryAppBackupTask(): TaskProvider<Task> =
    named(artifactoryAppBackupTaskName)

public val Project.artifactoryAndroidArtifactsBuildVariants: MapProperty<String, BuildVariant>
    by ProjectProperty.lazy { project ->
        project.objects.mapProperty(String::class.java, BuildVariant::class.java)
    }

public const val artifactoryUserParameterName: String = "artifactory_deployer"

public const val artifactoryPasswordParameterName: String = "artifactory_deployer_password"

public val Project.artifactoryUser: String
    get() = getMandatoryStringProperty(artifactoryUserParameterName)

public val Project.artifactoryPassword: String
    get() = getMandatoryStringProperty(artifactoryPasswordParameterName)
