package com.avito.cd

import com.avito.cd.CdBuildConfig.Deployment.GooglePlay
import com.avito.cd.CdBuildConfig.Deployment.Track
import com.google.common.truth.Truth
import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import org.junit.jupiter.api.Test

private const val actualConfig = """
{
    "schema_version": 1,
    "project": "avito",
    "release_version": "248.0",
    "output_descriptor": {
        "path": "http://foo.bar",
        "skip_upload": true
    },
    "deployments": [
        {
            "type": "google-play",
            "artifact_type": "bundle",
            "build_variant": "release",
            "track": "alpha"
        },
        {
            "type": "google-play",
            "artifact_type": "apk",
            "build_variant": "debug",
            "track": "internal"
        }
    ]
}
"""

class CdBuildConfigDeserializationTest {
    private val gson = GsonBuilder().run {
        setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
        registerTypeAdapter(CdBuildConfig.Deployment::class.java, DeploymentDeserializer)
        create()
    }

    @Test
    fun `build config deserialized correctly`() {
        val deserialized = gson.fromJson<CdBuildConfig>(actualConfig, CdBuildConfig::class.java)
        Truth.assertThat(deserialized)
            .isEqualTo(
                CdBuildConfig(
                    schemaVersion = 1,
                    project = NupokatiProject.AVITO,
                    releaseVersion = "248.0",
                    outputDescriptor = CdBuildConfig.OutputDescriptor(
                        path = "http://foo.bar",
                        skipUpload = true
                    ),
                    deployments = listOf(
                        GooglePlay(
                            AndroidArtifactType.BUNDLE,
                            BuildVariant.RELEASE,
                            Track.ALPHA
                        ),
                        GooglePlay(
                            AndroidArtifactType.APK,
                            BuildVariant.DEBUG,
                            Track.INTERNAL
                        )
                    )
                )
            )
    }
}

