package com.avito.impact.util

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.TestFactory
import java.io.File

internal class AndroidManifestPackageParserTest {

    data class Case(val name: String, val file: File, val expectedPackage: String)

    @Suppress("MaxLineLength")
    @TestFactory
    fun `parse - success - valid files`() = listOf(
        createCase(
            caseName = "oneliner",
            manifestContent = "<manifest package=\"com.test.account\" xmlns:android=\"http://schemas.android.com/apk/res/android\" />",
            expectedPackage = "com.test.account"
        ),
        createCase(
            caseName = "multiline",
            manifestContent = "<manifest package=\"com.test.account\"\nxmlns:android=\"http://schemas.android.com/apk/res/android\" />",
            expectedPackage = "com.test.account"
        ),
        createCase(
            caseName = "underscore",
            manifestContent = "<manifest package=\"com.test.one_two\" xmlns:android=\"http://schemas.android.com/apk/res/android\" />",
            expectedPackage = "com.test.one_two"
        ),
        createCase(
            caseName = "number",
            manifestContent = "<manifest package=\"com.test2.account\" xmlns:android=\"http://schemas.android.com/apk/res/android\" />",
            expectedPackage = "com.test2.account"
        ),
        createCase(
            caseName = "capitalLetters",
            manifestContent = "<manifest package=\"com.someModule\" xmlns:android=\"http://schemas.android.com/apk/res/android\" />",
            expectedPackage = "com.someModule"
        )
    ).map { case ->
        dynamicTest(case.name) {
            val result = AndroidManifestPackageParser.parse(case.file)
            assertThat(result).isEqualTo(case.expectedPackage)
        }
    }

    private fun createCase(caseName: String, manifestContent: String, expectedPackage: String): Case {
        return Case(
            name = caseName,
            file = File.createTempFile(caseName, null).apply { writeText(manifestContent) },
            expectedPackage = expectedPackage
        )
    }
}
