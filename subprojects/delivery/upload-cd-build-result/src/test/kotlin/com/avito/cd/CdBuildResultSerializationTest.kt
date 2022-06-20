package com.avito.cd

import com.avito.cd.model.BuildVariant
import org.junit.jupiter.api.Test
import org.skyscreamer.jsonassert.JSONAssert
import org.skyscreamer.jsonassert.JSONCompareMode

internal class CdBuildResultSerializationTest {

    @Test
    fun `build result serialized correctly`() {
        val serializedBuildResult = uploadCdGson.toJson(
            CdBuildResult(
                schemaVersion = 1,
                teamcityBuildUrl = "https://teamcity/viewLog.html?buildId=6696546",
                buildNumber = "323",
                releaseVersion = "52.0",
                testResults = CdBuildResult.TestResultsLink(
                    reportUrl =
                    "https://report/run/5cab53abc0c8b00001f03453?" +
                        "q=eyJmaWx0ZXIiOnsiZXJyb3IiOjEsImZhaWwiOjEsIm90aGVyIjoxfX0%3D",
                    reportCoordinates = CdBuildResult.TestResultsLink.ReportCoordinates(
                        planSlug = "AvitoAndroid",
                        jobSlug = "Regress",
                        runId = "1234"
                    )
                ),
                gitBranch = CdBuildResult.GitBranch(
                    name = "branchName",
                    commitHash = "commitHash"
                ),
                artifacts = listOf(
                    CdBuildResult.Artifact.AndroidBinary(
                        type = "bundle",
                        buildVariant = BuildVariant("release"),
                        uri = "http://artifactory/путь к сборке",
                        name = "Avito_release_52.0_323.aab"
                    ),
                    CdBuildResult.Artifact.FileArtifact(
                        type = "feature_toggles",
                        uri = "http://artifactory/file",
                        name = "feature-toggles.json"
                    )
                )
            )
        )
        JSONAssert.assertEquals(serializedBuildResult, expected, JSONCompareMode.STRICT)
    }
}

@Suppress("MaxLineLength")
private val expected = """
    {
      "schema_version": 1,
      "teamcity_build_url": "https://teamcity/viewLog.html?buildId=6696546",
      "build_number": "323",
      "release_version": "52.0",
      "git_branch": {
        "name": "branchName",
        "commit_hash": "commitHash"
      },
      "test_results": {
        "report_url": "https://report/run/5cab53abc0c8b00001f03453?q=eyJmaWx0ZXIiOnsiZXJyb3IiOjEsImZhaWwiOjEsIm90aGVyIjoxfX0%3D",
        "report_coordinates": {
          "plan_slug": "AvitoAndroid",
          "job_slug": "Regress",
          "run_id": "1234"
        }
      },
      "artifacts": [
        {
          "type": "bundle",
          "name": "Avito_release_52.0_323.aab",
          "uri": "http://artifactory/путь к сборке",
          "build_variant": "release"
        },
        {
          "type": "feature_toggles",
          "name": "feature-toggles.json",
          "uri": "http://artifactory/file"
        }
      ]
    }""".trimIndent()
