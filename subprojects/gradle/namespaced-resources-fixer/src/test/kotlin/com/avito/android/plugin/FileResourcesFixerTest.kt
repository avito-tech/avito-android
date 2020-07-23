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
        val app = AndroidModule("com.app.R", emptyList())
        val libraries = listOf(
            AndroidModule("com.library", listOf("container"))
        )
        val result = fix(
            """
            import com.library.R
            val id = R.id.container
        """.trimIndent(),
            app, libraries
        )
        assertThat(result).isEqualTo(
            """
            import com.library.R
            val id = R.id.container
        """.trimIndent()
        )
    }

    @Test
    fun `change nothing - not used merged R class`() {
        val app = AndroidModule("com.app.R", emptyList())
        val libraries = emptyList<AndroidModule>()
        val result = fix(
            """
            import com.app.R
            import com.library.R
            val id = R.id.container
        """.trimIndent(),
            app, libraries
        )
        assertThat(result).isEqualTo(
            """
            import com.app.R
            import com.library.R
            val id = R.id.container
        """.trimIndent()
        )
    }

    @Test
    fun `change nothing - ambiguous resource`() {
        val app = AndroidModule("com.app.R", emptyList())
        val libraries = listOf(
            AndroidModule("com.avito.android.a", listOf("container")),
            AndroidModule("com.avito.android.b", listOf("container"))
        )
        val result = fix(
            """
            import com.app.R

            val id = R.id.container
        """.trimIndent(),
            app, libraries
        )
        assertThat(result).isEqualTo(
            """
            import com.app.R

            val id = R.id.container
        """.trimIndent()
        )
    }

    @Test
    fun `replace library id - in property`() {
        val app = AndroidModule("com.app.R", emptyList())
        val libraries = listOf(
            AndroidModule("com.library", listOf("container"))
        )
        val result = fix(
            """
            import com.app.R

            val id = R.id.container
        """.trimIndent(),
            app, libraries
        )
        assertThat(result).isEqualTo(
            """
            import com.app.R
            import com.library.R as library_R

            val id = library_R.id.container
        """.trimIndent()
        )
    }

    @Test
    fun `replace library id - in function argument`() {
        val app = AndroidModule("com.app.R", emptyList())
        val libraries = listOf(
            AndroidModule("com.library", listOf("icon"))
        )
        val result = fix(
            """
            import androidx.test.espresso.matcher.ViewMatchers
            import com.app.R
            import com.library.ViewElement

            val icon = element<ViewElement>(ViewMatchers.withId(R.id.icon))
        """.trimIndent(),
            app, libraries
        )
        assertThat(result).isEqualTo(
            """
            import androidx.test.espresso.matcher.ViewMatchers
            import com.app.R
            import com.library.ViewElement
            import com.library.R as library_R

            val icon = element<ViewElement>(ViewMatchers.withId(library_R.id.icon))
        """.trimIndent()
        )
    }

    @Test
    fun `replace library id - merged R has alias`() {
        val app = AndroidModule("com.app.R", emptyList())
        val libraries = listOf(
            AndroidModule("com.library", listOf("container"))
        )
        val result = fix(
            """
            import com.app.R as custom_alias_R

            val id = custom_alias_R.id.container
        """.trimIndent(),
            app, libraries
        )
        assertThat(result).isEqualTo(
            """
            import com.app.R as custom_alias_R
            import com.library.R as library_R

            val id = library_R.id.container
        """.trimIndent()
        )
    }

    @Test
    fun `replace library id - library R has alias`() {
        val app = AndroidModule("com.app.R", emptyList())
        val libraries = listOf(
            AndroidModule("com.library", listOf("container"))
        )
        val result = fix(
            """
            import com.app.R
            import com.library.R as custom_alias_R

            val id1 = R.id.container
            val id2 = custom_alias_R.id.container
        """.trimIndent(),
            app, libraries
        )
        assertThat(result).isEqualTo(
            """
            import com.app.R
            import com.library.R as custom_alias_R

            val id1 = custom_alias_R.id.container
            val id2 = custom_alias_R.id.container
        """.trimIndent()
        )
    }

    @Test
    fun `replace library id - app and library has aliases`() {
        val app = AndroidModule("com.app.R", emptyList())
        val libraries = listOf(
            AndroidModule("com.library", listOf("container"))
        )
        val result = fix(
            """
            import com.app.R as app_R
            import com.library.R as library_R

            val id1 = app_R.id.container
            val id2 = library_R.id.container
        """.trimIndent(),
            app, libraries
        )
        assertThat(result).isEqualTo(
            """
            import com.app.R as app_R
            import com.library.R as library_R

            val id1 = library_R.id.container
            val id2 = library_R.id.container
        """.trimIndent()
        )
    }

    private fun fix(content: String, app: AndroidModule, modules: List<AndroidModule>): String? {
        file.writeText(content)

        return FileResourcesFixer().fixMergedResources(file, app, modules) ?: content
    }
}
