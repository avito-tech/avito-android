package com.avito.kotlin.dsl

import com.google.common.truth.Truth.assertThat
import com.google.common.truth.Truth.assertWithMessage
import org.gradle.api.Project
import org.gradle.api.internal.provider.Providers
import org.gradle.api.provider.Provider
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.util.UUID

internal class PropertyExtensionsKtTest {

    private var Project.simpleProperty: String by ProjectProperty.lateinit(fallbackValue = "default")

    private val Project.perProjectProperty: String
        by ProjectProperty.lazy(scope = PropertyScope.PER_PROJECT) { UUID.randomUUID().toString() }

    private val Project.singleProperty: String
        by ProjectProperty.lazy(scope = PropertyScope.ROOT_PROJECT) { UUID.randomUUID().toString() }

    private val Project.intProperty: Int by ProjectProperty.lazy { project ->
        project.property("intProperty").toString().toInt()
    }

    private val Project.transformedProviderProperty: Provider<String> by ProjectProperty.lazy { project ->
        Providers.of(UUID.randomUUID().toString() + project.providerProperty.get())
    }

    private val Project.mappedProviderProperty: Provider<String> by ProjectProperty.lazy { project ->
        project.providerProperty.map { UUID.randomUUID().toString() }
    }

    // use this form to achieve single value for every Provider.get() call
    private val Project.providerProperty: Provider<String> by ProjectProperty.lazy {
        Providers.of(UUID.randomUUID().toString())
    }

    // properties below will create new instance on every Provider.get() call

    private val Project.providersProperty: Provider<String> by ProjectProperty.lazy { project ->
        project.providers.provider { UUID.randomUUID().toString() }
    }

    private val Project.providersShortcutProperty: Provider<String> by ProjectProperty.lazy { project ->
        project.provider { UUID.randomUUID().toString() }
    }

    private val Project.providersOfUnitProperty: Provider<String> by ProjectProperty.lazy {
        Providers.of(Unit).map { UUID.randomUUID().toString() }
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

    @Test
    fun `single value - lazy project provider property`() {
        val project = childProject()

        assertStableProvidedValue { project.providerProperty }
    }

    @Test
    fun `single value - lazy transformed project provider property`() {
        val project = childProject()

        assertStableProvidedValue { project.transformedProviderProperty }
    }

    @Test
    fun `different values - lazy project providers property`() {
        val project = childProject()

        assertDifferentProvidedValues { project.providersProperty }
    }

    @Test
    fun `different values - lazy project mapped provider property`() {
        val project = childProject()

        assertDifferentProvidedValues { project.mappedProviderProperty }
    }

    @Test
    fun `different values - lazy project providers shortcut property`() {
        val project = childProject()

        assertDifferentProvidedValues { project.providersShortcutProperty }
    }

    @Test
    fun `different values - lazy project providers of Unit property`() {
        val project = childProject()

        assertDifferentProvidedValues { project.providersOfUnitProperty }
    }

    private fun <T : Any> assertStableValue(provider: () -> T): T {
        val first = provider()
        val second = provider()

        assertThat(first).isSameInstanceAs(second)
        return first
    }

    private fun <T : Any> assertStableProvidedValue(provider: () -> Provider<T>) {
        val first = provider().get()
        val second = provider().get()

        assertThat(first).isSameInstanceAs(second)
    }

    private fun <T : Any> assertDifferentProvidedValues(provider: () -> Provider<T>) {
        val first = provider().get()
        val second = provider().get()

        assertThat(first).isNotSameInstanceAs(second)
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
}
