@file:Suppress("TestFunctionName")

package com.avito.android.instant_feedback.internal.model

internal fun createPayload(
    author: String,
    authorEmail: String,
    displayName: String,
    title: String,
    repository: String
): String = """
    {
      "eventKey": "pr:merged",
      "actor": {
        "name": "$author",
        "emailAddress": "$authorEmail",
        "displayName": "$displayName"
      },
      "pullRequest": {
        "id": 1000,
        "title": "$title",
        "toRef": {
          "repository": {
            "name": "$repository"
          }
        }
      }
    }
""".trimIndent()
