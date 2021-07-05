package com.avito.android.plugin.artifactory

import com.avito.cd.BuildVariant
import com.avito.cd.CdBuildResult
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.net.URI

internal class MavenArtifactsToCdBuildResultArtifactMapperTest {

    @Test
    fun `maven artifacts to cd build result mapper - success apk mapping`(@TempDir dir: File) {
        val classifier = "releaseApk"
        val buildVariant = BuildVariant.RELEASE
        val artifactId = "artifactId"
        val version = "version"
        val artifactFile = File(dir, "test.apk")
        val groupId = "groupId"

        val mapper = MavenArtifactsToCdBuildResultArtifactMapper(
            artifactoryUri = URI.create(""),
            groupId = groupId,
            artifactId = artifactId,
            version = version,
            buildVariantByClassifier = mapOf(classifier to buildVariant)
        )
        val result = mapper.mapToCdBuildResultArtifacts(
            setOf(
                StubMavenArtifact(
                    file = artifactFile,
                    classifier = classifier
                )
            )
        )
        assertThat(result)
            .containsExactly(
                CdBuildResult.Artifact.AndroidBinary(
                    type = "apk",
                    name = "$artifactId-$version-$classifier.${artifactFile.extension}",
                    uri = "$groupId/$artifactId/$version/$artifactId-$version-$classifier.${artifactFile.extension}",
                    buildVariant = buildVariant
                )
            )
    }

    @Test
    fun `maven artifacts to cd build result mapper - success bundle mapping`(@TempDir dir: File) {
        val classifier = "releaseBundle"
        val buildVariant = BuildVariant.RELEASE
        val artifactId = "artifactId"
        val version = "version"
        val artifactFile = File(dir, "test.aab")
        val groupId = "groupId"

        val mapper = MavenArtifactsToCdBuildResultArtifactMapper(
            artifactoryUri = URI.create(""),
            groupId = groupId,
            artifactId = artifactId,
            version = version,
            buildVariantByClassifier = mapOf(classifier to buildVariant)
        )
        val result = mapper.mapToCdBuildResultArtifacts(
            setOf(
                StubMavenArtifact(
                    file = artifactFile,
                    classifier = classifier
                )
            )
        )
        assertThat(result)
            .containsExactly(
                CdBuildResult.Artifact.AndroidBinary(
                    type = "bundle",
                    name = "$artifactId-$version-$classifier.${artifactFile.extension}",
                    uri = "$groupId/$artifactId/$version/$artifactId-$version-$classifier.${artifactFile.extension}",
                    buildVariant = buildVariant
                )
            )
    }

    @Test
    fun `maven artifacts to cd build result mapper - success file mapping`(@TempDir dir: File) {
        val classifier = "xxx"
        val artifactId = "artifactId"
        val version = "version"
        val artifactFile = File(dir, "test.aab")
        val groupId = "groupId"

        val mapper = MavenArtifactsToCdBuildResultArtifactMapper(
            artifactoryUri = URI.create(""),
            groupId = groupId,
            artifactId = artifactId,
            version = version,
            buildVariantByClassifier = emptyMap()
        )
        val result = mapper.mapToCdBuildResultArtifacts(
            setOf(
                StubMavenArtifact(
                    file = artifactFile,
                    classifier = classifier
                )
            )
        )
        assertThat(result)
            .containsExactly(
                CdBuildResult.Artifact.FileArtifact(
                    type = classifier,
                    name = "$artifactId-$version-$classifier.${artifactFile.extension}",
                    uri = "$groupId/$artifactId/$version/$artifactId-$version-$classifier.${artifactFile.extension}"
                )
            )
    }
}
