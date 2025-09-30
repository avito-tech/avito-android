package com.avito.android.instant_feedback.internal.model

import com.google.common.truth.Truth.assertThat
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Test

class PullRequestMergedEventSerializationTest {

    private val json = Json {
        encodeDefaults = true
        ignoreUnknownKeys = true
        isLenient = true
    }

    private val model = PullRequestMergedEvent(
        user = User(
            username = "jaina",
            fullname = "Jaina Proudmoore",
            email = "jproudmoore@avito.ru"
        ),
        pr = PullRequest(
            id = 1000L,
            title = "MBSA-123: Freeze Arthas before he gets the crown",
            ref = RefMetadata(
                repository = Repository(
                    name = "avito-ios"
                )
            )
        )
    )

    private val payload: String
        @Language("JSON")
        get() = """
        {
          "eventKey": "pr:merged",
          "actor": {
            "name": "jaina",
            "emailAddress": "jproudmoore@avito.ru",
            "displayName": "Jaina Proudmoore"
          },
          "pullRequest": {
            "id": 1000,
            "title": "MBSA-123: Freeze Arthas before he gets the crown",
            "toRef": {
              "repository": {
                "name": "avito-ios"
              }
            }
          }
        }
    """.trimIndent()

    @Test
    fun deserialize() {
        val deserialized = json.decodeFromString<PullRequestMergedEvent>(payload)
        assertThat(deserialized).isEqualTo(model)
    }
}
