package com.avito.android.model.output

import com.avito.android.artifactory_backup.ArtifactV2
import com.avito.android.artifactory_backup.ArtifactsAdapter
import com.google.common.truth.Truth
import kotlinx.serialization.json.JsonElement
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Test

internal class ArtifactsAdapterV2Test {

    private val artifactsAdapter = ArtifactsAdapter(2L)

    private val url = "http://artifactory/"
    private val bundleFileName = "stub.aab"
    private val jsonFileName = "stub.json"

    @Language("json")
    private val json = """[
            |{
            |"artifact":"binary",
            |"type":"bundle",
            |"name":"stub.aab",
            |"uri":"http://artifactory/artifactory/mobile-releases/avito_test_android/1792.0_1/stub.aab",
            |"build_variant":"release"
            |},
            |{
            |"artifact":"file",
            |"type":"json",
            |"name":"stub.json",
            |"uri":"http://artifactory/artifactory/mobile-releases/avito_test_android/1792.0_1/stub.json"
            |}
            |]""".trimMargin().replace("\n", "")

    private val artifacts = listOf(
        ArtifactV2.AndroidBinary(
            type = "bundle",
            name = bundleFileName,
            uri = "${url}artifactory/mobile-releases/avito_test_android/1792.0_1/$bundleFileName",
            buildVariant = "release"
        ),
        ArtifactV2.FileArtifact(
            type = "json",
            name = jsonFileName,
            uri = "${url}artifactory/mobile-releases/avito_test_android/1792.0_1/$jsonFileName"
        ),
    )

    @Test
    fun serialize() {
        val result = artifactsAdapter.toJson(artifacts)
        Truth.assertThat(result).isEqualTo(json)
    }

    @Test
    fun deserialize() {
        val result: JsonElement = artifactsAdapter.fromJson(json)
        Truth.assertThat(result.toString()).isEqualTo(json)
    }
}
