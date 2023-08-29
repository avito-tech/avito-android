package com.avito.android.module_type.validation.publicimpl.internal

import com.avito.android.module_type.FunctionalType
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class DependenciesFileReaderTest {

    @Test
    fun `read file - return correct dependencies`(@TempDir reportDirectory: File) {
        val inputFile = prepareReportFile(reportDirectory)

        val dependenciesFileReader = DependenciesFileReader(inputFile, "lib-c:demo")
        val dependencies = dependenciesFileReader.readProjectDependencies()
        assertThat(dependencies).hasSize(8)
        assertThat(dependencies).contains(
            ProjectDependencyInfo(
                modulePath = ":lib-a:public",
                fullPath = "lib-c:demo -> :lib-b:fake -> :lib-a:public",
                level = 2,
                logicalModule = ":lib-a",
                functionalType = FunctionalType.Public
            )
        )
    }

    private fun prepareReportFile(directory: File): File {
        val text = """
            
            ------------------------------------------------------------
            Project ':lib-c:demo'
            ------------------------------------------------------------

            apiDependenciesMetadata
            No dependencies

            implementationDependenciesMetadata
            +--- project :lib-c:impl
            |    \--- project :lib-b:public
            +--- project :lib-b:fake
            |    +--- project :lib-a:public
            |    +--- project :utils
            |    \--- project :lib-b:public
            +--- project :lib-a:impl
            |    \--- project :lib-a:public
            \--- org.jetbrains.kotlin:kotlin-stdlib:1.7.10
                 +--- org.jetbrains.kotlin:kotlin-stdlib-common:1.7.10
                 \--- org.jetbrains:annotations:13.0

            (*) - dependencies omitted (listed previously)

            A web-based, searchable dependency report is available by adding the --scan option.
        """.trimIndent()
        val reportFile = File(directory, "report.txt")
        reportFile.writeText(text)
        return reportFile
    }
}
