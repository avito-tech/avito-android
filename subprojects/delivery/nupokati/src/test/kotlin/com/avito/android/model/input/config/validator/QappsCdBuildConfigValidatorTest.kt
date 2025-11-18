package com.avito.android.model.input.config.validator

import com.avito.android.model.input.AndroidArtifactType
import com.avito.android.model.input.OutputDescriptor
import com.avito.android.model.input.config.CdBuildConfigV2
import com.avito.android.model.input.config.CdBuildConfigV2.Deployment
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class QappsCdBuildConfigValidatorTest {

    private val validator = QappsCdBuildConfigValidator()

    @Test
    fun `validate - passes with no Qapps deployments`() {
        val config = createConfig(
            schemaVersion = 2,
            deployments = listOf(
                Deployment.GooglePlay(
                    artifactType = AndroidArtifactType.BUNDLE,
                    buildVariant = "release",
                    track = Deployment.Track.beta
                ),
                Deployment.RuStore(
                    artifactType = AndroidArtifactType.APK
                )
            )
        )

        validator.validate(config)
    }

    @Test
    fun `validate - passes with one Qapps deployment and schema version 2`() {
        val config = createConfig(
            schemaVersion = 2,
            deployments = listOf(
                Deployment.Qapps(isRelease = true)
            )
        )

        validator.validate(config)
    }

    @Test
    fun `validate - fails with Qapps deployment and schema version greater than 2`() {
        val config = createConfig(
            schemaVersion = 3,
            deployments = listOf(
                Deployment.Qapps(isRelease = false)
            )
        )

        val exception = assertThrows<IllegalArgumentException> {
            validator.validate(config)
        }

        assertThat(exception.message).contains("Qapps deployments is supported only in the 2'nd version of contract")
    }

    @Test
    fun `validate - passes with one Qapps deployment mixed with other deployments`() {
        val config = createConfig(
            schemaVersion = 2,
            deployments = listOf(
                Deployment.GooglePlay(
                    artifactType = AndroidArtifactType.BUNDLE,
                    buildVariant = "release",
                    track = Deployment.Track.production
                ),
                Deployment.Qapps(isRelease = true),
                Deployment.RuStore(
                    artifactType = AndroidArtifactType.APK
                )
            )
        )

        validator.validate(config)
    }

    @Test
    fun `validate - fails with multiple Qapps deployments`() {
        val config = createConfig(
            schemaVersion = 2,
            deployments = listOf(
                Deployment.Qapps(isRelease = true),
                Deployment.Qapps(isRelease = false)
            )
        )

        val exception = assertThrows<IllegalArgumentException> {
            validator.validate(config)
        }

        assertThat(exception.message).contains("Must be one Qapps deployment")
    }

    @Test
    fun `validate - passes with empty deployments list`() {
        val config = createConfig(
            schemaVersion = 2,
            deployments = emptyList()
        )

        validator.validate(config)
    }

    private fun createConfig(
        schemaVersion: Long,
        deployments: List<Deployment>,
    ): CdBuildConfigV2 {
        return CdBuildConfigV2(
            schemaVersion = schemaVersion,
            project = "avito",
            outputDescriptor = OutputDescriptor(
                path = "http://example.com/output.json",
                skipUpload = false
            ),
            releaseVersion = "1.0.0",
            deployments = deployments
        )
    }
}
