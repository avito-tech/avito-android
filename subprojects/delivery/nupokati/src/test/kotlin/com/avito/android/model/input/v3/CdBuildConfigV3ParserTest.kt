package com.avito.android.model.input.v3

import com.avito.android.model.input.AndroidArtifactType
import com.avito.android.model.input.CdBuildConfigV3
import com.avito.truth.assertThat
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.api.io.TempDir
import java.io.File

internal class CdBuildConfigV3ParserTest {

    private data class Case(val name: String, val assertion: (CdBuildConfigV3) -> Unit)

    @TestFactory
    fun `parse all known deployments`(@TempDir testProjectDir: File): List<DynamicTest> {
        val inputFile = File(testProjectDir, "config.json")

        inputFile.writeText(
            """
            {
                "schema_version": 3, 
                "project": "avito", 
                "release_version": "129.0", 
                "output_descriptor": {
                    "path": "http://artifactory.ru/artifactory/releases/avito/129.0_1/release_info.json", 
                    "skip_upload": false
                 }, 
                 "deployments": [
                    {
                        "type": "app-binary",
                        "store": "google-play", 
                        "file_type": "bundle", 
                        "build_configuration": "release" 
                    }, 
                    {
                        "type": "app-binary",
                        "store": "ru-store", 
                        "file_type": "apk",
                        "build_configuration": "release"
                    },
                    {
                        "type": "qapps", 
                        "is_release": true
                    },
                    {
                        "type": "artifact", 
                        "file_type": "json",
                        "kind" : "feature-toggles" 
                    }
                 ]
            }
        """.trimIndent()
        )

        val config = CdBuildConfigParser().transform { inputFile }

        return listOf(
            Case("size") {
                assertThat(it.deployments).hasSize(4)
            },
            Case("first deployment is google play") {
                config.deployments[0].also { deployment ->
                    assertThat<CdBuildConfigV3.Deployment.AppBinary>(deployment) {
                        assertThat(store).isEqualTo("google-play")
                        assertThat(fileType).isEqualTo(AndroidArtifactType.BUNDLE)
                        assertThat(buildConfiguration).isEqualTo("release")
                    }
                }
            },
            Case("second deployment is ru store") {
                config.deployments[1].also { deployment ->
                    assertThat<CdBuildConfigV3.Deployment.AppBinary>(deployment) {
                        assertThat(store).isEqualTo("ru-store")
                        assertThat(fileType).isEqualTo(AndroidArtifactType.APK)
                        assertThat(buildConfiguration).isEqualTo("release")
                    }
                }
            },
            Case("third deployment is qapps") {
                config.deployments[2].also { deployment ->
                    assertThat<CdBuildConfigV3.Deployment.Qapps>(deployment) {
                        assertThat(isRelease).isEqualTo(true)
                    }
                }
            },
            Case("fourth deployment is feature toggles artifact") {
                config.deployments[3].also { deployment ->
                    assertThat<CdBuildConfigV3.Deployment.Artifact>(deployment) {
                        assertThat(fileType).isEqualTo("json")
                        assertThat(kind).isEqualTo("feature-toggles")
                    }
                }
            }
        )
            .map { case -> dynamicTest(case.name) { case.assertion.invoke(config) } }
    }
}
