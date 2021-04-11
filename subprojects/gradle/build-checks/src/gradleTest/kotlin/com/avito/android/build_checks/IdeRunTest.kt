package com.avito.android.build_checks

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

internal class IdeRunTest {

    @Test
    fun `ide sync - no side effects`(@TempDir projectDir: File) {
        val result = BuildChecksTestProjectRunner(
            projectDir,
            buildChecksExtension = """
                enableByDefault = false
                // Fail checks
                javaVersion {
                    version = JavaVersion.VERSION_1_1
                }
                androidSdk {
                    compileSdkVersion = 1
                    revision = 1
                }
            """.trimIndent()
        )
            .runChecks(isIdeSync = true)

        result.assertThat().buildSuccessful()
    }
}
