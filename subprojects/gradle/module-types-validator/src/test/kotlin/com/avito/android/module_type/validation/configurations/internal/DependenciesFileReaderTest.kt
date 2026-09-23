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
        assertThat(dependencies).hasSize(10)
        assertThat(dependencies).contains(
            ProjectDependencyInfo(
                modulePath = ":lib-a:public",
                fullPath = "lib-c:demo -> :lib-b:fake -> :lib-a:public",
                logicalModule = ":lib-a",
                functionalType = FunctionalType.Public
            )
        )
    }

    @Test
    fun `return dependencies grouped by configuration`() {
        val dependenciesFileReader = DependenciesFileReader(INPUT_TEXT, "lib-c:demo")
        val configurations = dependenciesFileReader.readDependenciesByConfiguration()

        assertThat(configurations).hasSize(2)
        assertThat(configurations[0]).hasSize(8)
        assertThat(configurations[1]).containsExactly(
            ProjectDependencyInfo(
                modulePath = ":lib-test:impl",
                fullPath = "lib-c:demo -> :lib-test:impl",
                logicalModule = ":lib-test",
                functionalType = FunctionalType.Impl
            ),
            ProjectDependencyInfo(
                modulePath = ":lib-test:public",
                fullPath = "lib-c:demo -> :lib-test:impl -> :lib-test:public",
                logicalModule = ":lib-test",
                functionalType = FunctionalType.Public
            )
        ).inOrder()
    }

    companion object {
        private val INPUT_TEXT = """
            debugCompileClasspath
            :lib-c:impl
                :lib-b:public
            :lib-b:fake
                :lib-a:public
                :utils
                :lib-b:public
            :lib-a:impl
                :lib-a:public

            debugAndroidTestCompileClasspath
            :lib-test:impl
                :lib-test:public
        """.trimIndent()
    }
}
