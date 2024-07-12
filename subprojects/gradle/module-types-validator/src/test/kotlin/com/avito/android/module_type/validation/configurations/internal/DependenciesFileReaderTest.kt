package com.avito.android.module_type.validation.configurations.internal

import com.avito.android.module_type.FunctionalType
import com.avito.android.module_type.validation.configurations.missings.implementations.internal.DependenciesFileReader
import com.avito.android.module_type.validation.configurations.missings.implementations.internal.ProjectDependencyInfo
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

class DependenciesFileReaderTest {

    @Test
    fun `return correct dependencies`() {
        val dependenciesFileReader = DependenciesFileReader(INPUT_TEXT, "lib-c:demo")
        val dependencies = dependenciesFileReader.readProjectDependencies()
        assertThat(dependencies).hasSize(8)
        assertThat(dependencies).contains(
            ProjectDependencyInfo(
                modulePath = ":lib-a:public",
                fullPath = "lib-c:demo -> :lib-b:fake -> :lib-a:public",
                logicalModule = ":lib-a",
                functionalType = FunctionalType.Public
            )
        )
    }

    companion object {
        private val INPUT_TEXT = """
            
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
    }
}
