package com.avito.android.plugin

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class FileResourcesFixerTest {

    private lateinit var file: File

    @BeforeEach
    fun setup(@TempDir tempDir: File) {
        file = File(tempDir, "File.kt")
    }

    @Test
    fun `change nothing - has no import with merged R class`() {
        val app = AndroidModule("com.avito.android.R", emptyList())
        val libraries = emptyList<AndroidModule>()
        val result = fix(
            """
            class Screen {}
        """,
            app, libraries
        )
        assertThat(result).isEqualTo(
            """
            class Screen {}
        """
        )
    }

    @Test
    fun `change nothing - not used import`() {
        val app = AndroidModule("com.avito.android.R", emptyList())
        val libraries = emptyList<AndroidModule>()
        val result = fix(
            """
            import com.avito.android.R
            class Screen {}
        """,
            app, libraries
        )
        assertThat(result).isEqualTo(
            """
            import com.avito.android.R
            class Screen {}
        """
        )
    }

    @Test
    fun `change nothing - ambiguous imports`() {
        val app = AndroidModule("com.avito.android.R", emptyList())
        val libraries = listOf(
            AndroidModule("com.avito.android.a", listOf("container")),
            AndroidModule("com.avito.android.b", listOf("container"))
        )
        val result = fix(
            """
            import com.avito.android.R

            class Screen {
                val id = R.id.container
            }
        """,
            app, libraries
        )
        assertThat(result).isEqualTo(
            """
            import com.avito.android.R

            class Screen {
                val id = R.id.container
            }
        """
        )
    }

    @Test
    fun `replace library id - simple property`() {
        val app = AndroidModule("com.avito.android.R", emptyList())
        val libraries = listOf(
            AndroidModule("com.avito.android.library", listOf("container"))
        )
        val result = fix(
            """
            import com.avito.android.R

            class Screen {
                val id = R.id.container
            }
        """.trimIndent(),
            app, libraries
        )
        assertThat(result).isEqualTo(
            """
            import com.avito.android.R
            import com.avito.android.library.R as library_R

            class Screen {
                val id = library_R.id.container
            }
        """.trimIndent()
        )
    }

    @Test
    fun `replace library id - function argument`() {
        val app = AndroidModule("com.avito.android.R", emptyList())
        val libraries = listOf(
            AndroidModule("com.avito.android.library", listOf("icon"))
        )
        val result = fix(
            """
            import androidx.test.espresso.matcher.ViewMatchers
            import com.avito.android.R
            import com.avito.android.test.page_object.ViewElement

            class Element() : ViewElement {
                val icon = element<ViewElement>(ViewMatchers.withId(R.id.icon))
            }
        """.trimIndent(),
            app, libraries
        )
        assertThat(result).isEqualTo(
            """
            import androidx.test.espresso.matcher.ViewMatchers
            import com.avito.android.R
            import com.avito.android.test.page_object.ViewElement
            import com.avito.android.library.R as library_R

            class Element() : ViewElement {
                val icon = element<ViewElement>(ViewMatchers.withId(library_R.id.icon))
            }
        """.trimIndent()
        )
    }

    private fun fix(content: String, app: AndroidModule, modules: List<AndroidModule>): String? {
        file.writeText(content)

        return FileResourcesFixer().fixMergedResources(file, app, modules) ?: content
    }
}
