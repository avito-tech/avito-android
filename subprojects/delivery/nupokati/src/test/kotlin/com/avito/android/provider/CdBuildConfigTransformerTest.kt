package com.avito.android.provider

import com.avito.android.model.AndroidArtifactType
import com.avito.android.model.CdBuildConfig
import com.avito.android.model.CdBuildConfig.Deployment
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Path

internal class CdBuildConfigTransformerTest {

    private lateinit var testProjectDir: File

    @BeforeEach
    fun setup(@TempDir tempPath: Path) {
        testProjectDir = tempPath.toFile()
    }

    @Test
    fun `parse all known deployments`() {
        val config = parseConfig(
            """
            {
                "schema_version": 2, 
                "project": "avito", 
                "release_version": "129.0", 
                "output_descriptor": {
                    "path": "http://artifactory.ru/artifactory/releases/avito/129.0_1/release_info.json", 
                    "skip_upload": false
                 }, 
                 "deployments": [
                    {
                        "type": "google-play", 
                        "artifact_type": "bundle", 
                        "build_variant": "release", 
                        "track": "beta"
                    }, 
                    {
                        "type": "ru-store", 
                        "artifact_type": "apk"
                    },
                    {
                        "type": "qapps", 
                        "is_release": true
                    }
                 ]
            }
        """.trimIndent()
        )

        assertThat(config.deployments).hasSize(3)

        config.deployments[0].also { deployment ->
            assertThat(deployment).isInstanceOf(Deployment.GooglePlay::class.java)

            deployment as Deployment.GooglePlay
            assertThat(deployment.artifactType).isEqualTo(AndroidArtifactType.BUNDLE)
        }

        config.deployments[1].also { deployment ->
            assertThat(deployment).isInstanceOf(Deployment.RuStore::class.java)

            deployment as Deployment.RuStore
            assertThat(deployment.artifactType).isEqualTo(AndroidArtifactType.APK)
        }

        config.deployments[2].also { deployment ->
            assertThat(deployment).isInstanceOf(Deployment.Qapps::class.java)
        }
    }

    private fun parseConfig(config: String): CdBuildConfig {
        val inputFile = File(testProjectDir, "config.json")
        inputFile.writeText(config)

        return CdBuildConfigTransformer(StubCdBuildConfigValidator)
            .transform { inputFile }
    }
}
