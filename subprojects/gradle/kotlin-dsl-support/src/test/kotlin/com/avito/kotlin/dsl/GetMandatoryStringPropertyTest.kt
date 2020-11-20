package com.avito.kotlin.dsl

import com.google.common.truth.Truth.assertThat
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class GetMandatoryStringPropertyTest {

    @Test
    fun `getMandatoryStringProperty - throws exception - on empty string if not allowed`() {
        val project = ProjectBuilder.builder().build()
        project.extensions.extraProperties["someProperty"] = ""

        assertThrows<RuntimeException> {
            project.getMandatoryStringProperty("someProperty", allowBlank = false)
        }
    }

    // todo throw by default
    @Test
    fun `getMandatoryStringProperty - returns empty value - on empty string value by default`() {
        val project = ProjectBuilder.builder().build()
        project.extensions.extraProperties["someProperty"] = ""

        val value = project.getMandatoryStringProperty("someProperty", allowBlank = true)
        assertThat(value).isEqualTo("")
    }

    @Test
    fun `getMandatoryStringProperty - returns empty value - on empty string if allowed`() {
        val project = ProjectBuilder.builder().build()
        project.extensions.extraProperties["someProperty"] = ""

        val value = project.getMandatoryStringProperty("someProperty", allowBlank = true)
        assertThat(value).isEqualTo("")
    }

    @Test
    fun `getMandatoryStringProperty - throws exception - on no property`() {
        val project = ProjectBuilder.builder().build()

        assertThrows<RuntimeException> {
            project.getMandatoryStringProperty("someProperty")
        }
    }

    @Test
    fun `getMandatoryStringProperty - returns correct value`() {
        val project = ProjectBuilder.builder().build()
        project.extensions.extraProperties["someProperty"] = "12345"

        val value = project.getMandatoryStringProperty("someProperty")
        assertThat(value).isEqualTo("12345")
    }
}
