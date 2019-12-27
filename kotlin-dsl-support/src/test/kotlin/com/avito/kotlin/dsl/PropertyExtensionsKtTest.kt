package com.avito.kotlin.dsl

import com.avito.test.gradle.KotlinModule
import com.avito.test.gradle.TestProjectGenerator
import com.avito.test.gradle.gradlew
import com.google.common.truth.Truth.assertThat
import com.google.common.truth.Truth.assertWithMessage
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.util.UUID

internal class PropertyExtensionsKtTest {

    @Test
    fun `optional file input - transforms for nonexistent file to null - if skipIfNotExists used`(@TempDir tempDir: File) {
        TestProjectGenerator(
            modules = listOf(
                KotlinModule(
                    name = "app",
                    plugins = listOf("com.avito.test")
                )
            )
        ).generateIn(tempDir)

        gradlew(tempDir, "app:consumer", "-PuseSkipIfNotExists=true")
            .assertThat()
            .buildSuccessful()
    }

    @Test
    fun `optional file input - fails with error for nonexistent file`(@TempDir tempDir: File) {
        TestProjectGenerator(
            modules = listOf(
                KotlinModule(
                    name = "app",
                    plugins = listOf("com.avito.test")
                )
            )
        ).generateIn(tempDir)

        gradlew(tempDir, "app:consumer", expectFailure = true)
            .assertThat()
            .buildFailed("specified for property 'inputFile' does not exist")
    }

    @Test
    fun `late initialization - lazy project property`() {
        val project = childProject()

        assertWithMessage("default value as fallback")
            .that(project.simpleProperty).isEqualTo("default")

        project.simpleProperty = "custom value"
        assertWithMessage("has value after first modification")
            .that(project.simpleProperty).isEqualTo("custom value")

        val error = assertThrows(RuntimeException::class.java) {
            project.simpleProperty = "second custom value"
        }
        assertThat(error).hasMessageThat().contains("is already set")
    }

    @Test
    fun `produced value is cached - lazy project property`() {
        val project = childProject()

        assertStableValue { project.perProjectProperty }
    }

    @Test
    fun `cached value is not clashed with project property - lazy project property`() {
        val project = childProject()

        // "-PintProperty=0"
        project.extensions.extraProperties["intProperty"] = "0"

        assertThat(project.intProperty).isEqualTo(0)
    }

    @Test
    fun `per project value - lazy project property`() {
        val project = childProject()
        val rootProject = project.rootProject

        val projectValue = assertStableValue { project.perProjectProperty }
        val rootValue = assertStableValue { rootProject.perProjectProperty }

        assertThat(projectValue).isNotEqualTo(rootValue)
    }

    @Test
    fun `single value - lazy project property`() {
        val project = childProject()
        val rootProject = project.rootProject

        val projectValue = assertStableValue { project.singleProperty }
        val rootValue = assertStableValue { rootProject.singleProperty }

        assertThat(projectValue).isSameInstanceAs(rootValue)
    }

    private fun <T : Any> assertStableValue(provider: () -> T): T {
        val first = provider()
        val second = provider()

        assertThat(first).isSameInstanceAs(second)
        return first
    }

    private fun childProject(): Project {
        val rootProject = ProjectBuilder.builder().build()
        val project = ProjectBuilder.builder()
            .withParent(rootProject)
            .build()

        assertThat(project.isRoot()).isFalse()
        assertThat(rootProject.isRoot()).isTrue()
        assertThat(project.rootProject).isSameInstanceAs(rootProject)

        return project
    }

    private var Project.simpleProperty: String by ProjectProperty.lateinit(fallbackValue = "default")
    private val Project.perProjectProperty: String
        by ProjectProperty.lazy(scope = PropertyScope.PER_PROJECT) { UUID.randomUUID().toString() }
    private val Project.singleProperty: String
        by ProjectProperty.lazy(scope = PropertyScope.ROOT_PROJECT) { UUID.randomUUID().toString() }
    private val Project.intProperty: Int by ProjectProperty.lazy { project ->
        project.property("intProperty").toString().toInt()
    }
}
