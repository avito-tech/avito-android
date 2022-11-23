package com.avito.tech_budget.warnings

import com.avito.android.model.FakeOwners
import com.avito.android.tech_budget.internal.warnings.log.LogEntry
import com.avito.android.tech_budget.internal.warnings.log.ProjectInfo
import com.avito.android.tech_budget.internal.warnings.log.converter.LogToWarningConverter
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

internal class LogToWarningConverterTest {

    private val converter = LogToWarningConverter()

    @Test
    fun `convert valid log entry - project path parsed`() {
        val projectPath = ":app"
        val entry = LogEntry(
            ProjectInfo(projectPath, owners = listOf()),
            taskName = "compile",
            rawText = "w: /project/src/kotlin/AdvertList.kt: (35, 18): 'AdvertItem' is deprecated. Migrate to Parcelize"
        )

        val warning = converter.convert(entry)

        assertThat(warning.moduleName).isEqualTo(projectPath)
    }

    @Test
    fun `convert valid log entry - owners parsed`() {
        val owners = listOf(FakeOwners.Speed, FakeOwners.Messenger)
        val entry = LogEntry(
            ProjectInfo(":app", owners = owners),
            taskName = "compile",
            rawText = "w: /project/src/kotlin/AdvertList.kt: (35, 18): 'AdvertItem' is deprecated. Migrate to Parcelize"
        )

        val warning = converter.convert(entry)

        assertThat(warning.owners).isEqualTo(owners)
    }

    @Test
    fun `convert valid log entry - warning source file parsed`(@TempDir outputDir: File) {
        val fileWithWarning = File(outputDir, "AdvertList.kt")
        fileWithWarning.createNewFile()
        val entry = LogEntry(
            ProjectInfo(":app", owners = listOf()),
            taskName = "compile",
            rawText = "w: $fileWithWarning: (35, 18): 'AdvertItem' is deprecated. Migrate to Parcelize"
        )

        val warning = converter.convert(entry)

        assertThat(warning.sourceFile).isEqualTo(fileWithWarning.path)
    }

    @Test
    fun `convert log entry without prefix - warning source file parsed`(@TempDir outputDir: File) {
        val fileWithWarning = File(outputDir, "AdvertList.kt")
        fileWithWarning.createNewFile()
        val entry = LogEntry(
            ProjectInfo(":app", owners = listOf()),
            taskName = "compile",
            rawText = "$fileWithWarning: (35, 18): 'AdvertItem' is deprecated. Migrate to Parcelize"
        )

        val warning = converter.convert(entry)

        assertThat(warning.sourceFile).isEqualTo(fileWithWarning.path)
    }

    @Test
    fun `convert log entry with non existing file - source returns null`() {
        val fileWithWarning = File("AdvertList.kt")
        val entry = LogEntry(
            ProjectInfo(":app", owners = listOf()),
            taskName = "compile",
            rawText = "w: $fileWithWarning: (35, 18): 'AdvertItem' is deprecated. Migrate to Parcelize"
        )

        val warning = converter.convert(entry)

        assertThat(warning.sourceFile).isNull()
    }

    @Test
    fun `convert log entry with invalid file - source returns null`() {
        val invalidFileWarningText = listOf(
            "w:: 'AdvertItem' is deprecated. Migrate to Parcelize",
            "'AdvertItem' is deprecated: Migrate to Parcelize",
            "w: ",
        )

        invalidFileWarningText.forEach { rawText ->
            val entry = LogEntry(
                ProjectInfo(":app", owners = listOf()),
                taskName = "compile",
                rawText = rawText
            )

            val warning = converter.convert(entry)

            assertThat(warning.sourceFile).isNull()
        }
    }

    @Test
    fun `convert log entry without file - unknown source file parsed`() {
        val entry = LogEntry(
            ProjectInfo(":app", owners = listOf()),
            taskName = "compile",
            rawText = "w: 'AdvertItem' is deprecated. Migrate to Parcelize"
        )

        val warning = converter.convert(entry)

        assertThat(warning.sourceFile).isNull()
    }

    @Test
    fun `convert log entry without prefix - full text parsed`() {
        val entry = LogEntry(
            ProjectInfo(":app", owners = listOf()),
            taskName = "compile",
            rawText = "/project/src/kotlin/AdvertList.kt: (35, 18): 'AdvertItem' is deprecated. Migrate to Parcelize"
        )

        val warning = converter.convert(entry)

        assertThat(warning.fullMessage).isEqualTo(entry.rawText)
    }
}
