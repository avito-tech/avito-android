package com.avito.android.plugin.artifactory

import com.avito.cd.BuildVariant
import com.avito.cd.CdBuildResult
import org.gradle.api.publish.maven.MavenArtifact
import java.net.URI

/**
 * @param artifactoryUri e.g. ${-PartifactoryUrl}/apps-release-local
 * @param groupId e.g. ${project.name}-android
 * @param artifactId e.g. ${project.name}
 * @param version e.g. "${defaultConfig.versionName}-${defaultConfig.versionCode}-${envArgs.buildNumber}"
 * @param buildVariantByClassifier e.g. "releaseApk": [BuildVariant.RELEASE];
 */
internal class MavenArtifactsToCdBuildResultArtifactMapper(
    artifactoryUri: URI,
    groupId: String,
    artifactId: String,
    version: String,
    private val buildVariantByClassifier: Map<String, BuildVariant>
) {

    private val publicationRootPath = URI.create("$artifactoryUri$groupId/$artifactId/$version")

    private val artifactsFileNamePrefix = "$artifactId-$version"

    private val MavenArtifact.fileUri
        get() = "$publicationRootPath/$fileName"

    private val MavenArtifact.fileName
        get() = "$artifactsFileNamePrefix-$classifier.$fileExtension"

    private val MavenArtifact.fileExtension
        get() = file.extension

    fun mapToCdBuildResultArtifacts(
        mavenArtifacts: Set<MavenArtifact>
    ): List<CdBuildResult.Artifact> {
        return mavenArtifacts.map { mavenArtifact ->
            val classifier = mavenArtifact.classifier!! // TODO содержит artifactId
            val isApk = classifier.contains("apk", true)
            val isBundle = classifier.contains("bundle", true)
            when {
                isApk -> mavenArtifact.toApk()
                isBundle -> mavenArtifact.toBundle()
                else -> createFile(mavenArtifact)
            }
        }
    }

    private fun createFile(
        mavenArtifact: MavenArtifact
    ): CdBuildResult.Artifact.FileArtifact {
        return CdBuildResult.Artifact.FileArtifact(
            type = mavenArtifact.classifier!!,
            name = mavenArtifact.fileName,
            uri = mavenArtifact.fileUri
        )
    }

    private fun MavenArtifact.toApk() = toBinary("apk")
    private fun MavenArtifact.toBundle() = toBinary("bundle")

    private fun MavenArtifact.toBinary(
        binaryType: String
    ): CdBuildResult.Artifact.AndroidBinary {
        return CdBuildResult.Artifact.AndroidBinary(
            type = binaryType,
            name = fileName,
            buildVariant = buildVariantByClassifier.getValue(classifier!!),
            uri = fileUri
        )
    }
}
