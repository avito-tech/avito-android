package com.avito.android.module_type.validation.configurations.internal

import com.avito.android.module_type.validation.configurations.missings.implementations.internal.ProjectListFileReader
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

class ProjectListFileReaderTest {

    @Test
    fun `return correct project list`() {
        val reader = ProjectListFileReader(INPUT_TEXT)
        assertThat(reader.readProjectList()).isEqualTo(EXPECTED_OUTPUT)
    }

    companion object {
        private val INPUT_TEXT = """
            ------------------------------------------------------------
            Root project 'avito-android'
            ------------------------------------------------------------
            
            Root project 'avito-android'
            +--- Project ':common'
            |    \--- Project ':common:analytics'
            |    |    +--- Project ':common:analytics:fake'
            |    |    +--- Project ':common:analytics:impl'
            |    |    \--- Project ':common:analytics:public'
            \--- Project ':platform'
            
            Included builds
            +--- Included build ':build-logic-settings'
            \--- Included build ':build-logic'
            
            To see a list of the tasks of a project, run gradlew <project-path>:tasks
            For example, try running gradlew :common:tasks
        """.trimIndent()

        private val EXPECTED_OUTPUT = listOf(
            ":common",
            ":common:analytics",
            ":common:analytics:fake",
            ":common:analytics:impl",
            ":common:analytics:public",
            ":platform",
        )
    }
}
