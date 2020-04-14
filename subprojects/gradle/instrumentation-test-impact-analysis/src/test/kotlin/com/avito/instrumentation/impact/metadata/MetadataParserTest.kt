package com.avito.instrumentation.impact.metadata

import com.avito.utils.logging.CILogger
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

internal class MetadataParserTest {

    @Test
    fun `rootId package`(@TempDir projectDir: File) {
        File(projectDir, "NewFile.kt").apply {
            writeText(
                """
                import com.test.pkg.R
                    
                class NewClass : Screen {
                
                    val rootId: Int = R.id.something_root
                }
            """.trimIndent()
            )
        }

        val result = MetadataParser(
            ciLogger = CILogger.allToStdout,
            screenClass = "com.test.Screen",
            fieldName = "rootId"
        ).parseMetadata(setOf(projectDir))

        assertThat(result).containsEntry("NewClass", "com.test.pkg")
    }

    @Test
    fun `rootId package via getter`(@TempDir projectDir: File) {
        File(projectDir, "NewFile.kt").apply {
            writeText(
                """
                import com.test.pkg.R
                    
                class NewClass : Screen {
                
                    val rootId: Int
                        get() = R.id.something_root
                }
            """.trimIndent()
            )
        }

        val result = MetadataParser(
            ciLogger = CILogger.allToStdout,
            screenClass = "com.test.Screen",
            fieldName = "rootId"
        ).parseMetadata(setOf(projectDir))

        assertThat(result).containsEntry("NewClass", "com.test.pkg")
    }

    @Test
    fun test2() {
        val result = getPackageName("com.test.R.id.some_layout", emptyList())

        assertThat(result).isEqualTo("com.test")
    }

    @Test
    fun test3() {
        val result = getPackageName("R.id.some_layout", listOf("com.test.R"))

        assertThat(result).isEqualTo("com.test")
    }
}
