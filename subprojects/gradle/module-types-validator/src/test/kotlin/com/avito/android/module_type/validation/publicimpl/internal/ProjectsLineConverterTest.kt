package com.avito.android.module_type.validation.publicimpl.internal

import com.avito.android.module_type.FunctionalType
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

class ProjectsLineConverterTest {

    @Test
    fun `input random line - empty output`() {
        val converter = ProjectsLineConverter()
        val line = "any string"

        assertThat(converter.convert(line)).isNull()
    }

    @Test
    fun `input project line - return project info`() {
        val converter = ProjectsLineConverter()
        val line = "project :lib-c:impl"

        assertThat(converter.convert(line)).isEqualTo(
            ProjectConvertedData(
                modulePath = ":lib-c:impl",
                logicalModule = ":lib-c",
                functionalType = FunctionalType.Impl,
                level = 1
            )
        )
    }

    @Test
    fun `input project line with level 2 - return project info`() {
        val converter = ProjectsLineConverter()
        val line = "|    +--- project :lib-a:public"

        assertThat(converter.convert(line)).isEqualTo(
            ProjectConvertedData(
                modulePath = ":lib-a:public",
                logicalModule = ":lib-a",
                functionalType = FunctionalType.Public,
                level = 2
            )
        )
    }

    @Test
    fun `input project line with composite implementation - return project info`() {
        val converter = ProjectsLineConverter()
        val line = "project :lib-a:impl-a"

        assertThat(converter.convert(line)).isEqualTo(
            ProjectConvertedData(
                modulePath = ":lib-a:impl-a",
                logicalModule = ":lib-a",
                functionalType = FunctionalType.Impl,
                level = 1
            )
        )
    }

    @Test
    fun `input project line without logical module - return project info without logical module`() {
        val converter = ProjectsLineConverter()
        val line = "project :lib-a"

        assertThat(converter.convert(line)).isEqualTo(
            ProjectConvertedData(
                modulePath = ":lib-a",
                logicalModule = "",
                functionalType = null,
                level = 1
            )
        )
    }
}
