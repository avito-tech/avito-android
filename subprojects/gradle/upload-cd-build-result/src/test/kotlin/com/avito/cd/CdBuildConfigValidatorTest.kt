package com.avito.cd

import com.avito.utils.logging.CILogger
import com.avito.utils.logging.FakeCILogger
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class CdBuildConfigValidatorTest {

    @Test
    fun `duplicated google play deployments with the same build variant`() {
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
            CdBuildConfigValidator(config, CILogger.allToStdout).validate()
        }
        assertThat(error).hasMessageThat().contains("Must be one deploy per variant")
    }

    @Test
    fun `skip unknown deployment`() {
        val config = CdBuildConfig(
            schemaVersion = 1,
            project = NupokatiProject.AVITO,
            releaseVersion = "248.0",
            outputDescriptor = CdBuildConfig.OutputDescriptor(
                path = "http://foo.bar",
                skipUpload = true
            ),
            deployments = listOf(
                CdBuildConfig.Deployment.Unknown(
                    type = "UnknownDeploymentType"
                )
            )
        )
        val logger = FakeCILogger()
        CdBuildConfigValidator(config, logger).validate()

        assertThat(logger.infoHandler.messages).contains("Ignore unknown CD config deployment: UnknownDeploymentType")
    }
}
