package com.avito.android.plugin.artifactory

import groovy.lang.Closure
import org.gradle.api.model.ObjectFactory
import javax.inject.Inject

data class Backup(
    val name: String,
    val type: String,
    val version: String,
    val artifacts: Set<Artifact>
) {
    data class Artifact(
        val id: String,
        val path: String
    )
}

class ArtifactBuilder {
    var id: String = ""
    var path: String = ""
}

interface BackupBuilder {
    var name: String
    var type: String
    var version: String
    fun artifact(action: Closure<in ArtifactBuilder>)
}

private class DefaultBackupBuilder(
    objects: ObjectFactory
) : BackupBuilder {

    val artifacts = objects.domainObjectSet(Backup.Artifact::class.java)

    init {
        artifacts.all { artifact ->
            require(artifact.id.isNotEmpty()) {
                "id can't be empty"
            }
            require(artifact.path.isNotEmpty()) {
                "path can't be empty"
            }
        }
    }

    override var name: String = ""
    override var type: String = ""
    override var version: String = ""

    override fun artifact(action: Closure<in ArtifactBuilder>) {
        val artifactBuilder = ArtifactBuilder()
        action.delegate = artifactBuilder
        action.resolveStrategy = Closure.DELEGATE_ONLY
        action.call()
        artifacts.add(
            Backup.Artifact(
                id = artifactBuilder.id,
                path = artifactBuilder.path
            )
        )
    }

}

interface ArtifactoryBackupExtension {
    fun backup(action: Closure<in BackupBuilder>)
}

internal abstract class DefaultArtifactoryAppBackupExtension @Inject constructor(
    private val objects: ObjectFactory
) : ArtifactoryBackupExtension {

    val backups = objects.domainObjectSet(Backup::class.java)

    init {
        backups.all { backup ->
            require(backup.name.isNotEmpty()) {
                "name can't be empty"
            }

            require(backup.type.isNotEmpty()) {
                "name can't be empty"
            }

            require(backup.version.isNotEmpty()) {
                "version can't be empty"
            }
        }
    }

    override fun backup(action: Closure<in BackupBuilder>) {
        val backupBuilder = DefaultBackupBuilder(objects)
        action.delegate = backupBuilder
        action.resolveStrategy = Closure.DELEGATE_ONLY
        action.call()
        backups.add(
            Backup(
                name = backupBuilder.name,
                type = backupBuilder.type,
                version = backupBuilder.version,
                artifacts = backupBuilder.artifacts
            )
        )
    }
}
