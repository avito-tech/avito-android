package com.avito.android.model.output

import com.google.common.truth.Truth
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Test

internal class ArtifactsAdapterV3Test {

    private val artifactsAdapter = ArtifactsAdapter(3L)

    private val url = "http://artifactory/"
    private val bundleFileName = "stub.aab"
    private val apkFileName = "stub.apk"
    private val jsonFileName = "feature_toggles.json"

    @Language("json")
    private val json = """[
            |{
            |"type":"app-binary",
            |"store":"ru-store",
            |"file_type":"apk",
            |"name":"stub.apk",
            |"uri":"http://artifactory/artifactory/mobile-releases/avito_test_android/1792.0_1/stub.apk",
            |"build_configuration":"release"
            |},
            |{
            |"type":"app-binary",
            |"store":"google-play",
            |"file_type":"bundle",
            |"name":"stub.aab",
            |"uri":"http://artifactory/artifactory/mobile-releases/avito_test_android/1792.0_1/stub.aab",
            |"build_configuration":"release"
            |},
            |{
            |"type":"artifact",
            |"file_type":"json",
            |"name":"feature_toggles.json",
            |"uri":"http://artifactory/artifactory/mobile-releases/avito_test_android/1792.0_1/feature_toggles.json",
            |"kind":"feature-toggles"
            |}
            |]""".trimMargin().replace("\n", "")

    private val artifacts = listOf(
        ArtifactV3.AppBinary(
            store = "ru-store",
            fileType = "apk",
            name = apkFileName,
            uri = "${url}artifactory/mobile-releases/avito_test_android/1792.0_1/$apkFileName",
            buildConfiguration = "release"
        ),
        ArtifactV3.AppBinary(
            store = "google-play",
            fileType = "bundle",
            name = bundleFileName,
            uri = "${url}artifactory/mobile-releases/avito_test_android/1792.0_1/$bundleFileName",
            buildConfiguration = "release"
        ),
        ArtifactV3.FileArtifact(
            fileType = "json",
            name = jsonFileName,
            uri = "${url}artifactory/mobile-releases/avito_test_android/1792.0_1/$jsonFileName",
            kind = "feature-toggles"
        ),
    )

    @Test
    fun serialize() {
        val result = artifactsAdapter.toJson(artifacts)
        Truth.assertThat(result).isEqualTo(json)
    }

    @Test
    fun deserialize() {
        val result = artifactsAdapter.fromJson(json)
        Truth.assertThat(result.toString()).isEqualTo(json)
    }
}
