package com.avito.cd

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class CdBuildConfigValidatorTest {

    @Test
    fun `success - valid contract last supported version`() {
        val config = CdBuildConfig(
            schemaVersion = 2,
            project = NupokatiProject.AVITO,
            releaseVersion = "248.0",
            outputDescriptor = CdBuildConfig.OutputDescriptor(
                path = "http://foo.bar",
                skipUpload = true
            ),
            deployments = listOf(
                CdBuildConfig.Deployment.GooglePlay(
                    AndroidArtifactType.BUNDLE,
                    BuildVariant.RELEASE,
                    CdBuildConfig.Deployment.Track.ALPHA
                ),
                CdBuildConfig.Deployment.Qapps(
                    isRelease = true
                )
            )
        )

        CdBuildConfigValidator(config).validate()
    }

    @Test
    fun `fail - unsupported deployment type`() {
        val config = CdBuildConfig(
            schemaVersion = 2,
            project = NupokatiProject.AVITO,
            releaseVersion = "248.0",
            outputDescriptor = CdBuildConfig.OutputDescriptor(
                path = "http://foo.bar",
                skipUpload = true
            ),
            deployments = listOf(
                CdBuildConfig.Deployment.Unknown(
                    type = "UNKNOWN_TYPE"
                )
            )
        )
        val error = assertThrows<RuntimeException> {
            CdBuildConfigValidator(config).validate()
        }
        assertThat(error).hasMessageThat().contains("Unknown deployment type")
        assertThat(error).hasMessageThat().contains("UNKNOWN_TYPE")
    }

    @Test
    fun `fails - duplicated google play deployments with the same build variant`() {
        val config = CdBuildConfig(
            schemaVersion = 1,
            project = NupokatiProject.AVITO,
            releaseVersion = "248.0",
            outputDescriptor = CdBuildConfig.OutputDescriptor(
                path = "http://foo.bar",
                skipUpload = true
            ),
            deployments = listOf(
                CdBuildConfig.Deployment.GooglePlay(
                    AndroidArtifactType.BUNDLE,
                    BuildVariant.RELEASE,
                    CdBuildConfig.Deployment.Track.ALPHA
                ),
                CdBuildConfig.Deployment.GooglePlay(
                    AndroidArtifactType.APK,
                    BuildVariant.RELEASE,
                    CdBuildConfig.Deployment.Track.ALPHA
                )
            )
        )
        val error = assertThrows<RuntimeException> {
            CdBuildConfigValidator(config).validate()
        }
        assertThat(error).hasMessageThat().contains("Must be one deploy per variant")
    }

    @Test
    fun `fails - qapps deployment in the first version of contract`() {
        val config = CdBuildConfig(
            schemaVersion = 1,
            project = NupokatiProject.AVITO,
            releaseVersion = "248.0",
            outputDescriptor = CdBuildConfig.OutputDescriptor(
                path = "http://foo.bar",
                skipUpload = true
            ),
            deployments = listOf(
                CdBuildConfig.Deployment.Qapps(
                    isRelease = true
                )
            )
        )
        val error = assertThrows<RuntimeException> {
            CdBuildConfigValidator(config).validate()
        }
        assertThat(error).hasMessageThat()
            .contains("Qapps deployments is supported only in the 2'nd version of contract")
    }
}
