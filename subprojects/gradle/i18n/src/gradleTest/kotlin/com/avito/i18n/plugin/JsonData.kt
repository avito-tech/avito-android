package com.avito.i18n.plugin

import org.intellij.lang.annotations.Language

@Language("JSON")
val RESPONSE_BODY = """
    {
      "result": {
        "data": {
          "componentSlug": "",
          "namespaceSlug": "",
          "targetTextUnits": {
            "en": {
              "textUnits": [
                {
                  "key": "some_string",
                  "other": "emos gnirts"
                },
                {
                  "key": "params_string",
                  "other": "smarap %s gnirts %d"
                }
              ]
            }
          }
        },
        "error": null
      }
    }
    """.trimIndent()

@Language("JSON")
val RESPONSE_BODY_TRANSLATABLE_FALSE = """
    {
      "result": {
        "data": {
          "componentSlug": "",
          "namespaceSlug": "",
          "targetTextUnits": {
            "en": {
              "textUnits": [
                {
                  "key": "params_string",
                  "other": "smarap %s gnirts %d"
                }
              ]
            }
          }
        },
        "error": null
      }
    }
    """.trimIndent()

@Language("JSON")
val RESPONSE_BODY_CHANGE_STRING = """
    {
      "result": {
        "data": {
          "componentSlug": "",
          "namespaceSlug": "",
          "targetTextUnits": {
            "en": {
              "textUnits": [
                {
                  "key": "some_string",
                  "other": "emos gnirts 321"
                }
              ]
            }
          }
        },
        "error": null
      }
    }
    """.trimIndent()
