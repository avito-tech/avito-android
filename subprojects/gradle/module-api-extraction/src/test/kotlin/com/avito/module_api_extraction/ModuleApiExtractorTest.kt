package com.avito.module_api_extraction

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

internal class ModuleApiExtractorTest {
    @Test
    fun extract(@TempDir tempDir: File) {
        val jsonFiles = INPUT_JSON_TEXTS.mapIndexed { i, jsonText ->
            val jsonFile = tempDir.resolve("$i.json")
            jsonFile.writeText(jsonText)
            jsonFile
        }.toSet()

        val extractor = ModuleApiExtractor()
        val actualOutputJsonText = extractor.extract(INPUT_MODULE_NAMES, jsonFiles)
        assertEquals(EXPECTED_OUTPUT_JSON_TEXT, actualOutputJsonText)
    }

    companion object {
        private val INPUT_MODULE_NAMES = listOf(
            ":a", /* src */
            ":b", /* src */
            ":b" /* test */
        )

        private val INPUT_JSON_TEXTS = listOf(
            """
            {
                "sources": [
                    {
                        "type": "code",
                        "relativePath": "src/main/java/com/avito/android/A.kt",
                        "className": "com.avito.android.A",
                        "usedClasses": [
                            "kotlin.Metadata"
                        ]
                    },
                    {
                        "type": "code",
                        "relativePath": "src/main/java/com/avito/android/AImpl.kt",
                        "className": "com.avito.android.AImpl",
                        "usedClasses": [
                            "com.avito.android.A"
                        ]
                    },
                    {
                        "type": "code",
                        "relativePath": "src/main/java/com/avito/android/A2.kt",
                        "className": "com.avito.android.A2",
                        "usedClasses": [
                        ]
                    }
                ]
            }
            """,
            """
            {
                "sources": [
                    {
                        "type": "code",
                        "relativePath": "src/main/java/com/avito/android/B.kt",
                        "className": "com.avito.android.B",
                        "usedClasses": [
                            "com.avito.android.A"
                        ]
                    },
                    {
                        "type": "code",
                        "relativePath": "src/main/java/com/avito/android/BImpl.kt",
                        "className": "com.avito.android.BImpl",
                        "usedClasses": [
                            "com.avito.android.A",
                            "com.avito.android.B"
                        ]
                    },
                    {
                        "type": "code",
                        "relativePath": "tmp/kotlin-classes/anvil/A2Kt.class",
                        "className": "com.avito.android.A2Kt",
                        "usedClasses": [
                            "com.avito.android.A2"
                        ]
                    },
                    {
                        "type": "code",
                        "relativePath": "intermediates/kotlin-classes/dagger/A2Kt2.java",
                        "className": "com.avito.android.A2Kt2",
                        "usedClasses": [
                            "com.avito.android.A2"
                        ]
                    },
                    {
                        "type": "android_res",
                        "relativePath": "src/main/AndroidManifest.xml"
                    }
                ]
            }
            """,
            """
            {
                "sources": [
                    {
                        "type": "code",
                        "relativePath": "src/main/java/com/avito/android/BTest.kt",
                        "className": "com.avito.android.BTest",
                        "usedClasses": [
                            "com.avito.android.B"
                        ]
                    }
                ]
            }
            """,
        )

        private val EXPECTED_OUTPUT_JSON_TEXT = """
        {
          ":a": [
            "com.avito.android.A"
          ],
          ":b": []
        }
        """.trimIndent()
    }
}
